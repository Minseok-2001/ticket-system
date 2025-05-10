package ticket.be.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ticket.be.dto.ReserveTicketCommand
import ticket.be.dto.ConfirmTicketCommand
import ticket.be.dto.CancelTicketCommand
import ticket.be.dto.ReservationStatusDto
import ticket.be.dto.TicketSummaryDto
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
        @RequestParam userId: Long
    ): ResponseEntity<Map<String, String>> {
        ticketCommandService.confirmTicket(ConfirmTicketCommand(userId, ticketId))
        return ResponseEntity.accepted().body(mapOf("message" to "예약 확정 요청이 접수되었습니다."))
    }
    
    @PostMapping("/{ticketId}/cancel")
    fun cancelTicket(
        @PathVariable ticketId: Long,
        @RequestParam userId: Long
    ): ResponseEntity<Map<String, String>> {
        ticketCommandService.cancelTicket(CancelTicketCommand(userId, ticketId))
        return ResponseEntity.accepted().body(mapOf("message" to "예약 취소 요청이 접수되었습니다."))
    }
    
    // 조회(Query) 처리 API - 동기 처리, 읽기 전용 데이터베이스 사용
    @GetMapping("/event/{eventId}")
    fun getTicketsForEvent(@PathVariable eventId: Long): ResponseEntity<List<TicketSummaryDto>> {
        val tickets = ticketQueryService.getTicketSummaries(eventId)
        return ResponseEntity.ok(tickets)
    }
    
    @GetMapping("/status")
    fun getReservationStatus(
        @RequestParam userId: Long,
        @RequestParam ticketId: Long
    ): ResponseEntity<ReservationStatusDto> {
        val status = ticketQueryService.getReservationStatus(userId, ticketId)
        return if (status != null) {
            ResponseEntity.ok(status)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/event/{eventId}/available-count")
    fun getAvailableTicketsCount(@PathVariable eventId: Long): ResponseEntity<Map<String, Int>> {
        val count = ticketQueryService.getAvailableTicketCount(eventId)
        return ResponseEntity.ok(mapOf("availableCount" to count))
    }
} 