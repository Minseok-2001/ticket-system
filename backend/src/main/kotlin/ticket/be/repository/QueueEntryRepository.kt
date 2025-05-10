package ticket.be.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ticket.be.domain.QueueEntry
import ticket.be.domain.QueueStatus

@Repository
interface QueueEntryRepository : JpaRepository<QueueEntry, Long> {
    
    fun findByEventIdAndMemberId(eventId: Long, memberId: Long): QueueEntry?
    
    fun findByEventIdAndStatus(eventId: Long, status: QueueStatus, pageable: Pageable): Page<QueueEntry>
    
    @Query("SELECT q FROM QueueEntry q WHERE q.event.id = :eventId AND q.status = :status ORDER BY q.queuePosition ASC")
    fun findByEventIdAndStatusOrderByPositionAsc(eventId: Long, status: QueueStatus, pageable: Pageable): Page<QueueEntry>
    
    @Query("SELECT COUNT(q) FROM QueueEntry q WHERE q.event.id = :eventId AND q.status = :status")
    fun countByEventIdAndStatus(eventId: Long, status: QueueStatus): Long
    
    @Query("SELECT q FROM QueueEntry q WHERE q.status = :status AND q.expiresAt < CURRENT_TIMESTAMP")
    fun findExpiredEntries(status: QueueStatus, pageable: Pageable): Page<QueueEntry>
} 