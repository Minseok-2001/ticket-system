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
    @JoinColumn(
        name = "member_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    val member: Member,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "event_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    val event: Event,
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "ticket_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    val ticket: Ticket? = null,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val totalAmount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReservationStatus = ReservationStatus.PENDING,
    
    @Column(length = 255)
    var paymentId: String? = null,
    
    @Column(length = 50)
    val paymentMethod: String? = null,
    
    @Column
    var confirmedAt: LocalDateTime? = null,
    
    @Column
    var cancelledAt: LocalDateTime? = null,
    
    @Column(length = 500)
    var cancelReason: String? = null
) : BaseTimeEntity() {
    
    fun confirm() {
        if (status != ReservationStatus.PENDING) {
            throw IllegalStateException("확정할 수 없는 상태입니다: $status")
        }
        status = ReservationStatus.CONFIRMED
        confirmedAt = LocalDateTime.now()
    }
    
    fun cancel(reason: String? = null) {
        if (status != ReservationStatus.PENDING && status != ReservationStatus.CONFIRMED) {
            throw IllegalStateException("취소할 수 없는 상태입니다: $status")
        }
        status = ReservationStatus.CANCELLED
        cancelledAt = LocalDateTime.now()
        cancelReason = reason
    }
    
    fun complete(paymentId: String) {
        if (status != ReservationStatus.CONFIRMED) {
            throw IllegalStateException("완료할 수 없는 상태입니다: $status")
        }
        status = ReservationStatus.COMPLETED
        this.paymentId = paymentId
    }
    
    fun refund(reason: String? = null) {
        if (status != ReservationStatus.COMPLETED) {
            throw IllegalStateException("환불할 수 없는 상태입니다: $status")
        }
        status = ReservationStatus.REFUNDED
        cancelledAt = LocalDateTime.now()
        cancelReason = reason
    }
}

enum class ReservationStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED, REFUNDED
} 