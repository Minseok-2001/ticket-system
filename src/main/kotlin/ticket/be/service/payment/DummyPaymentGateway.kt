package ticket.be.service.payment

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 더미 결제 게이트웨이 구현
 * 테스트 및 개발 환경에서 실제 결제 게이트웨이 대신 사용
 */
@Service
class DummyPaymentGateway : PaymentGateway {
    
    private val logger = LoggerFactory.getLogger(DummyPaymentGateway::class.java)
    
    // 테스트용 결제 내역 저장소
    private val paymentStore = ConcurrentHashMap<String, DummyPaymentInfo>()
    private val dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    
    override fun processPayment(paymentRequest: PaymentRequest): PaymentResponse {
        logger.info("결제 요청 처리: reservationId={}, amount={}, method={}", 
            paymentRequest.reservationId, paymentRequest.amount, paymentRequest.paymentMethod)
        
        // 임의로 90%의 확률로 성공, 10%의 확률로 실패
        val shouldSucceed = Math.random() > 0.1
        
        if (shouldSucceed) {
            val transactionId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            
            // 테스트 결제 정보 저장
            val paymentInfo = DummyPaymentInfo(
                transactionId = transactionId,
                reservationId = paymentRequest.reservationId,
                memberId = paymentRequest.memberId,
                amount = paymentRequest.amount,
                paymentMethod = paymentRequest.paymentMethod,
                status = "COMPLETED",
                paidAt = now,
                description = paymentRequest.description
            )
            
            paymentStore[transactionId] = paymentInfo
            
            logger.info("결제 성공: transactionId={}", transactionId)
            
            return PaymentResponse(
                success = true,
                transactionId = transactionId,
                errorMessage = null,
                amount = paymentRequest.amount,
                paymentMethod = paymentRequest.paymentMethod,
                paidAt = now.format(dateTimeFormatter)
            )
        } else {
            logger.info("결제 실패: 가상 결제 오류 발생")
            
            return PaymentResponse(
                success = false,
                transactionId = null,
                errorMessage = "결제 처리 중 오류가 발생했습니다.",
                amount = paymentRequest.amount,
                paymentMethod = paymentRequest.paymentMethod,
                paidAt = null
            )
        }
    }
    
    override fun checkPaymentStatus(transactionId: String): PaymentStatusResponse {
        logger.info("결제 상태 확인: transactionId={}", transactionId)
        
        val paymentInfo = paymentStore[transactionId]
        
        return if (paymentInfo != null) {
            PaymentStatusResponse(
                transactionId = transactionId,
                status = paymentInfo.status,
                paidAt = paymentInfo.paidAt?.format(dateTimeFormatter),
                amount = paymentInfo.amount,
                errorMessage = null
            )
        } else {
            PaymentStatusResponse(
                transactionId = transactionId,
                status = "NOT_FOUND",
                paidAt = null,
                amount = BigDecimal.ZERO,
                errorMessage = "결제 내역을 찾을 수 없습니다."
            )
        }
    }
    
    override fun cancelPayment(cancelRequest: PaymentCancelRequest): PaymentCancelResponse {
        logger.info("결제 취소 요청: transactionId={}, reason={}", 
            cancelRequest.transactionId, cancelRequest.reason)
        
        val paymentInfo = paymentStore[cancelRequest.transactionId]
        
        if (paymentInfo == null) {
            logger.warn("결제 취소 실패: 결제 내역을 찾을 수 없음")
            return PaymentCancelResponse(
                success = false,
                transactionId = cancelRequest.transactionId,
                refundId = null,
                refundedAt = null,
                errorMessage = "결제 내역을 찾을 수 없습니다."
            )
        }
        
        // 이미 취소된 경우
        if (paymentInfo.status == "REFUNDED") {
            logger.warn("결제 취소 실패: 이미 취소된 결제")
            return PaymentCancelResponse(
                success = false,
                transactionId = cancelRequest.transactionId,
                refundId = paymentInfo.refundId,
                refundedAt = paymentInfo.refundedAt?.format(dateTimeFormatter),
                errorMessage = "이미 취소된 결제입니다."
            )
        }
        
        // 취소 처리
        val refundId = UUID.randomUUID().toString()
        val refundedAt = LocalDateTime.now()
        
        paymentInfo.status = "REFUNDED"
        paymentInfo.refundId = refundId
        paymentInfo.refundedAt = refundedAt
        paymentInfo.refundReason = cancelRequest.reason
        
        logger.info("결제 취소 성공: transactionId={}, refundId={}", cancelRequest.transactionId, refundId)
        
        return PaymentCancelResponse(
            success = true,
            transactionId = cancelRequest.transactionId,
            refundId = refundId,
            refundedAt = refundedAt.format(dateTimeFormatter),
            errorMessage = null
        )
    }
    
    /**
     * 테스트용으로 저장된 모든 결제 정보 조회
     */
    fun getAllPayments(): List<DummyPaymentInfo> {
        return paymentStore.values.toList()
    }
    
    /**
     * 테스트용으로 저장된 결제 정보 초기화
     */
    fun clearAllPayments() {
        paymentStore.clear()
    }
}

/**
 * 더미 결제 정보 클래스
 */
data class DummyPaymentInfo(
    val transactionId: String,
    val reservationId: Long,
    val memberId: Long,
    val amount: BigDecimal,
    val paymentMethod: String,
    var status: String,
    val paidAt: LocalDateTime?,
    val description: String,
    var refundId: String? = null,
    var refundedAt: LocalDateTime? = null,
    var refundReason: String? = null
) 