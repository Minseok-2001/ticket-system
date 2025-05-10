package ticket.be.dto

data class ConfirmTicketCommand(
    val userId: Long,
    val ticketId: Long
)
