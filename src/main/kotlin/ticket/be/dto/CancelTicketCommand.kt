package ticket.be.dto

data class CancelTicketCommand(
    val memberId: Long,
    val ticketId: Long,
    val reason: String? = null
) 