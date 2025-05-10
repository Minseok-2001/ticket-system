package ticket.be.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
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
    private val logger = LoggerFactory.getLogger(TicketController::class.java)

    // 예매 관련 API - 비동기 처리
    @PostMapping("/reserve")
    @Operation(summary = "티켓 예약", description = "티켓을 예약합니다.")
    @PreAuthorize("isAuthenticated()")
    fun reserveTicket(
        @RequestBody command: ReserveTicketCommand,
        @CurrentMember email: String
    ): ResponseEntity<Map<String, String>> {
        logger.info("티켓 예약 요청: eventId={}, ticketTypeId={}, email={}", 
            command.eventId, command.ticketTypeId, email)
        
        val member = authService.getMemberByEmail(email)
        val reserveCommand = command.copy(memberId = member.id)
        
        ticketCommandService.reserveTicket(reserveCommand)
        
        return ResponseEntity.accepted().body(mapOf(
            "message" to "예약 요청이 접수되었습니다.",
            "status" to "ACCEPTED"
        ))
    }

    @PostMapping("/cancel")
    @Operation(summary = "티켓 취소", description = "예약된 티켓을 취소합니다.")
    @PreAuthorize("isAuthenticated()")
    fun cancelTicket(
        @RequestBody command: CancelTicketCommand,
        @CurrentMember email: String
    ): ResponseEntity<Map<String, String>> {
        logger.info("티켓 취소 요청: reservationId={}, email={}", command.reservationId, email)
        
        ticketCommandService.cancelTicket(command)
        
        return ResponseEntity.accepted().body(mapOf(
            "message" to "취소 요청이 접수되었습니다.",
            "status" to "ACCEPTED"
        ))
    }
    
    // 조회 관련 API
    @GetMapping("/my")
    @Operation(summary = "내 티켓 조회", description = "로그인한 사용자의 티켓 목록을 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getMyTickets(
        @CurrentMember email: String,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<TicketResponse>> {
        logger.info("내 티켓 목록 조회 요청: email={}", email)
        
        val member = authService.getMemberByEmail(email)
        val tickets = ticketQueryService.getTicketsByMember(member.id, pageable)
        
        return ResponseEntity.ok(tickets)
    }
    
    @GetMapping("/reservations/my")
    @Operation(summary = "내 예매 내역 조회", description = "로그인한 사용자의 예매 내역을 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getMyReservations(
        @CurrentMember email: String,
        @PageableDefault(size = 10) pageable: Pageable
    ): ResponseEntity<Page<ReservationResponse>> {
        logger.info("내 예매 내역 조회 요청: email={}", email)
        
        val reservations = ticketQueryService.getRecentReservationsByEmail(email, pageable)
        
        return ResponseEntity.ok(reservations)
    }
    
    @GetMapping("/reservations/{reservationId}")
    @Operation(summary = "예매 상세 조회", description = "예매 상세 정보를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getReservationDetail(
        @PathVariable reservationId: Long,
        @CurrentMember email: String
    ): ResponseEntity<ReservationResponse> {
        logger.info("예매 상세 조회 요청: reservationId={}, email={}", reservationId, email)
        
        val reservation = ticketQueryService.getReservationById(reservationId)
        
        // 추가 보안 검증: 예매자가 아닌 경우 접근 차단
        val member = authService.getMemberByEmail(email)
        if (reservation.memberId != member.id) {
            return ResponseEntity.status(403).build()
        }
        
        return ResponseEntity.ok(reservation)
    }
    
    @GetMapping("/reservations/{reservationId}/status")
    @Operation(summary = "예매 상태 조회", description = "예매 상태를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getReservationStatus(
        @PathVariable reservationId: Long,
        @CurrentMember email: String
    ): ResponseEntity<ReservationStatusResponse> {
        logger.info("예매 상태 조회 요청: reservationId={}, email={}", reservationId, email)
        
        val status = ticketQueryService.getReservationStatus(reservationId)
        
        return ResponseEntity.ok(status)
    }
    
    @GetMapping("/{ticketId}")
    @Operation(summary = "티켓 상세 조회", description = "티켓 상세 정보를 조회합니다.")
    @PreAuthorize("isAuthenticated()")
    fun getTicketDetail(
        @PathVariable ticketId: Long,
        @CurrentMember email: String
    ): ResponseEntity<TicketResponse> {
        logger.info("티켓 상세 조회 요청: ticketId={}, email={}", ticketId, email)
        
        val ticket = ticketQueryService.getTicketById(ticketId)
        
        return ResponseEntity.ok(ticket)
    }
    
    @GetMapping("/event/{eventId}")
    @Operation(summary = "이벤트 티켓 조회", description = "특정 이벤트의 티켓 목록을 조회합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    fun getEventTickets(
        @PathVariable eventId: Long,
        @RequestParam status: TicketStatus?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<TicketResponse>> {
        logger.info("이벤트 티켓 조회 요청: eventId={}, status={}", eventId, status)
        
        val tickets = ticketQueryService.getTicketsByEventAndStatus(
            eventId, status ?: TicketStatus.AVAILABLE, pageable
        )
        
        return ResponseEntity.ok(tickets)
    }
} 