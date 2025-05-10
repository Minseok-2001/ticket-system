package ticket.be.dto

import java.time.LocalDateTime

data class TicketCancelledEvent(
    val ticketId: Long,
    val userId: Long,
    val cancelledAt: LocalDateTime
) 