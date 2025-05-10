package ticket.be.dto

import java.time.LocalDateTime

data class ReservationStatusDto(
    val ticketId: Long,
    val userId: Long,
    val status: String,
    val reservedAt: LocalDateTime?
)
