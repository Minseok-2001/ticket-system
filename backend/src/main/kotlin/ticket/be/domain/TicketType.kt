package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity
import java.math.BigDecimal

@Entity
@Table(name = "ticket_type")
class TicketType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val event: Event,
    
    @Column(nullable = false, length = 50)
    var name: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    
    @Column(nullable = false)
    var quantity: Int,
    
    @Column(nullable = false)
    var availableQuantity: Int,
    
    @Column(length = 255)
    var content: String? = null
) : BaseTimeEntity() {

    fun decreaseAvailableQuantity(count: Int = 1): Boolean {
        if (availableQuantity < count) return false
        availableQuantity -= count
        return true
    }
    
    fun increaseAvailableQuantity(count: Int = 1) {
        availableQuantity += count
        if (availableQuantity > quantity) {
            availableQuantity = quantity
        }
    }
} 