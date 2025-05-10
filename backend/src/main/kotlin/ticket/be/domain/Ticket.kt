package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "ticket")
class Ticket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val event: Event,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val ticketType: TicketType,
    
    @Column(nullable = false)
    var seatNumber: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TicketStatus = TicketStatus.AVAILABLE,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserved_by_member_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    var reservedByMember: Member? = null,
    
    @Column
    var reservedAt: LocalDateTime? = null,
    
    @OneToOne(mappedBy = "ticket", cascade = [CascadeType.ALL], orphanRemoval = true)
    var reservation: Reservation? = null
) : BaseTimeEntity() {
    
    fun reserve(member: Member): Boolean {
        if (status != TicketStatus.AVAILABLE) {
            return false
        }
        status = TicketStatus.RESERVED
        reservedByMember = member
        reservedAt = LocalDateTime.now()
        return true
    }
    
    fun confirm(): Boolean {
        if (status != TicketStatus.RESERVED) {
            return false
        }
        status = TicketStatus.SOLD
        return true
    }
    
    fun cancel(): Boolean {
        if (status != TicketStatus.RESERVED) {
            return false
        }
        status = TicketStatus.AVAILABLE
        reservedByMember = null
        reservedAt = null
        return true
    }
}

enum class TicketStatus {
    AVAILABLE, RESERVED, SOLD, CANCELLED
} 