package ticket.be.dto

data class ReserveTicketCommand(
    val memberId: Long,
    val eventId: Long,
    val ticketCount: Int
)