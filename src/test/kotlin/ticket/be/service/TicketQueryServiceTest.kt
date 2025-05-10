package ticket.be.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import ticket.be.domain.Event
import ticket.be.domain.Member
import ticket.be.domain.Ticket
import ticket.be.domain.TicketStatus
import ticket.be.domain.TicketType
import ticket.be.repository.TicketQueryRepository
import ticket.be.repository.TicketRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import org.assertj.core.api.Assertions.assertThat

@ExtendWith(MockitoExtension::class)
class TicketQueryServiceTest {

    @Mock
    private lateinit var ticketRepository: TicketRepository

    @Mock
    private lateinit var ticketQueryRepository: TicketQueryRepository

    @InjectMocks
    private lateinit var ticketQueryService: TicketQueryService

    private lateinit var member: Member
    private lateinit var event: Event
    private lateinit var ticketType: TicketType
    private lateinit var ticket: Ticket
    private lateinit var tickets: List<Ticket>

    @BeforeEach
    fun setUp() {
        // 테스트 데이터 준비
        member = Member(
            id = 1L,
            email = "test@example.com",
            password = "password",
            name = "Test User"
        )

        event = Event(
            id = 1L,
            name = "Test Event",
            content = "Test Event Description",
            venue = "Test Venue",
            eventDate = LocalDateTime.now().plusDays(30),
            salesStartDate = LocalDateTime.now().minusDays(1),
            salesEndDate = LocalDateTime.now().plusDays(15),
            totalSeats = 100,
            status = ticket.be.domain.EventStatus.UPCOMING
        )

        ticketType = TicketType(
            id = 1L,
            event = event,
            name = "VIP",
            price = BigDecimal("100.00"),
            quantity = 10,
            availableQuantity = 8,
            content = "VIP 티켓"
        )

        ticket = Ticket(
            id = 1L,
            event = event,
            ticketType = ticketType,
            seatNumber = "A1",
            price = BigDecimal("100.00"),
            status = TicketStatus.AVAILABLE
        )

        // 리스트에 티켓 추가
        tickets = listOf(
            ticket,
            Ticket(
                id = 2L,
                event = event,
                ticketType = ticketType,
                seatNumber = "A2",
                price = BigDecimal("100.00"),
                status = TicketStatus.RESERVED,
                reservedByMember = member,
                reservedAt = LocalDateTime.now().minusDays(1)
            )
        )
    }

    @Test
    @DisplayName("이벤트 ID로 티켓 목록 조회")
    fun should_return_tickets_by_event_id() {
        // Given
        `when`(ticketRepository.findAllByEventId(event.id)).thenReturn(tickets)

        // When
        val result = ticketQueryService.getTicketsByEventId(event.id)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(1L)
        assertThat(result[0].eventId).isEqualTo(1L)
        assertThat(result[0].status).isEqualTo(TicketStatus.AVAILABLE.name)
        
        assertThat(result[1].id).isEqualTo(2L)
        assertThat(result[1].eventId).isEqualTo(1L)
        assertThat(result[1].status).isEqualTo(TicketStatus.RESERVED.name)
    }

    @Test
    @DisplayName("회원 ID로 예약된 티켓 목록 조회")
    fun should_return_tickets_by_member_id() {
        // Given
        val memberTickets = listOf(tickets[1]) // 회원이 예약한 티켓만
        `when`(ticketRepository.findAllByReservedByMemberId(member.id)).thenReturn(memberTickets)

        // When
        val result = ticketQueryService.getTicketsByMemberId(member.id)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(2L)
        assertThat(result[0].status).isEqualTo(TicketStatus.RESERVED.name)
    }

    @Test
    @DisplayName("티켓 ID로 상세 정보 조회")
    fun should_return_detailed_ticket_by_id() {
        // Given
        `when`(ticketRepository.findById(ticket.id)).thenReturn(Optional.of(ticket))

        // When
        val result = ticketQueryService.getDetailedTicket(ticket.id)

        // Then
        assertThat(result.id).isEqualTo(ticket.id)
        assertThat(result.eventId).isEqualTo(event.id)
        assertThat(result.ticketTypeId).isEqualTo(ticketType.id)
        assertThat(result.price).isEqualTo(ticket.price)
        assertThat(result.status).isEqualTo(ticket.status)
    }

    @Test
    @DisplayName("이벤트의 사용 가능한 티켓 수 조회")
    fun should_return_available_tickets_count() {
        // Given
        `when`(ticketRepository.countByEventIdAndStatus(event.id, TicketStatus.AVAILABLE)).thenReturn(8L)

        // When
        val result = ticketQueryService.getAvailableTicketsCount(event.id)

        // Then
        assertThat(result).isEqualTo(8)
    }

    @Test
    @DisplayName("이벤트의 상태별 티켓 수 조회")
    fun should_return_tickets_count_by_status() {
        // Given
        `when`(ticketRepository.countByEventIdAndStatus(event.id, TicketStatus.SOLD)).thenReturn(5L)

        // When
        val result = ticketQueryService.getTicketsCountByStatus(event.id, TicketStatus.SOLD)

        // Then
        assertThat(result).isEqualTo(5)
    }
} 