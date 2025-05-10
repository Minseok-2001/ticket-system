package ticket.be.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "ticket")
class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val eventId: Long,

    @Column(nullable = false)
    var price: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TicketStatus = TicketStatus.AVAILABLE,

    @Column(nullable = true)
    var reservedByUserId: Long? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // 비즈니스 로직
    fun reserve(userId: Long): Boolean {
        if (status != TicketStatus.AVAILABLE) {
            return false
        }
        status = TicketStatus.RESERVED
        reservedByUserId = userId
        updatedAt = LocalDateTime.now()
        return true
    }
    
    fun confirm(): Boolean {
        if (status != TicketStatus.RESERVED) {
            return false
        }
        status = TicketStatus.SOLD
        updatedAt = LocalDateTime.now()
        return true
    }
    
    fun cancel(): Boolean {
        if (status != TicketStatus.RESERVED) {
            return false
        }
        status = TicketStatus.AVAILABLE
        reservedByUserId = null
        updatedAt = LocalDateTime.now()
        return true
    }
}

enum class TicketStatus {
    AVAILABLE, RESERVED, SOLD
} 