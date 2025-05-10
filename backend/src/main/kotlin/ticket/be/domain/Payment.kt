package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val member: Member,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val reservation: Reservation,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: PaymentStatus = PaymentStatus.PENDING,
    
    @Column(nullable = false, length = 50)
    var paymentMethod: String,
    
    @Column(length = 100)
    var transactionId: String? = null,
    
    @Column
    var paidAt: LocalDateTime? = null,
    
    @Column
    var refundedAt: LocalDateTime? = null,
    
    @Column(length = 500)
    var refundReason: String? = null
) : BaseTimeEntity() {

    fun markAsPaid(transactionId: String) {
        this.status = PaymentStatus.COMPLETED
        this.transactionId = transactionId
        this.paidAt = LocalDateTime.now()
    }
    
    fun markAsRefunded(reason: String) {
        this.status = PaymentStatus.REFUNDED
        this.refundReason = reason
        this.refundedAt = LocalDateTime.now()
    }
    
    fun markAsFailed() {
        this.status = PaymentStatus.FAILED
    }
}

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
} 