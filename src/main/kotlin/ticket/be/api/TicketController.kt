package ticket.be.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ticket.be.domain.TicketStatus
import ticket.be.dto.*
import ticket.be.service.TicketCommandService
import ticket.be.service.TicketQueryService

@RestController
@RequestMapping("/api/tickets")
class TicketController(
    private val ticketCommandService: TicketCommandService,
    private val ticketQueryService: TicketQueryService
) {

    // 명령(Command) 처리 API - 비동기 처리
    @PostMapping("/reserve")
    fun reserveTicket(@RequestBody command: ReserveTicketCommand): ResponseEntity<Map<String, String>> {
        ticketCommandService.reserveTicket(command)
        return ResponseEntity.accepted().body(mapOf("message" to "예약 요청이 접수되었습니다."))
    }

    @PostMapping("/{ticketId}/confirm")
    fun confirmTicket(
        @PathVariable ticketId: Long,
        @RequestParam memberId: Long
    ): ResponseEntity<Map<String, String>> {
        ticketCommandService.confirmTicket(ConfirmTicketCommand(memberId, ticketId))
        return ResponseEntity.accepted().body(mapOf("message" to "예약 확정 요청이 접수되었습니다."))
    }

    @PostMapping("/{ticketId}/cancel")
    fun cancelTicket(
        @PathVariable ticketId: Long,
        @RequestParam memberId: Long,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<Map<String, String>> {
        ticketCommandService.cancelTicket(CancelTicketCommand(memberId, ticketId, reason))
        return ResponseEntity.accepted().body(mapOf("message" to "예약 취소 요청이 접수되었습니다."))
    }

    // 조회(Query) 처리 API - 동기 처리, 읽기 전용 데이터베이스 사용
    @GetMapping("/event/{eventId}")
    fun getTicketsByEventId(@PathVariable eventId: Long): ResponseEntity<List<TicketSummaryDto>> {
        return ResponseEntity.ok(ticketQueryService.getTicketsByEventId(eventId))
    }

    @GetMapping("/member/{memberId}")
    fun getTicketsByMemberId(@PathVariable memberId: Long): ResponseEntity<List<TicketSummaryDto>> {
        return ResponseEntity.ok(ticketQueryService.getTicketsByMemberId(memberId))
    }

    @GetMapping("/{ticketId}")
    fun getTicketDetails(@PathVariable ticketId: Long): ResponseEntity<TicketDto> {
        return ResponseEntity.ok(ticketQueryService.getDetailedTicket(ticketId))
    }

    @GetMapping("/{ticketId}/status")
    fun getTicketStatus(@PathVariable ticketId: Long): ResponseEntity<ReservationStatusDto> {
        return ResponseEntity.ok(ticketQueryService.getTicketStatus(ticketId))
    }
    
    @GetMapping("/event/{eventId}/count")
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