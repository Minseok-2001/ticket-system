package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity
import java.time.LocalDateTime

@Entity
@Table(name = "event")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 100)
    var name: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,
    
    @Column(nullable = false, length = 100)
    var venue: String,
    
    @Column(nullable = false)
    var eventDate: LocalDateTime,
    
    @Column(nullable = false)
    var salesStartDate: LocalDateTime,
    
    @Column(nullable = false)
    var salesEndDate: LocalDateTime,
    
    @Column(nullable = false)
    var totalSeats: Int,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: EventStatus = EventStatus.UPCOMING,
    
    @Column(nullable = false)
    var isQueueActive: Boolean = false,
    
    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ticketTypes: MutableSet<TicketType> = mutableSetOf()
) : BaseTimeEntity() {

    fun activateQueue() {
        this.isQueueActive = true
    }
    
    fun deactivateQueue() {
        this.isQueueActive = false
    }
    
    fun updateStatus(status: EventStatus) {
        this.status = status
    }
}

enum class EventStatus {
    UPCOMING, ACTIVE, ENDED, CANCELLED
} 