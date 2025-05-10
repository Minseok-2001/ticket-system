package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "reservation")
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val member: Member,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val event: Event,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val ticket: Ticket,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReservationStatus = ReservationStatus.PENDING,
    
    @Column(length = 255)
    var paymentId: String? = null,
    
    @Column
    var confirmedAt: LocalDateTime? = null,
    
    @Column
    var cancelledAt: LocalDateTime? = null,
    
    @Column(length = 500)
    var cancelReason: String? = null
) : BaseTimeEntity() {

    fun confirm(paymentId: String) {
        this.status = ReservationStatus.CONFIRMED
        this.paymentId = paymentId
        this.confirmedAt = LocalDateTime.now()
    }
    
    fun cancel(reason: String?) {
        this.status = ReservationStatus.CANCELLED
        this.cancelReason = reason
        this.cancelledAt = LocalDateTime.now()
    }
}

enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED
} 