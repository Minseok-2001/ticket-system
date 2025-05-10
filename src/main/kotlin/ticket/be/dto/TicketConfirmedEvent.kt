package ticket.be.dto

import java.time.LocalDateTime

data class TicketConfirmedEvent(
    val ticketId: Long,
    val userId: Long,
    val confirmedAt: LocalDateTime
)