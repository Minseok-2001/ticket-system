package ticket.be.dto
data class TicketSummaryDto(
    val id: Long,
    val eventId: Long,
    val status: String
)