package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity
import java.time.LocalDateTime

@Entity
@Table(name = "queue_entry")
class QueueEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "event_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    val event: Event,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    val member: Member,

    @Column(nullable = false)
    var queuePosition: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: QueueStatus = QueueStatus.WAITING,

    @Column
    var notifiedAt: LocalDateTime? = null,

    @Column
    var enteredAt: LocalDateTime? = null,

    @Column
    var expiresAt: LocalDateTime? = null
) : BaseTimeEntity() {

    fun getNotify() {
        status = QueueStatus.NOTIFIED
        notifiedAt = LocalDateTime.now()
        // 일반적으로 30분 만료 시간 설정
        expiresAt = notifiedAt!!.plusMinutes(30)
    }

    fun enter() {
        status = QueueStatus.ENTERED
        enteredAt = LocalDateTime.now()
    }

    fun expire() {
        status = QueueStatus.EXPIRED
    }

    fun isExpired(): Boolean {
        return expiresAt?.isBefore(LocalDateTime.now()) ?: false
    }
}

enum class QueueStatus {
    WAITING, NOTIFIED, ENTERED, EXPIRED, COMPLETED
} 