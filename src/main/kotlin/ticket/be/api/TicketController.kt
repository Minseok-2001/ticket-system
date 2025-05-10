package ticket.be.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ticket.be.config.CurrentMember
import ticket.be.domain.TicketStatus
import ticket.be.dto.*
import ticket.be.service.AuthService
import ticket.be.service.TicketCommandService
import ticket.be.service.TicketQueryService

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "티켓", description = "티켓 예매 관련 API")
class TicketController(
    private val ticketCommandService: TicketCommandService,
    private val ticketQueryService: TicketQueryService,
    private val authService: AuthService
) {

    // 명령(Command) 처리 API - 비동기 처리
    @PostMapping("/reserve")
    @Operation(summary = "티켓 예약", description = "티켓을 예약합니다.")
    @PreAuthorize("isAuthenticated()")
    fun reserveTicket(
        @RequestBody command: ReserveTicketCommand,
        @CurrentMember email: String
    ): ResponseEntity<Map<String, String>> {
        val member = authService.getMemberByEmail(email)
        val reserveCommand = command.copy(memberId = member.id)
        ticketCommandService.reserveTicket(reserveCommand)
        return ResponseEntity.accepted().body(mapOf("message" to "예약 요청이 접수되었습니다."))
    }

    @PostMapping("/{ticketId}/confirm")
    @Operation(summary = "티켓 확정", description = "예약된 티켓을 확정합니다.")
    @PreAuthorize("isAuthenticated()")
    fun confirmTicket(
        @PathVariable ticketId: Long,
        @CurrentMember email: String
    ): ResponseEntity<Map<String, String>> {
        val member = authService.getMemberByEmail(email)
        ticketCommandService.confirmTicket(ConfirmTicketCommand(member.id, ticketId))
        return ResponseEntity.accepted().body(mapOf("message" to "예약 확정 요청이 접수되었습니다."))
    }

    @PostMapping("/{ticketId}/cancel")
    @Operation(summary = "티켓 취소", description = "예약된 티켓을 취소합니다.")
    @PreAuthorize("isAuthenticated()")
    fun cancelTicket(
        @PathVariable ticketId: Long,
        @RequestParam(required = false) reason: String?,
        @CurrentMember email: String
    ): ResponseEntity<Map<String, String>> {
        val member = authService.getMemberByEmail(email)
        ticketCommandService.cancelTicket(CancelTicketCommand(member.id, ticketId, reason))
        return ResponseEntity.accepted().body(mapOf("message" to "예약 취소 요청이 접수되었습니다."))
    }

    // 조회(Query) 처리 API - 동기 처리, 읽기 전용 데이터베이스 사용
    @GetMapping("/event/{eventId}")
    @Operation(summary = "이벤트별 티켓 조회", description = "특정 이벤트의 모든 티켓을 조회합니다.")
    fun getTicketsByEventId(@PathVariable eventId: Long): ResponseEntity<List<TicketSummaryDto>> {
        return ResponseEntity.ok(ticketQueryService.getTicketsByEventId(eventId))
    }

    @GetMapping("/member/me")
    @Operation(summary = "내 티켓 조회", description = "현재 로그인한 사용자의 티켓을 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getMyTickets(@CurrentMember email: String): ResponseEntity<List<TicketSummaryDto>> {
        val member = authService.getMemberByEmail(email)
        return ResponseEntity.ok(ticketQueryService.getTicketsByMemberId(member.id))
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "회원별 티켓 조회", description = "특정 회원의 티켓을 조회합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    fun getTicketsByMemberId(@PathVariable memberId: Long): ResponseEntity<List<TicketSummaryDto>> {
        return ResponseEntity.ok(ticketQueryService.getTicketsByMemberId(memberId))
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "티켓 상세 조회", description = "티켓 상세 정보를 조회합니다.")
    fun getTicketDetails(@PathVariable ticketId: Long): ResponseEntity<TicketDto> {
        return ResponseEntity.ok(ticketQueryService.getDetailedTicket(ticketId))
    }

    @GetMapping("/{ticketId}/status")
    @Operation(summary = "티켓 상태 조회", description = "티켓의 예약 상태를 조회합니다.")
    fun getTicketStatus(@PathVariable ticketId: Long): ResponseEntity<ReservationStatusDto> {
        return ResponseEntity.ok(ticketQueryService.getTicketStatus(ticketId))
    }
    
    @GetMapping("/event/{eventId}/count")
    @Operation(summary = "티켓 수량 조회", description = "이벤트의 특정 상태별 티켓 수량을 조회합니다.")
    fun getTicketsCount(
        @PathVariable eventId: Long,
        @RequestParam(required = false) status: String?
    ): ResponseEntity<Map<String, Int>> {
        val count = if (status != null) {
            try {
                val ticketStatus = TicketStatus.valueOf(status.uppercase())
                ticketQueryService.getTicketsCountByStatus(eventId, ticketStatus)
            } catch (e: IllegalArgumentException) {
                ticketQueryService.getAvailableTicketsCount(eventId)
            }
        } else {
            ticketQueryService.getAvailableTicketsCount(eventId)
        }
        
        return ResponseEntity.ok(mapOf("count" to count))
    }
} 