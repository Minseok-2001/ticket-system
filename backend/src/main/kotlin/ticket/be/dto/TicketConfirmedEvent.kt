package ticket.be.dto

import java.time.LocalDateTime

data class TicketConfirmedEvent(
    val ticketId: Long,
    val memberId: Long,
    val confirmedAt: LocalDateTime
)