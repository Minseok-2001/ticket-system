package ticket.be.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.Payment
import ticket.be.domain.PaymentStatus
import ticket.be.repository.MemberRepository
import ticket.be.repository.PaymentRepository
import ticket.be.repository.ReservationRepository
import ticket.be.service.payment.DummyPaymentGateway
import ticket.be.service.payment.PaymentCancelRequest
import ticket.be.service.payment.PaymentRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 결제 서비스
 * 결제 처리 및 관리를 담당하는 서비스
 */
@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val memberRepository: MemberRepository,
    private val paymentGateway: DummyPaymentGateway,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val notificationCommandService: NotificationCommandService
) {
    private val logger = LoggerFactory.getLogger(PaymentService::class.java)
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

    /**
     * 결제 처리
     */
    @Transactional
    fun processPayment(reservationId: Long): PaymentResult {
        logger.info("결제 처리 시작: reservationId={}", reservationId)

        // 예약 정보 조회
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalArgumentException("예약 정보를 찾을 수 없습니다: id=$reservationId") }

        // 이미 결제 완료된 예약인지 확인
        if (reservation.paymentId != null) {
            logger.warn("이미 결제 완료된 예약: reservationId={}, paymentId={}", reservationId, reservation.paymentId)
            return PaymentResult(
                success = false,
                reservationId = reservationId,
                transactionId = reservation.paymentId,
                errorMessage = "이미 결제 완료된 예약입니다."
            )
        }

        // 예약 상태 확인
        if (reservation.status != ticket.be.domain.ReservationStatus.PENDING) {
            logger.warn("결제 불가능한 예약 상태: reservationId={}, status={}", reservationId, reservation.status)
            return PaymentResult(
                success = false,
                reservationId = reservationId,
                transactionId = null,
                errorMessage = "결제 불가능한 예약 상태입니다: ${reservation.status}"
            )
        }

        try {
            // 결제 요청 생성
            val paymentRequest = PaymentRequest(
                reservationId = reservationId,
                memberId = reservation.member.id,
                amount = reservation.totalAmount,
                paymentMethod = reservation.paymentMethod ?: "CREDIT_CARD",
                description = "${reservation.event.name} 티켓 예매"
            )

            // 결제 게이트웨이로 결제 요청
            val paymentResponse = paymentGateway.processPayment(paymentRequest)

            if (!paymentResponse.success || paymentResponse.transactionId == null) {
                logger.error("결제 처리 실패: reservationId={}, error={}", reservationId, paymentResponse.errorMessage)
                
                // 결제 실패 정보 저장
                val payment = Payment(
                    member = reservation.member,
                    reservation = reservation,
                    amount = reservation.totalAmount,
                    status = PaymentStatus.FAILED,
                    paymentMethod = reservation.paymentMethod ?: "CREDIT_CARD"
                )
                paymentRepository.save(payment)
                
                // 결제 실패 알림 전송
                notificationCommandService.sendNotification(
                    memberId = reservation.member.id,
                    type = "PAYMENT_FAILED",
                    title = "결제 실패",
                    content = "결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.",
                    link = "/reservations/${reservationId}"
                )

                return PaymentResult(
                    success = false,
                    reservationId = reservationId,
                    transactionId = null,
                    errorMessage = paymentResponse.errorMessage ?: "결제 처리 중 오류가 발생했습니다."
                )
            }

            // 결제 성공 처리
            val paidAt = if (paymentResponse.paidAt != null) {
                LocalDateTime.parse(paymentResponse.paidAt, dateTimeFormatter)
            } else {
                LocalDateTime.now()
            }

            // 결제 정보 저장
            val payment = Payment(
                member = reservation.member,
                reservation = reservation,
                amount = reservation.totalAmount,
                status = PaymentStatus.COMPLETED,
                paymentMethod = paymentResponse.paymentMethod,
                transactionId = paymentResponse.transactionId,
                paidAt = paidAt
            )
            val savedPayment = paymentRepository.save(payment)

            // 예약 상태 업데이트
            reservation.confirm()
            reservation.complete(paymentResponse.transactionId)
            reservationRepository.save(reservation)

            // Kafka로 결제 완료 이벤트 발행
            val paymentCompletedEvent = mapOf(
                "eventType" to "PAYMENT_COMPLETED",
                "timestamp" to System.currentTimeMillis(),
                "reservationId" to reservationId,
                "transactionId" to paymentResponse.transactionId,
                "amount" to paymentResponse.amount,
                "memberId" to reservation.member.id
            )
            kafkaTemplate.send("payment-events", objectMapper.writeValueAsString(paymentCompletedEvent))

            // 결제 완료 알림 전송
            notificationCommandService.sendNotification(
                memberId = reservation.member.id,
                type = "PAYMENT_COMPLETED",
                title = "결제 완료",
                content = "${reservation.event.name} 티켓 예매가 확정되었습니다.",
                link = "/reservations/${reservationId}"
            )

            logger.info("결제 처리 성공: reservationId={}, transactionId={}", reservationId, paymentResponse.transactionId)

            return PaymentResult(
                success = true,
                reservationId = reservationId,
                transactionId = paymentResponse.transactionId,
                amount = paymentResponse.amount,
                paidAt = paidAt
            )
        } catch (e: Exception) {
            logger.error("결제 처리 중 오류 발생: reservationId={}, error={}", reservationId, e.message, e)
            return PaymentResult(
                success = false,
                reservationId = reservationId,
                transactionId = null,
                errorMessage = "결제 처리 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    /**
     * 결제 취소
     */
    @Transactional
    fun cancelPayment(reservationId: Long, reason: String?): PaymentResult {
        logger.info("결제 취소 시작: reservationId={}, reason={}", reservationId, reason)

        // 예약 정보 조회
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalArgumentException("예약 정보를 찾을 수 없습니다: id=$reservationId") }

        // 결제 정보 확인
        if (reservation.paymentId == null) {
            logger.warn("결제 정보가 없는 예약: reservationId={}", reservationId)
            return PaymentResult(
                success = false,
                reservationId = reservationId,
                transactionId = null,
                errorMessage = "결제 정보가 없는 예약입니다."
            )
        }

        // 결제 취소 요청
        val cancelRequest = PaymentCancelRequest(
            transactionId = reservation.paymentId!!,
            reason = reason
        )

        try {
            val cancelResponse = paymentGateway.cancelPayment(cancelRequest)

            if (!cancelResponse.success) {
                logger.error("결제 취소 실패: reservationId={}, error={}", reservationId, cancelResponse.errorMessage)
                return PaymentResult(
                    success = false,
                    reservationId = reservationId,
                    transactionId = reservation.paymentId,
                    errorMessage = cancelResponse.errorMessage
                )
            }

            // 예약 상태 업데이트
            reservation.refund(reason)
            reservationRepository.save(reservation)

            // 결제 정보 업데이트
            val payment = paymentRepository.findByReservationIdAndTransactionId(reservationId, reservation.paymentId!!)
            if (payment != null) {
                payment.markAsRefunded(reason ?: "사용자 요청에 의한 취소")
                paymentRepository.save(payment)
            }

            // Kafka로 결제 취소 이벤트 발행
            val paymentCancelledEvent = mapOf(
                "eventType" to "PAYMENT_CANCELLED",
                "timestamp" to System.currentTimeMillis(),
                "reservationId" to reservationId,
                "transactionId" to reservation.paymentId,
                "refundId" to cancelResponse.refundId,
                "memberId" to reservation.member.id,
                "reason" to (reason ?: "사용자 요청에 의한 취소")
            )
            kafkaTemplate.send("payment-events", objectMapper.writeValueAsString(paymentCancelledEvent))

            // 결제 취소 알림 전송
            notificationCommandService.sendNotification(
                memberId = reservation.member.id,
                type = "PAYMENT_CANCELLED",
                title = "결제 취소 완료",
                content = "${reservation.event.name} 티켓 예매 결제가 취소되었습니다.",
                link = "/reservations/${reservationId}"
            )

            logger.info("결제 취소 성공: reservationId={}, transactionId={}, refundId={}", 
                reservationId, reservation.paymentId, cancelResponse.refundId)

            return PaymentResult(
                success = true,
                reservationId = reservationId,
                transactionId = reservation.paymentId,
                refundId = cancelResponse.refundId
            )
        } catch (e: Exception) {
            logger.error("결제 취소 중 오류 발생: reservationId={}, error={}", reservationId, e.message, e)
            return PaymentResult(
                success = false,
                reservationId = reservationId,
                transactionId = reservation.paymentId,
                errorMessage = "결제 취소 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }

    /**
     * 결제 상태 조회
     */
    fun getPaymentStatus(transactionId: String): PaymentStatusResult {
        logger.info("결제 상태 조회: transactionId={}", transactionId)

        try {
            val statusResponse = paymentGateway.checkPaymentStatus(transactionId)

            return PaymentStatusResult(
                transactionId = transactionId,
                status = statusResponse.status,
                paidAt = statusResponse.paidAt?.let { LocalDateTime.parse(it, dateTimeFormatter) },
                amount = statusResponse.amount,
                errorMessage = statusResponse.errorMessage
            )
        } catch (e: Exception) {
            logger.error("결제 상태 조회 중 오류 발생: transactionId={}, error={}", transactionId, e.message, e)
            return PaymentStatusResult(
                transactionId = transactionId,
                status = "ERROR",
                errorMessage = "결제 상태 조회 중 오류가 발생했습니다: ${e.message}"
            )
        }
    }
}

/**
 * 결제 결과
 */
data class PaymentResult(
    val success: Boolean,
    val reservationId: Long,
    val transactionId: String?,
    val refundId: String? = null,
    val amount: java.math.BigDecimal? = null,
    val paidAt: LocalDateTime? = null,
    val errorMessage: String? = null
)

/**
 * 결제 상태 결과
 */
data class PaymentStatusResult(
    val transactionId: String,
    val status: String,
    val paidAt: LocalDateTime? = null,
    val amount: java.math.BigDecimal? = null,
    val errorMessage: String? = null
) 