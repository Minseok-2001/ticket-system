package ticket.be.dto

import java.time.LocalDateTime

data class ReservationStatusDto(
    val ticketId: Long,
    val memberId: Long,
    val status: String,
    val reservedAt: LocalDateTime?
)
