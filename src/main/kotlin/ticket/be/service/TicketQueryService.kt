package ticket.be.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.dto.ReservationStatusDto
import ticket.be.dto.TicketSummaryDto
import ticket.be.repository.TicketQueryRepository

@Service
class TicketQueryService(
    private val ticketQueryRepository: TicketQueryRepository
) {
    
    @Transactional(readOnly = true)
    fun getTicketSummaries(eventId: Long): List<TicketSummaryDto> {
        return ticketQueryRepository.findTicketSummariesByEventId(eventId)
    }
    
    @Transactional(readOnly = true)
    fun getReservationStatus(userId: Long, ticketId: Long): ReservationStatusDto? {
        return ticketQueryRepository.findReservationStatus(userId, ticketId)
    }
    
    @Transactional(readOnly = true)
    fun getAvailableTicketCount(eventId: Long): Int {
        return ticketQueryRepository.countAvailableTickets(eventId)
    }
} 