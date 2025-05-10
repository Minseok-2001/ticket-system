package ticket.be.dto

data class ReserveTicketCommand(
    val userId: Long,
    val eventId: Long,
    val ticketCount: Int
)