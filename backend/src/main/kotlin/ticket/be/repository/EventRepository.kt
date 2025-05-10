package ticket.be.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ticket.be.domain.Event
import ticket.be.domain.EventStatus
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {

    fun findByStatus(status: EventStatus, pageable: Pageable): Page<Event>
    
    @Query("SELECT e FROM Event e WHERE e.salesStartDate <= :now AND e.salesEndDate >= :now")
    fun findActiveEvents(now: LocalDateTime, pageable: Pageable): Page<Event>
    
    @Query("SELECT e FROM Event e WHERE e.eventDate >= :now")
    fun findUpcomingEvents(now: LocalDateTime, pageable: Pageable): Page<Event>
    
    @Query("SELECT e FROM Event e WHERE e.name LIKE CONCAT('%', :keyword, '%') OR e.content LIKE CONCAT('%', :keyword, '%')")
    fun searchEvents(keyword: String, pageable: Pageable): Page<Event>
    
    // 대기열 관련 메서드
    fun findByIsQueueActiveTrue(): List<Event>
    
    @Query("SELECT e FROM Event e WHERE e.isQueueActive = true AND e.salesStartDate <= :now AND e.salesEndDate >= :now")
    fun findActiveEventsWithQueue(now: LocalDateTime, pageable: Pageable): Page<Event>
} 