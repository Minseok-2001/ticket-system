package ticket.be.dto

import java.time.LocalDateTime

data class TicketReservedEvent(
    val ticketId: Long,
    val userId: Long,
    val eventId: Long,
    val reservedAt: LocalDateTime
)