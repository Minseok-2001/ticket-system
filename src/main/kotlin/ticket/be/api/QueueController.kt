package ticket.be.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ticket.be.config.CurrentMember
import ticket.be.dto.*
import ticket.be.service.AuthService
import ticket.be.service.DistributedLockService
import ticket.be.service.EventQueueService
import java.util.function.Supplier

@RestController
@RequestMapping("/api/queue")
@Tag(name = "대기열", description = "대기열 관련 API")
class QueueController(
    private val eventQueueService: EventQueueService,
    private val distributedLockService: DistributedLockService,
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(QueueController::class.java)
    
    @PostMapping("/enter")
    @Operation(summary = "대기열 진입", description = "이벤트 대기열에 사용자를 추가합니다.")
    @PreAuthorize("isAuthenticated()")
    fun enterQueue(
        @RequestBody request: EnterQueueRequest,
        @CurrentMember email: String
    ): ResponseEntity<QueuePositionResponse> {
        logger.info("대기열 진입 요청: eventId={}, email={}", request.eventId, email)
        
        val member = authService.getMemberByEmail(email)
        
        // 분산 락 사용하여 동시성 제어
        val lockName = "queue:enter:${request.eventId}"
        val result = distributedLockService.executeWithLock(
            lockName = lockName,
            supplier = Supplier {
                eventQueueService.addToQueue(request.eventId, member.id)
            }
        )
        
        return ResponseEntity.ok(result)
    }
    
    @GetMapping("/position/{eventId}")
    @Operation(summary = "대기열 위치 조회", description = "이벤트 대기열에서 사용자의 현재 위치를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getQueuePosition(
        @PathVariable eventId: Long,
        @CurrentMember email: String
    ): ResponseEntity<QueuePositionResponse> {
        logger.info("대기열 위치 조회 요청: eventId={}, email={}", eventId, email)
        
        val member = authService.getMemberByEmail(email)
        val position = eventQueueService.getQueuePosition(eventId, member.id)
        
        return ResponseEntity.ok(position)
    }
    
    @GetMapping("/status/{eventId}")
    @Operation(summary = "대기열 상태 조회", description = "이벤트 대기열의 전체 상태를 조회합니다.")
    fun getQueueStatus(
        @PathVariable eventId: Long
    ): ResponseEntity<QueueStatusResponse> {
        logger.info("대기열 상태 조회 요청: eventId={}", eventId)
        
        val status = eventQueueService.getQueueStatus(eventId)
        
        return ResponseEntity.ok(status)
    }
    
    @PostMapping("/admin/toggle")
    @Operation(summary = "대기열 활성화/비활성화", description = "이벤트 대기열을 활성화하거나 비활성화합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    fun toggleQueueStatus(
        @RequestBody request: ToggleQueueRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("대기열 상태 변경 요청: eventId={}, active={}", request.eventId, request.active)
        
        eventQueueService.toggleQueueStatus(request.eventId, request.active)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "대기열 상태가 변경되었습니다."
        ))
    }
    
    @PostMapping("/admin/admit")
    @Operation(summary = "사용자 입장 허가", description = "대기열 상위 N명의 사용자에게 입장을 허가합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    fun admitUsers(
        @RequestBody request: AdmitUsersRequest
    ): ResponseEntity<Map<String, Any>> {
        logger.info("사용자 입장 허가 요청: eventId={}, count={}", request.eventId, request.count)
        
        // 분산 락 사용하여 동시성 제어
        val lockName = "queue:admit:${request.eventId}"
        val admitted = distributedLockService.executeWithLock(
            lockName = lockName,
            supplier = Supplier {
                eventQueueService.admitNextUsers(request.eventId, request.count)
            }
        )
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "$admitted 명의 사용자에게 입장이 허가되었습니다."
        ))
    }
    
    @PostMapping("/admin/cleanup/{eventId}")
    @Operation(summary = "만료 항목 정리", description = "이벤트 대기열에서 만료된 항목을 정리합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    fun cleanupExpiredEntries(
        @PathVariable eventId: Long
    ): ResponseEntity<Map<String, Any>> {
        logger.info("만료 항목 정리 요청: eventId={}", eventId)
        
        val expired = eventQueueService.cleanupExpiredEntries(eventId)
        
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "message" to "$expired 개의 만료된 항목이 정리되었습니다."
        ))
    }
} 