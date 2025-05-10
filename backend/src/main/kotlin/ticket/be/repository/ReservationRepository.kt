package ticket.be.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ticket.be.domain.Reservation
import ticket.be.domain.ReservationStatus
import java.time.LocalDateTime

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId")
    fun findByMemberId(memberId: Long, pageable: Pageable): Page<Reservation>
    
    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId AND r.status = :status")
    fun findByMemberIdAndStatus(memberId: Long, status: ReservationStatus, pageable: Pageable): Page<Reservation>
    
    @Query("SELECT r FROM Reservation r WHERE r.event.id = :eventId")
    fun findByEventId(eventId: Long, pageable: Pageable): Page<Reservation>
    
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.createdAt < :expiryTime")
    fun findExpiredReservations(status: ReservationStatus, expiryTime: LocalDateTime, pageable: Pageable): Page<Reservation>
    
    @Query("SELECT r FROM Reservation r WHERE r.member.email = :email ORDER BY r.createdAt DESC")
    fun findRecentByMemberEmail(email: String, pageable: Pageable): Page<Reservation>
} 