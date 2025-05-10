package ticket.be.dto

data class ConfirmTicketCommand(
    val memberId: Long,
    val ticketId: Long
)
