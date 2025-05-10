package ticket.be.dto

import ticket.be.domain.TicketStatus
import java.math.BigDecimal
import java.time.LocalDateTime

data class TicketDto(
    val id: Long,
    val eventId: Long,
    val ticketTypeId: Long,
    val seatNumber: String,
    val price: BigDecimal,
    val status: TicketStatus,
    val reservedByMemberId: Long?,
    val reservedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

/**
 * 티켓 예매 명령 DTO
 */
data class ReserveTicketCommand(
    val eventId: Long,
    val ticketTypeId: Long,
    var memberId: Long = 0,
    val quantity: Int = 1,
    val paymentMethod: String? = null
)

/**
 * 티켓 Kafka 이벤트 DTO
 */
data class TicketEvent(
    val eventType: String = "RESERVE_TICKET",
    val timestamp: Long = System.currentTimeMillis(),
    val reservationId: Long? = null,
    val eventId: Long,
    val ticketTypeId: Long,
    val memberId: Long,
    val quantity: Int = 1,
    val paymentMethod: String? = null,
    val status: String = "PENDING"
)

/**
 * 티켓 정보 응답 DTO
 */
data class TicketResponse(
    val id: Long,
    val eventId: Long,
    val eventName: String,
    val ticketTypeId: Long,
    val ticketTypeName: String,
    val seatNumber: String,
    val price: BigDecimal,
    val status: String,
    val reservedAt: LocalDateTime?
)

/**
 * 예매 정보 응답 DTO
 */
data class ReservationResponse(
    val id: Long,
    val eventId: Long,
    val eventName: String,
    val memberId: Long,
    val memberName: String,
    val ticketId: Long,
    val totalAmount: BigDecimal,
    val status: String,
    val createdAt: LocalDateTime,
    val confirmedAt: LocalDateTime?,
    val cancelledAt: LocalDateTime?
)

/**
 * 예매 상태 응답 DTO
 */
data class ReservationStatusResponse(
    val reservationId: Long,
    val status: String,
    val message: String
)

/**
 * 티켓 취소 명령 DTO
 */
data class CancelTicketCommand(
    val reservationId: Long,
    val reason: String? = null
)





