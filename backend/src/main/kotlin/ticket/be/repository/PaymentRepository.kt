package ticket.be.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ticket.be.domain.Payment
import ticket.be.domain.PaymentStatus
import java.time.LocalDateTime

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {

    /**
     * 회원의 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.member.id = :memberId ORDER BY p.createdAt DESC")
    fun findByMemberId(memberId: Long, pageable: Pageable): Page<Payment>
    
    /**
     * 예약 ID로 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.reservation.id = :reservationId")
    fun findByReservationId(reservationId: Long): List<Payment>
    
    /**
     * 예약 ID와 트랜잭션 ID로 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.reservation.id = :reservationId AND p.transactionId = :transactionId")
    fun findByReservationIdAndTransactionId(reservationId: Long, transactionId: String): Payment?
    
    /**
     * 특정 상태의 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    fun findByStatus(status: PaymentStatus, pageable: Pageable): Page<Payment>
    
    /**
     * 특정 기간의 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime, pageable: Pageable): Page<Payment>
    
    /**
     * 트랜잭션 ID로 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
    fun findByTransactionId(transactionId: String): Payment?
    
    /**
     * 특정 이벤트에 대한 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.reservation.event.id = :eventId")
    fun findByEventId(eventId: Long, pageable: Pageable): Page<Payment>
} 