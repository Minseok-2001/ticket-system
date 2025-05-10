package ticket.be.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ticket.be.domain.TicketType

@Repository
interface TicketTypeRepository : JpaRepository<TicketType, Long> {
    
    fun findByEventId(eventId: Long, pageable: Pageable): Page<TicketType>
    
    @Query("SELECT tt FROM TicketType tt WHERE tt.event.id = :eventId AND tt.availableQuantity > 0")
    fun findAvailableByEventId(eventId: Long, pageable: Pageable): Page<TicketType>
    
    @Query("SELECT SUM(tt.availableQuantity) FROM TicketType tt WHERE tt.event.id = :eventId")
    fun sumAvailableQuantityByEventId(eventId: Long): Int?
    
    @Query("SELECT tt FROM TicketType tt WHERE tt.event.id = :eventId AND tt.availableQuantity > 0 AND tt.price > :minPrice AND tt.price < :maxPrice")
    fun findByEventIdAndPriceRange(eventId: Long, minPrice: Double, maxPrice: Double, pageable: Pageable): Page<TicketType>
} 