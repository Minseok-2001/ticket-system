package ticket.be.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ticket.be.domain.Notification
import ticket.be.domain.NotificationStatus

interface NotificationRepository : JpaRepository<Notification, Long> {
    
    // 회원 이메일로 알림 목록 페이징 조회
    @Query("SELECT n FROM Notification n JOIN n.member m WHERE m.email = :email ORDER BY n.createdAt DESC")
    fun findByMemberEmailOrderByCreatedAtDesc(
        @Param("email") email: String,
        pageable: Pageable
    ): Page<Notification>
    
    // 회원 이메일과 알림 ID로 조회
    @Query("SELECT n FROM Notification n JOIN n.member m WHERE m.email = :email AND n.id = :id")
    fun findByMemberEmailAndId(
        @Param("email") email: String,
        @Param("id") id: Long
    ): Notification?
    
    // 회원 ID로 알림 조회
    fun findByMemberId(memberId: Long): List<Notification>
    
    // 특정 상태의 알림 조회
    fun findByStatus(status: NotificationStatus): List<Notification>
} 