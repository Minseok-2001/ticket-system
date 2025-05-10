package ticket.be.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ticket.be.dto.ReservationStatusDto
import ticket.be.dto.TicketSummaryDto
import java.sql.ResultSet

@Repository
class TicketQueryRepository(private val jdbcTemplate: JdbcTemplate) {

    @Transactional(readOnly = true)
    fun findTicketSummariesByEventId(eventId: Long): List<TicketSummaryDto> {
        val sql = """
            SELECT id, event_id, status 
            FROM tickets 
            WHERE event_id = ?
        """.trimIndent()

        return jdbcTemplate.query(sql, TicketSummaryDtoRowMapper(), eventId)
    }

    @Transactional(readOnly = true)
    fun findReservationStatus(userId: Long, ticketId: Long): ReservationStatusDto? {
        val sql = """
            SELECT t.id, t.reserved_by_user_id, t.status, t.updated_at 
            FROM tickets t
            WHERE t.id = ? AND t.reserved_by_user_id = ?
        """.trimIndent()

        val results = jdbcTemplate.query(sql, ReservationStatusDtoRowMapper(), ticketId, userId)
        return if (results.isEmpty()) null else results[0]
    }

    @Transactional(readOnly = true)
    fun countAvailableTickets(eventId: Long): Int {
        val sql = "SELECT COUNT(*) FROM tickets WHERE event_id = ? AND status = 'AVAILABLE'"
        return jdbcTemplate.queryForObject(sql, Int::class.java, eventId) ?: 0
    }

    private class TicketSummaryDtoRowMapper : RowMapper<TicketSummaryDto> {
        override fun mapRow(rs: ResultSet, rowNum: Int): TicketSummaryDto {
            return TicketSummaryDto(
                id = rs.getLong("id"),
                eventId = rs.getLong("event_id"),
                status = rs.getString("status")
            )
        }
    }

    private class ReservationStatusDtoRowMapper : RowMapper<ReservationStatusDto> {
        override fun mapRow(rs: ResultSet, rowNum: Int): ReservationStatusDto {
            return ReservationStatusDto(
                ticketId = rs.getLong("id"),
                memberId = rs.getLong("reserved_by_user_id"),
                status = rs.getString("status"),
                reservedAt = rs.getTimestamp("updated_at")?.toLocalDateTime()
            )
        }
    }
} 