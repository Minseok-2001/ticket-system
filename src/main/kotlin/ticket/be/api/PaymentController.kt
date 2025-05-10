package ticket.be.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ticket.be.config.CurrentMember
import ticket.be.service.AuthService
import ticket.be.service.PaymentResult
import ticket.be.service.PaymentService
import ticket.be.service.TicketQueryService
import java.math.BigDecimal

@RestController
@RequestMapping("/api/payments")
@Tag(name = "결제", description = "결제 관련 API")
class PaymentController(
    private val paymentService: PaymentService,
    private val ticketQueryService: TicketQueryService,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(PaymentController::class.java)

    @PostMapping("/process")
    @Operation(summary = "결제 처리", description = "예약에 대한 결제를 처리합니다.")
    @PreAuthorize("isAuthenticated()")
    fun processPayment(
        @RequestBody request: ProcessPaymentRequest,
        @CurrentMember email: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("결제 처리 요청: reservationId={}, email={}", request.reservationId, email)
        
        val member = authService.getMemberByEmail(email)
        val reservation = ticketQueryService.getReservationById(request.reservationId)
        
        // 예약자 본인 확인
        if (reservation.memberId != member.id) {
            return ResponseEntity.status(403).body(mapOf<String, Any>(
                "success" to false,
                "message" to "예약자 본인만 결제할 수 있습니다."
            ))
        }
        
        val result = paymentService.processPayment(request.reservationId)
        
        return if (result.success) {
            ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "message" to "결제가 성공적으로 처리되었습니다.",
                "transactionId" to (result.transactionId ?: ""),
                "reservationId" to result.reservationId
            ))
        } else {
            ResponseEntity.badRequest().body(mapOf<String, Any>(
                "success" to false,
                "message" to (result.errorMessage ?: "결제 처리 중 오류가 발생했습니다."),
                "reservationId" to result.reservationId
            ))
        }
    }
    
    @PostMapping("/cancel")
    @Operation(summary = "결제 취소", description = "기존 결제를 취소합니다.")
    @PreAuthorize("isAuthenticated()")
    fun cancelPayment(
        @RequestBody request: CancelPaymentRequest,
        @CurrentMember email: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("결제 취소 요청: reservationId={}, email={}", request.reservationId, email)
        
        val member = authService.getMemberByEmail(email)
        val reservation = ticketQueryService.getReservationById(request.reservationId)
        
        // 예약자 본인 확인
        if (reservation.memberId != member.id) {
            return ResponseEntity.status(403).body(mapOf<String, Any>(
                "success" to false,
                "message" to "예약자 본인만 결제를 취소할 수 있습니다."
            ))
        }
        
        val result = paymentService.cancelPayment(request.reservationId, request.reason)
        
        return if (result.success) {
            ResponseEntity.ok(mapOf<String, Any>(
                "success" to true,
                "message" to "결제가 성공적으로 취소되었습니다.",
                "refundId" to (result.refundId ?: ""),
                "reservationId" to result.reservationId
            ))
        } else {
            ResponseEntity.badRequest().body(mapOf<String, Any>(
                "success" to false,
                "message" to (result.errorMessage ?: "결제 취소 중 오류가 발생했습니다."),
                "reservationId" to result.reservationId
            ))
        }
    }
    
    @GetMapping("/status/{transactionId}")
    @Operation(summary = "결제 상태 조회", description = "결제 상태를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getPaymentStatus(
        @PathVariable transactionId: String
    ): ResponseEntity<Map<String, Any>> {
        logger.info("결제 상태 조회 요청: transactionId={}", transactionId)
        
        val result = paymentService.getPaymentStatus(transactionId)
        
        return ResponseEntity.ok(mapOf<String, Any>(
            "transactionId" to (result.transactionId ?: ""),
            "status" to (result.status ?: ""),
            "paidAt" to (result.paidAt ?: ""),
            "amount" to (result.amount ?: BigDecimal.ZERO),
            "errorMessage" to (result.errorMessage ?: "")
        ))
    }
}

/**
 * 결제 처리 요청 DTO
 */
data class ProcessPaymentRequest(
    val reservationId: Long
)

/**
 * 결제 취소 요청 DTO
 */
data class CancelPaymentRequest(
    val reservationId: Long,
    val reason: String? = null
) 