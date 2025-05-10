package ticket.be.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

// 알림 목록 응답
data class NotificationListResponse(
    val notifications: List<NotificationSummaryDto>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)

// 알림 요약 DTO
data class NotificationSummaryDto(
    val id: Long,
    val type: String,
    val title: String,
    val content: String,
    val link: String?,
    val status: String,
    val sentAt: LocalDateTime?,
    val readAt: LocalDateTime?,
    val isRead: Boolean
)

// 알림 상세 DTO
data class NotificationDetailResponse(
    val id: Long,
    val type: String,
    val title: String,
    val content: String,
    val link: String?,
    val status: String,
    val sentAt: LocalDateTime?,
    val readAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val errorMessage: String?
)

// 디바이스 토큰 요청
data class DeviceTokenRequest(
    @field:NotBlank(message = "디바이스 토큰은 필수입니다")
    val deviceToken: String
)

// 알림 이벤트 DTO
data class NotificationEvent(
    val memberId: Long,
    val type: String,
    val title: String,
    val content: String,
    val link: String?
) 