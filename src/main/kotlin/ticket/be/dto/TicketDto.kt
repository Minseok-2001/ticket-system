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





