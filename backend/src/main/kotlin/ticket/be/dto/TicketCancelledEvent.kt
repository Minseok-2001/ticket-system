package ticket.be.dto

import java.time.LocalDateTime

data class TicketCancelledEvent(
    val ticketId: Long,
    val memberId: Long,
    val cancelledAt: LocalDateTime
) 