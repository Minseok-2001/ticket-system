package ticket.be.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ticket.be.domain.Ticket
import ticket.be.domain.TicketStatus
import jakarta.persistence.LockModeType
import java.util.Optional

@Repository
interface TicketRepository : JpaRepository<Ticket, Long> {
    
    // 명령(Command) 용 쓰기 쿼리 - 락 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Optional<Ticket>
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'AVAILABLE' ORDER BY t.id ASC LIMIT 1")
    fun findFirstAvailableTicketForEvent(@Param("eventId") eventId: Long): Optional<Ticket>
    
    // 조회(Query) 용 읽기 쿼리 - @Transactional(readOnly = true)와 함께 사용
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId")
    fun findAllByEventId(@Param("eventId") eventId: Long): List<Ticket>
    
    @Query("SELECT t FROM Ticket t WHERE t.reservedByMember.id = :memberId")
    fun findAllByReservedByMemberId(@Param("memberId") memberId: Long): List<Ticket>
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status = :status")
    fun countByEventIdAndStatus(@Param("eventId") eventId: Long, @Param("status") status: TicketStatus): Long
} 