package ticket.be.dto

import java.time.LocalDateTime

/**
 * 대기열 진입 요청 DTO
 */
data class EnterQueueRequest(
    val eventId: Long
)

/**
 * 대기열 위치 응답 DTO
 */
data class QueuePositionResponse(
    val eventId: Long,
    val memberId: Long,
    val position: Int,
    val estimatedWaitTimeSeconds: Long,
    val timestamp: LocalDateTime
)

/**
 * 대기열 상태 응답 DTO
 */
data class QueueStatusResponse(
    val eventId: Long,
    val totalWaiting: Long,
    val activeUsers: Long,
    val maxActiveUsers: Int,
    val isQueueActive: Boolean,
    val timestamp: LocalDateTime
)

/**
 * 대기열 상태 변경 요청 DTO
 */
data class ToggleQueueRequest(
    val eventId: Long,
    val active: Boolean
)

/**
 * 대기열 입장 허가 요청 DTO
 */
data class AdmitUsersRequest(
    val eventId: Long,
    val count: Int = 10
) 