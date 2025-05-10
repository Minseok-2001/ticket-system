package ticket.be.service.payment

import java.math.BigDecimal

/**
 * 결제 게이트웨이 인터페이스
 * 결제 시스템과의 통신을 추상화하는 인터페이스
 */
interface PaymentGateway {
    
    /**
     * 결제 요청 처리
     * 
     * @param paymentRequest 결제 요청 정보
     * @return 결제 응답 정보
     */
    fun processPayment(paymentRequest: PaymentRequest): PaymentResponse
    
    /**
     * 결제 상태 확인
     * 
     * @param transactionId 결제 트랜잭션 ID
     * @return 결제 상태 정보
     */
    fun checkPaymentStatus(transactionId: String): PaymentStatusResponse
    
    /**
     * 결제 취소 요청
     * 
     * @param cancelRequest 결제 취소 요청 정보
     * @return 결제 취소 응답 정보
     */
    fun cancelPayment(cancelRequest: PaymentCancelRequest): PaymentCancelResponse
}

/**
 * 결제 요청 정보
 */
data class PaymentRequest(
    val reservationId: Long,
    val memberId: Long,
    val amount: BigDecimal,
    val paymentMethod: String,
    val description: String
)

/**
 * 결제 응답 정보
 */
data class PaymentResponse(
    val success: Boolean,
    val transactionId: String?,
    val errorMessage: String?,
    val amount: BigDecimal,
    val paymentMethod: String,
    val paidAt: String?
)

/**
 * 결제 상태 응답 정보
 */
data class PaymentStatusResponse(
    val transactionId: String,
    val status: String,
    val paidAt: String?,
    val amount: BigDecimal,
    val errorMessage: String?
)

/**
 * 결제 취소 요청 정보
 */
data class PaymentCancelRequest(
    val transactionId: String,
    val reason: String?
)

/**
 * 결제 취소 응답 정보
 */
data class PaymentCancelResponse(
    val success: Boolean,
    val transactionId: String,
    val refundId: String?,
    val refundedAt: String?,
    val errorMessage: String?
) 