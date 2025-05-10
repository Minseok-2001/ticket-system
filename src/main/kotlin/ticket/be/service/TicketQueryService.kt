package ticket.be.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.Reservation
import ticket.be.domain.ReservationStatus
import ticket.be.domain.Ticket
import ticket.be.domain.TicketStatus
import ticket.be.dto.ReservationResponse
import ticket.be.dto.ReservationStatusResponse
import ticket.be.dto.TicketResponse
import ticket.be.repository.ReservationRepository
import ticket.be.repository.TicketRepository
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class TicketQueryService(
    private val ticketRepository: TicketRepository,
    private val reservationRepository: ReservationRepository
) {

    /**
     * 회원의 티켓 목록 조회
     */
    fun getTicketsByMember(memberId: Long, pageable: Pageable): Page<TicketResponse> {
        return ticketRepository.findByMemberId(memberId, pageable)
            .map { ticket -> convertToTicketResponse(ticket) }
    }
    
    /**
     * 이벤트의 티켓 상태별 조회
     */
    fun getTicketsByEventAndStatus(eventId: Long, status: TicketStatus, pageable: Pageable): Page<TicketResponse> {
        return ticketRepository.findByEventIdAndStatus(eventId, status, pageable)
            .map { ticket -> convertToTicketResponse(ticket) }
    }
    
    /**
     * 티켓 상세 조회
     */
    fun getTicketById(ticketId: Long): TicketResponse {
        val ticket = ticketRepository.findById(ticketId)
            .orElseThrow { IllegalArgumentException("티켓을 찾을 수 없습니다: id=$ticketId") }
        
        return convertToTicketResponse(ticket)
    }
    
    /**
     * 회원의 예매 내역 조회
     */
    fun getReservationsByMember(memberId: Long, pageable: Pageable): Page<ReservationResponse> {
        return reservationRepository.findByMemberId(memberId, pageable)
            .map { reservation -> convertToReservationResponse(reservation) }
    }
    
    /**
     * 회원의 이메일로 최근 예매 내역 조회
     */
    fun getRecentReservationsByEmail(email: String, pageable: Pageable): Page<ReservationResponse> {
        return reservationRepository.findRecentByMemberEmail(email, pageable)
            .map { reservation -> convertToReservationResponse(reservation) }
    }
    
    /**
     * 예매 상세 조회
     */
    fun getReservationById(reservationId: Long): ReservationResponse {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalArgumentException("예매 내역을 찾을 수 없습니다: id=$reservationId") }
        
        return convertToReservationResponse(reservation)
    }
    
    /**
     * 예매 상태 조회
     */
    fun getReservationStatus(reservationId: Long): ReservationStatusResponse {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { IllegalArgumentException("예매 내역을 찾을 수 없습니다: id=$reservationId") }
        
        return ReservationStatusResponse(
            reservationId = reservation.id,
            status = reservation.status.name,
            message = getStatusMessage(reservation.status)
        )
    }
    
    /**
     * 만료된 예매 내역 조회
     */
    fun getExpiredReservations(expiryTime: LocalDateTime, pageable: Pageable): Page<ReservationResponse> {
        return reservationRepository.findExpiredReservations(ReservationStatus.PENDING, expiryTime, pageable)
            .map { reservation -> convertToReservationResponse(reservation) }
    }
    
    /**
     * Ticket 엔티티를 TicketResponse DTO로 변환
     */
    private fun convertToTicketResponse(ticket: Ticket): TicketResponse {
        return TicketResponse(
            id = ticket.id,
            eventId = ticket.event.id,
            eventName = ticket.event.name,
            ticketTypeId = ticket.ticketType.id,
            ticketTypeName = ticket.ticketType.name,
            seatNumber = ticket.seatNumber,
            price = ticket.price,
            status = ticket.status.name,
            reservedAt = ticket.reservedAt
        )
    }
    
    /**
     * Reservation 엔티티를 ReservationResponse DTO로 변환
     */
    private fun convertToReservationResponse(reservation: Reservation): ReservationResponse {
        return ReservationResponse(
            id = reservation.id,
            eventId = reservation.event.id,
            eventName = reservation.event.name,
            memberId = reservation.member.id,
            memberName = reservation.member.name,
            ticketId = reservation.ticket?.id ?: 0,
            totalAmount = reservation.totalAmount,
            status = reservation.status.name,
            createdAt = reservation.createdAt,
            confirmedAt = reservation.confirmedAt,
            cancelledAt = reservation.cancelledAt
        )
    }
    
    /**
     * 예매 상태에 따른 메시지 반환
     */
    private fun getStatusMessage(status: ReservationStatus): String {
        return when (status) {
            ReservationStatus.PENDING -> "결제 대기 중입니다."
            ReservationStatus.CONFIRMED -> "예매가 확정되었습니다."
            ReservationStatus.CANCELLED -> "예매가 취소되었습니다."
            ReservationStatus.COMPLETED -> "예매가 완료되었습니다."
            ReservationStatus.REFUNDED -> "환불이 완료되었습니다."
        }
    }
} 