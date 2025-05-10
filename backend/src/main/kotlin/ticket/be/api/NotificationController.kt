package ticket.be.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ticket.be.config.CurrentMember
import ticket.be.dto.*
import ticket.be.service.NotificationCommandService
import ticket.be.service.NotificationQueryService

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "알림", description = "알림 관련 API")
class NotificationController(
    private val notificationCommandService: NotificationCommandService,
    private val notificationQueryService: NotificationQueryService
) {
    private val logger = LoggerFactory.getLogger(NotificationController::class.java)

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 조회합니다.")
    fun getNotifications(
        @CurrentMember email: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<NotificationListResponse> {
        logger.info("알림 목록 조회: email={}, page={}, size={}", email, page, size)
        return ResponseEntity.ok(notificationQueryService.getNotifications(email, page, size))
    }

    @GetMapping("/{id}")
    @Operation(summary = "알림 상세 조회", description = "알림의 상세 정보를 조회합니다.")
    fun getNotification(
        @CurrentMember email: String,
        @PathVariable id: Long
    ): ResponseEntity<NotificationDetailResponse> {
        logger.info("알림 상세 조회: email={}, id={}", email, id)
        return ResponseEntity.ok(notificationQueryService.getNotificationDetail(email, id))
    }

    @PostMapping("/register")
    @Operation(summary = "디바이스 토큰 등록", description = "푸시 알림을 위한 디바이스 토큰을 등록합니다.")
    fun registerDeviceToken(
        @CurrentMember email: String,
        @Valid @RequestBody request: DeviceTokenRequest
    ): ResponseEntity<Void> {
        logger.info("디바이스 토큰 등록: email={}", email)
        notificationCommandService.registerDeviceToken(email, request.deviceToken)
        return ResponseEntity.ok().build()
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다.")
    fun markAsRead(
        @CurrentMember email: String,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        logger.info("알림 읽음 처리: email={}, id={}", email, id)
        notificationCommandService.markAsRead(email, id)
        return ResponseEntity.ok().build()
    }
} 