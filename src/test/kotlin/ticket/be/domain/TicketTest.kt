package ticket.be.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime

class TicketTest {

    private lateinit var member: Member
    private lateinit var event: Event
    private lateinit var ticketType: TicketType
    private lateinit var ticket: Ticket

    @BeforeEach
    fun setUp() {
        member = Member(
            id = 1L,
            email = "test@example.com",
            password = "password",
            name = "Test User"
        )

        event = Event(
            id = this.event.id,
            name = "Test Event",
            content = "Test Event Description",
            venue = "Test Venue",
            eventDate = LocalDateTime.now().plusDays(30),
            salesStartDate = LocalDateTime.now().minusDays(1),
            salesEndDate = LocalDateTime.now().plusDays(15),
            totalSeats = 100,
            status = EventStatus.UPCOMING
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
    }

    @Test
    @DisplayName("Ticket 예약 성공 테스트")
    fun should_reserve_ticket_when_available() {
        // Given: 사용 가능한 티켓이 있음
        assertThat(ticket.status).isEqualTo(TicketStatus.AVAILABLE)
        assertThat(ticket.reservedByMember).isNull()

        // When: 티켓 예약
        val result = ticket.reserve(member)

        // Then: 예약 성공 확인
        assertThat(result).isTrue()
        assertThat(ticket.status).isEqualTo(TicketStatus.RESERVED)
        assertThat(ticket.reservedByMember).isEqualTo(member)
        assertThat(ticket.reservedAt).isNotNull()
    }

    @Test
    @DisplayName("이미 예약된 티켓 예약 실패 테스트")
    fun should_fail_to_reserve_ticket_when_already_reserved() {
        // Given: 이미 예약된 티켓
        ticket.status = TicketStatus.RESERVED
        ticket.reservedByMember = member
        ticket.reservedAt = LocalDateTime.now()

        // When: 다시 예약 시도
        val result = ticket.reserve(member)

        // Then: 예약 실패 확인
        assertThat(result).isFalse()
    }

    @Test
    @DisplayName("Ticket 확정 성공 테스트")
    fun should_confirm_ticket_when_reserved() {
        // Given: 예약된 티켓
        ticket.status = TicketStatus.RESERVED
        ticket.reservedByMember = member
        ticket.reservedAt = LocalDateTime.now()

        // When: 티켓 확정
        val result = ticket.confirm()

        // Then: 확정 성공 확인
        assertThat(result).isTrue()
        assertThat(ticket.status).isEqualTo(TicketStatus.SOLD)
    }

    @Test
    @DisplayName("예약되지 않은 티켓 확정 실패 테스트")
    fun should_fail_to_confirm_ticket_when_not_reserved() {
        // Given: 예약되지 않은 티켓
        ticket.status = TicketStatus.AVAILABLE

        // When: 티켓 확정 시도
        val result = ticket.confirm()

        // Then: 확정 실패 확인
        assertThat(result).isFalse()
        assertThat(ticket.status).isEqualTo(TicketStatus.AVAILABLE)
    }

    @Test
    @DisplayName("Ticket 취소 성공 테스트")
    fun should_cancel_ticket_when_reserved() {
        // Given: 예약된 티켓
        ticket.status = TicketStatus.RESERVED
        ticket.reservedByMember = member
        ticket.reservedAt = LocalDateTime.now()

        // When: 티켓 취소
        val result = ticket.cancel()

        // Then: 취소 성공 확인
        assertThat(result).isTrue()
        assertThat(ticket.status).isEqualTo(TicketStatus.AVAILABLE)
        assertThat(ticket.reservedByMember).isNull()
        assertThat(ticket.reservedAt).isNull()
    }

    @Test
    @DisplayName("예약되지 않은 티켓 취소 실패 테스트")
    fun should_fail_to_cancel_ticket_when_not_reserved() {
        // Given: 예약되지 않은 티켓
        ticket.status = TicketStatus.AVAILABLE

        // When: 티켓 취소 시도
        val result = ticket.cancel()

        // Then: 취소 실패 확인
        assertThat(result).isFalse()
    }

    companion object {
        const val event_id = 1L
    }
} 