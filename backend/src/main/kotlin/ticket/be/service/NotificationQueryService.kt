package ticket.be.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.dto.NotificationDetailResponse
import ticket.be.dto.NotificationListResponse
import ticket.be.repository.NotificationQueryRepository

@Service
class NotificationQueryService(
    private val notificationQueryRepository: NotificationQueryRepository
) {
    
    @Transactional(readOnly = true)
    fun getNotifications(email: String, page: Int, size: Int): NotificationListResponse {
        val pageable = PageRequest.of(page, size)
        val result = notificationQueryRepository.findNotificationsByEmail(email, pageable)
        
        return NotificationListResponse(
            notifications = result.content,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            currentPage = result.number,
            size = result.size,
            hasNext = result.hasNext()
        )
    }
    
    @Transactional(readOnly = true)
    fun getNotificationDetail(email: String, id: Long): NotificationDetailResponse {
        return notificationQueryRepository.findNotificationDetailByEmailAndId(email, id)
            ?: throw IllegalArgumentException("알림을 찾을 수 없습니다.")
    }
} 