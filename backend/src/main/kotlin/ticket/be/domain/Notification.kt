package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity
import java.time.LocalDateTime

@Entity
@Table(name = "notification")
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val member: Member,
    
    @Column(nullable = false, length = 50)
    val type: String,
    
    @Column(nullable = false, length = 200)
    val title: String,
    
    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
    
    @Column(length = 500)
    val link: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: NotificationStatus = NotificationStatus.PENDING,
    
    @Column
    var sentAt: LocalDateTime? = null,
    
    @Column
    var readAt: LocalDateTime? = null,
    
    @Column(length = 500)
    var errorMessage: String? = null
) : BaseTimeEntity() {

    fun markAsSent() {
        this.status = NotificationStatus.SENT
        this.sentAt = LocalDateTime.now()
    }
    
    fun markAsRead() {
        this.status = NotificationStatus.READ
        this.readAt = LocalDateTime.now()
    }
    
    fun markAsFailed(errorMessage: String) {
        this.status = NotificationStatus.FAILED
        this.errorMessage = errorMessage
    }
}

enum class NotificationStatus {
    PENDING, SENT, READ, FAILED
} 