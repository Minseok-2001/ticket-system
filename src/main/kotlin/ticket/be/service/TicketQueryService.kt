package ticket.be.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.Ticket
import ticket.be.domain.TicketStatus
import ticket.be.dto.ReservationStatusDto
import ticket.be.dto.TicketDto
import ticket.be.dto.TicketSummaryDto
import ticket.be.repository.TicketQueryRepository
import ticket.be.repository.TicketRepository

@Service
class TicketQueryService(
    private val ticketRepository: TicketRepository,
    private val ticketQueryRepository: TicketQueryRepository
) {
    
    @Transactional(readOnly = true)
    fun getTicketsByEventId(eventId: Long): List<TicketSummaryDto> {
        return ticketRepository.findAllByEventId(eventId)
            .map { ticket -> mapToTicketSummaryDto(ticket) }
    }
    
    @Transactional(readOnly = true)
    fun getTicketsByMemberId(memberId: Long): List<TicketSummaryDto> {
        return ticketRepository.findAllByReservedByMemberId(memberId)
            .map { ticket -> mapToTicketSummaryDto(ticket) }
    }
    
    @Transactional(readOnly = true)
    fun getTicketStatus(ticketId: Long): ReservationStatusDto {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { IllegalArgumentException("Ticket not found: $ticketId") }
        
        return ReservationStatusDto(
            ticketId = ticket.id,
            memberId = ticket.reservedByMember?.id ?: 0,
            status = ticket.status.name,
            reservedAt = ticket.reservedAt
        )
    }

    @Transactional(readOnly = true)
    fun getDetailedTicket(ticketId: Long): TicketDto {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { IllegalArgumentException("Ticket not found: $ticketId") }
        
        return mapToTicketDto(ticket)
    }
    
    @Transactional(readOnly = true)
    fun getAvailableTicketsCount(eventId: Long): Int {
        return ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.AVAILABLE).toInt()
    }
    
    @Transactional(readOnly = true)
    fun getTicketsCountByStatus(eventId: Long, status: TicketStatus): Int {
        return ticketRepository.countByEventIdAndStatus(eventId, status).toInt()
    }
    
    private fun mapToTicketSummaryDto(ticket: Ticket): TicketSummaryDto {
        return TicketSummaryDto(
            id = ticket.id,
            eventId = ticket.event.id,
            status = ticket.status.name
        )
    }
    
    private fun mapToTicketDto(ticket: Ticket): TicketDto {
        return TicketDto(
            id = ticket.id,
            eventId = ticket.event.id,
            ticketTypeId = ticket.ticketType.id,
            seatNumber = ticket.seatNumber,
            price = ticket.price,
            status = ticket.status,
            reservedByMemberId = ticket.reservedByMember?.id,
            reservedAt = ticket.reservedAt,
            createdAt = ticket.createdAt,
            updatedAt = ticket.updatedAt
        )
    }
} 