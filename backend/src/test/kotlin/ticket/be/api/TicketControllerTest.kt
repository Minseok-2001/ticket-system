package ticket.be.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ticket.be.config.TestSecurityConfig
import ticket.be.domain.Member
import ticket.be.domain.TicketStatus
import ticket.be.dto.*
import ticket.be.service.AuthService
import ticket.be.service.TicketCommandService
import ticket.be.service.TicketQueryService
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@WebMvcTest(TicketController::class)
@Import(TestSecurityConfig::class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=none", "spring.flyway.enabled=false"])
class TicketControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var ticketCommandService: TicketCommandService

    @MockBean
    private lateinit var ticketQueryService: TicketQueryService

    @MockBean
    private lateinit var authService: AuthService

    @Test
    @DisplayName("티켓 예약 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_reserve_ticket() {
        // Mock AuthService
        val member =
            Member(id = 1L, email = "user@example.com", password = "password", name = "Test User")

        `when`(authService.getMemberByEmail("user@example.com")).thenReturn(
            MemberResponse.from(
                member
            )
        )

        val command = ReserveTicketCommand(
            memberId = 1L,
            eventId = 1L,
            ticketCount = 1
        )

        mockMvc.perform(
            post("/api/tickets/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(command))
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.message").value("예약 요청이 접수되었습니다."))
    }

    @Test
    @DisplayName("티켓 확정 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_confirm_ticket() {
        // Mock AuthService
        val member =
            Member(id = 1L, email = "user@example.com", password = "password", name = "Test User")
        `when`(authService.getMemberByEmail("user@example.com")).thenReturn(
            MemberResponse.from(
                member
            )
        )

        mockMvc.perform(
            post("/api/tickets/1/confirm")
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.message").value("예약 확정 요청이 접수되었습니다."))
    }

    @Test
    @DisplayName("티켓 취소 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_cancel_ticket() {
        // Mock AuthService
        val member =
            Member(id = 1L, email = "user@example.com", password = "password", name = "Test User")
        `when`(authService.getMemberByEmail("user@example.com")).thenReturn(
            MemberResponse.from(
                member
            )
        )

        mockMvc.perform(
            post("/api/tickets/1/cancel")
                .param("reason", "테스트 취소")
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.message").value("예약 취소 요청이 접수되었습니다."))
    }

    @Test
    @DisplayName("이벤트별 티켓 목록 조회 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_get_tickets_by_event_id() {
        val tickets = listOf(
            TicketSummaryDto(1L, 1L, "AVAILABLE"),
            TicketSummaryDto(2L, 1L, "RESERVED")
        )

        `when`(ticketQueryService.getTicketsByEventId(1L)).thenReturn(tickets)

        mockMvc.perform(get("/api/tickets/event/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].eventId").value(1))
            .andExpect(jsonPath("$[0].status").value("AVAILABLE"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].status").value("RESERVED"))
    }

    @Test
    @DisplayName("회원별 티켓 목록 조회 API 테스트")
    @WithMockUser(username = "user@example.com", roles = ["ADMIN"])
    fun should_get_tickets_by_member_id() {
        val tickets = listOf(
            TicketSummaryDto(2L, 1L, "RESERVED")
        )

        `when`(ticketQueryService.getTicketsByMemberId(1L)).thenReturn(tickets)

        mockMvc.perform(get("/api/tickets/member/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].eventId").value(1))
            .andExpect(jsonPath("$[0].status").value("RESERVED"))
    }

    @Test
    @DisplayName("내 티켓 목록 조회 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_get_my_tickets() {
        // Mock AuthService
        val member =
            Member(id = 1L, email = "user@example.com", password = "password", name = "Test User")
        `when`(authService.getMemberByEmail("user@example.com")).thenReturn(
            MemberResponse.from(
                member
            )
        )

        val tickets = listOf(
            TicketSummaryDto(2L, 1L, "RESERVED")
        )

        `when`(ticketQueryService.getTicketsByMemberId(1L)).thenReturn(tickets)

        mockMvc.perform(get("/api/tickets/member/me"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].eventId").value(1))
            .andExpect(jsonPath("$[0].status").value("RESERVED"))
    }

    @Test
    @DisplayName("티켓 상세 조회 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_get_ticket_details() {
        val ticket = TicketDto(
            id = 1L,
            eventId = 1L,
            ticketTypeId = 1L,
            seatNumber = "A1",
            price = BigDecimal("100.00"),
            status = TicketStatus.AVAILABLE,
            reservedByMemberId = null,
            reservedAt = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(ticketQueryService.getDetailedTicket(1L)).thenReturn(ticket)

        mockMvc.perform(get("/api/tickets/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.eventId").value(1))
            .andExpect(jsonPath("$.ticketTypeId").value(1))
            .andExpect(jsonPath("$.seatNumber").value("A1"))
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
    }

    @Test
    @DisplayName("티켓 상태 조회 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_get_ticket_status() {
        val status = ReservationStatusDto(
            ticketId = 1L,
            memberId = 1L,
            status = "RESERVED",
            reservedAt = LocalDateTime.now()
        )

        `when`(ticketQueryService.getTicketStatus(1L)).thenReturn(status)

        mockMvc.perform(get("/api/tickets/1/status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.ticketId").value(1))
            .andExpect(jsonPath("$.memberId").value(1))
            .andExpect(jsonPath("$.status").value("RESERVED"))
    }

    @Test
    @DisplayName("이벤트별 티켓 수량 조회 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_get_tickets_count() {
        `when`(ticketQueryService.getAvailableTicketsCount(1L)).thenReturn(8)

        mockMvc.perform(get("/api/tickets/event/1/count"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(8))
    }

    @Test
    @DisplayName("이벤트별 특정 상태 티켓 수량 조회 API 테스트")
    @WithMockUser(username = "user@example.com")
    fun should_get_tickets_count_by_status() {
        `when`(ticketQueryService.getTicketsCountByStatus(1L, TicketStatus.SOLD)).thenReturn(5)

        mockMvc.perform(
            get("/api/tickets/event/1/count")
                .param("status", "SOLD")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(5))
    }
} 