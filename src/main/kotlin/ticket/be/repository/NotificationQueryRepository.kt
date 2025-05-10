package ticket.be.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import ticket.be.domain.QMember
import ticket.be.domain.QNotification
import ticket.be.dto.NotificationDetailResponse
import ticket.be.dto.NotificationSummaryDto

@Repository
class NotificationQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    
    fun findNotificationsByEmail(email: String, pageable: Pageable): Page<NotificationSummaryDto> {
        val member = QMember.member
        val notification = QNotification.notification
        
        // 전체 개수 조회
        val totalCount = queryFactory
            .select(notification.count())
            .from(notification)
            .join(notification.member, member)
            .where(member.email.eq(email))
            .fetchOne() ?: 0L
        
        // 페이징 처리된 알림 목록 조회
        val results = queryFactory
            .select(
                Projections.constructor(
                    NotificationSummaryDto::class.java,
                    notification.id,
                    notification.type,
                    notification.title,
                    notification.content,
                    notification.link,
                    notification.status.stringValue(),
                    notification.sentAt,
                    notification.readAt,
                    notification.readAt.isNotNull
                )
            )
            .from(notification)
            .join(notification.member, member)
            .where(member.email.eq(email))
            .orderBy(notification.createdAt.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        
        return PageImpl(results, pageable, totalCount)
    }
    
    fun findNotificationDetailByEmailAndId(email: String, id: Long): NotificationDetailResponse? {
        val member = QMember.member
        val notification = QNotification.notification
        
        return queryFactory
            .select(
                Projections.constructor(
                    NotificationDetailResponse::class.java,
                    notification.id,
                    notification.type,
                    notification.title,
                    notification.content,
                    notification.link,
                    notification.status.stringValue(),
                    notification.sentAt,
                    notification.readAt,
                    notification.createdAt,
                    notification.errorMessage
                )
            )
            .from(notification)
            .join(notification.member, member)
            .where(member.email.eq(email).and(notification.id.eq(id)))
            .fetchOne()
    }
} 