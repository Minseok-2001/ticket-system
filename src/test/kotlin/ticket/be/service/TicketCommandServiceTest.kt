package ticket.be.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import ticket.be.domain.Event
import ticket.be.domain.Member
import ticket.be.domain.Ticket
import ticket.be.domain.TicketStatus
import ticket.be.dto.CancelTicketCommand
import ticket.be.dto.ConfirmTicketCommand
import ticket.be.dto.ReserveTicketCommand
import ticket.be.repository.MemberRepository
import ticket.be.repository.TicketRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class TicketCommandServiceTest {

    @Mock
    private lateinit var ticketRepository: TicketRepository

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>

    @InjectMocks
    private lateinit var ticketCommandService: TicketCommandService

    private lateinit var member: Member
    private lateinit var event: Event
    private lateinit var ticket: Ticket

    @BeforeEach
    fun setup() {
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

        ticket = Ticket(
            id = 1L,
            event = event,
            ticketType = mock(),
            seatNumber = "A1",
            price = BigDecimal("100.00"),
            status = TicketStatus.AVAILABLE
        )
    }

    @Test
    @DisplayName("예약 가능한 티켓이 있을 때 티켓 예약 성공")
    fun should_reserve_ticket_when_available() {
        // Given
        val command = ReserveTicketCommand(memberId = 1L, eventId = 1L, ticketCount = 1)
        
        `when`(ticketRepository.findFirstAvailableTicketForEvent(1L)).thenReturn(Optional.of(ticket))
        `when`(memberRepository.findById(1L)).thenReturn(Optional.of(member))
        
        // When
        ticketCommandService.processReserveTicket(command)
        
        // Then
        verify(ticketRepository).save(any())
        verify(kafkaTemplate).send(eq("ticket-events"), any(), any())
        
        // 티켓 상태가 변경되었는지 확인
        assert(ticket.status == TicketStatus.RESERVED)
        assert(ticket.reservedByMember == member)
    }
    
    @Test
    @DisplayName("예약된 티켓 확정 성공")
    fun should_confirm_ticket_when_reserved() {
        // Given
        val command = ConfirmTicketCommand(memberId = 1L, ticketId = 1L)
        ticket.status = TicketStatus.RESERVED
        ticket.reservedByMember = member
        
        `when`(ticketRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ticket))
        
        // When
        ticketCommandService.processConfirmTicket(command)
        
        // Then
        verify(ticketRepository).save(any())
        verify(kafkaTemplate).send(eq("ticket-events"), any(), any())
        
        // 티켓 상태가 변경되었는지 확인
        assert(ticket.status == TicketStatus.SOLD)
    }
    
    @Test
    @DisplayName("예약된 티켓 취소 성공")
    fun should_cancel_ticket_when_reserved() {
        // Given
        val command = CancelTicketCommand(memberId = 1L, ticketId = 1L, reason = "테스트 취소")
        ticket.status = TicketStatus.RESERVED
        ticket.reservedByMember = member
        
        `when`(ticketRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(ticket))
        
        // When
        ticketCommandService.processCancelTicket(command)
        
        // Then
        verify(ticketRepository).save(any())
        verify(kafkaTemplate).send(eq("ticket-events"), any(), any())
        
        // 티켓 상태가 변경되었는지 확인
        assert(ticket.status == TicketStatus.AVAILABLE)
        assert(ticket.reservedByMember == null)
    }
} 