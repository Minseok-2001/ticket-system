package ticket.be.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import ticket.be.BaseTest
import ticket.be.domain.*
import ticket.be.repository.MemberRepository
import ticket.be.repository.PaymentRepository
import ticket.be.repository.ReservationRepository
import ticket.be.service.payment.DummyPaymentGateway
import ticket.be.service.payment.PaymentRequest
import ticket.be.service.payment.PaymentResponse
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest : BaseTest() {

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Mock
    private lateinit var reservationRepository: ReservationRepository

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var paymentGateway: DummyPaymentGateway

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @Mock
    private lateinit var notificationCommandService: NotificationCommandService

    @InjectMocks
    private lateinit var paymentService: PaymentService

    private lateinit var testMember: Member
    private lateinit var testEvent: Event
    private lateinit var testReservation: Reservation
    private lateinit var testPayment: Payment

    @BeforeEach
    fun setUp() {
        testMember = testDataFactory.createMember()
        testEvent = testDataFactory.createEvent()
        testReservation = testDataFactory.createReservation(
            member = testMember,
            event = testEvent,
            status = ReservationStatus.PENDING
        )
        testPayment = testDataFactory.createPayment(
            member = testMember,
            reservation = testReservation,
            status = PaymentStatus.COMPLETED
        )
    }

    @Test
    @DisplayName("결제 처리 성공 테스트")
    fun processPaymentSuccess() {
        // given
        val reservationId = testReservation.id
        val transactionId = "test-transaction-id"
        val amount = testReservation.totalAmount
        val paymentMethod = testReservation.paymentMethod!!

        `when`(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation))
        `when`(paymentGateway.processPayment(any())).thenReturn(
            PaymentResponse(
                success = true,
                transactionId = transactionId,
                errorMessage = null,
                amount = amount,
                paymentMethod = paymentMethod,
                paidAt = LocalDateTime.now().toString()
            )
        )
        `when`(paymentRepository.save(any())).thenReturn(testPayment)
        `when`(objectMapper.writeValueAsString(any())).thenReturn("{}")

        // when
        val result = paymentService.processPayment(reservationId)

        // then
        verify(reservationRepository).findById(reservationId)
        verify(paymentGateway).processPayment(any())
        verify(paymentRepository).save(any())
        verify(kafkaTemplate).send(eq("payment-events"), anyString())
        verify(notificationCommandService).sendNotification(
            eq(testMember.id),
            eq("PAYMENT_COMPLETED"),
            anyString(),
            anyString(),
            anyString()
        )
        
        assert(result.success)
        assert(result.reservationId == reservationId)
        assert(result.transactionId == transactionId)
    }

    @Test
    @DisplayName("결제 처리 실패 테스트 - 게이트웨이 오류")
    fun processPaymentFailWhenGatewayError() {
        // given
        val reservationId = testReservation.id
        val errorMessage = "결제 처리 중 오류가 발생했습니다."

        `when`(reservationRepository.findById(reservationId)).thenReturn(Optional.of(testReservation))
        `when`(paymentGateway.processPayment(any())).thenReturn(
            PaymentResponse(
                success = false,
                transactionId = null,
                errorMessage = errorMessage,
                amount = testReservation.totalAmount,
                paymentMethod = testReservation.paymentMethod!!,
                paidAt = null
            )
        )
        `when`(paymentRepository.save(any())).thenReturn(testPayment)

        // when
        val result = paymentService.processPayment(reservationId)

        // then
        verify(reservationRepository).findById(reservationId)
        verify(paymentGateway).processPayment(any())
        verify(paymentRepository).save(any()) // 실패 정보도 저장
        verify(notificationCommandService).sendNotification(
            eq(testMember.id),
            eq("PAYMENT_FAILED"),
            anyString(),
            anyString(),
            anyString()
        )
        
        assert(!result.success)
        assert(result.reservationId == reservationId)
        assert(result.transactionId == null)
        assert(result.errorMessage == errorMessage)
    }

    @Test
    @DisplayName("결제 처리 실패 테스트 - 이미 완료된 예약")
    fun processPaymentFailWhenAlreadyPaid() {
        // given
        val reservationId = testReservation.id
        val paidReservation = testDataFactory.createReservation(
            member = testMember,
            event = testEvent,
            status = ReservationStatus.COMPLETED,
            paymentId = "existing-payment-id"
        )

        `when`(reservationRepository.findById(reservationId)).thenReturn(Optional.of(paidReservation))

        // when
        val result = paymentService.processPayment(reservationId)

        // then
        verify(reservationRepository).findById(reservationId)
        verify(paymentGateway, never()).processPayment(any())
        
        assert(!result.success)
        assert(result.reservationId == reservationId)
        assert(result.transactionId == "existing-payment-id")
    }

    @Test
    @DisplayName("결제 취소 성공 테스트")
    fun cancelPaymentSuccess() {
        // given
        val reservationId = testReservation.id
        val transactionId = "test-transaction-id"
        val refundId = "test-refund-id"
        val reason = "고객 요청에 의한 취소"
        
        val completedReservation = testDataFactory.createReservation(
            member = testMember,
            event = testEvent,
            status = ReservationStatus.COMPLETED,
            paymentId = transactionId
        )

        `when`(reservationRepository.findById(reservationId)).thenReturn(Optional.of(completedReservation))
        `when`(paymentRepository.findByReservationIdAndTransactionId(reservationId, transactionId)).thenReturn(testPayment)
        `when`(paymentGateway.cancelPayment(any())).thenReturn(
            ticket.be.service.payment.PaymentCancelResponse(
                success = true,
                transactionId = transactionId,
                refundId = refundId,
                refundedAt = LocalDateTime.now().toString(),
                errorMessage = null
            )
        )
        `when`(objectMapper.writeValueAsString(any())).thenReturn("{}")

        // when
        val result = paymentService.cancelPayment(reservationId, reason)

        // then
        verify(reservationRepository).findById(reservationId)
        verify(paymentGateway).cancelPayment(any())
        verify(paymentRepository).findByReservationIdAndTransactionId(reservationId, transactionId)
        verify(paymentRepository).save(any())
        verify(kafkaTemplate).send(eq("payment-events"), anyString())
        verify(notificationCommandService).sendNotification(
            eq(testMember.id),
            eq("PAYMENT_CANCELLED"),
            anyString(),
            anyString(),
            anyString()
        )
        
        assert(result.success)
        assert(result.reservationId == reservationId)
        assert(result.transactionId == transactionId)
        assert(result.refundId == refundId)
    }

    @Test
    @DisplayName("결제 상태 조회 테스트")
    fun getPaymentStatus() {
        // given
        val transactionId = "test-transaction-id"
        val statusResponse = ticket.be.service.payment.PaymentStatusResponse(
            transactionId = transactionId,
            status = "COMPLETED",
            paidAt = LocalDateTime.now().toString(),
            amount = BigDecimal("50000"),
            errorMessage = null
        )

        `when`(paymentGateway.checkPaymentStatus(transactionId)).thenReturn(statusResponse)

        // when
        val result = paymentService.getPaymentStatus(transactionId)

        // then
        verify(paymentGateway).checkPaymentStatus(transactionId)
        
        assert(result.transactionId == transactionId)
        assert(result.status == "COMPLETED")
        assert(result.amount == BigDecimal("50000"))
    }
} 