package ticket.be.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import ticket.be.BaseTest
import java.math.BigDecimal
import java.time.LocalDateTime

@DisplayName("Event 도메인 모델 테스트")
class EventTest : BaseTest() {

    private lateinit var event: Event
    
    @BeforeEach
    fun setUp() {
        event = testDataFactory.createEvent(
            totalSeats = 1000,
            availableSeats = 1000
        )
    }

    @Test
    @DisplayName("이벤트 생성 테스트 - 기본 정보로 생성 시 성공해야 함")
    fun createEventWithBasicInfo() {
        // given
        val name = "테스트 콘서트"
        val description = "테스트 콘서트 설명"
        val startDateTime = LocalDateTime.now().plusDays(10)
        val endDateTime = startDateTime.plusHours(3)
        val location = "테스트 공연장"
        val totalSeats = 500
        val ticketPrice = BigDecimal("50000")
        val saleStartDateTime = LocalDateTime.now()
        val saleEndDateTime = LocalDateTime.now().plusDays(5)

        // when
        val event = Event(
            name = name,
            description = description,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            location = location,
            totalSeats = totalSeats,
            availableSeats = totalSeats,
            ticketPrice = ticketPrice,
            saleStartDateTime = saleStartDateTime,
            saleEndDateTime = saleEndDateTime
        )

        // then
        assertEquals(name, event.name)
        assertEquals(description, event.description)
        assertEquals(startDateTime, event.startDateTime)
        assertEquals(endDateTime, event.endDateTime)
        assertEquals(location, event.location)
        assertEquals(totalSeats, event.totalSeats)
        assertEquals(totalSeats, event.availableSeats)
        assertEquals(ticketPrice, event.ticketPrice)
        assertEquals(saleStartDateTime, event.saleStartDateTime)
        assertEquals(saleEndDateTime, event.saleEndDateTime)
    }

    @Test
    @DisplayName("티켓 예약 테스트 - 성공적으로 좌석이 예약되어야 함")
    fun reserveTicketsSuccessfully() {
        // given
        val numTickets = 5

        // when
        val result = event.reserveTickets(numTickets)

        // then
        assertTrue(result)
        assertEquals(995, event.availableSeats)
    }

    @Test
    @DisplayName("티켓 예약 실패 테스트 - 요청 좌석 수가 가용 좌석 수를 초과할 경우")
    fun reserveTicketsFailWhenExceedsAvailable() {
        // given
        val tooManyTickets = 1001

        // when
        val result = event.reserveTickets(tooManyTickets)

        // then
        assertFalse(result)
        assertEquals(1000, event.availableSeats) // 변화 없음
    }

    @Test
    @DisplayName("티켓 예약 취소 테스트 - 성공적으로 좌석이 반환되어야 함")
    fun cancelTicketReservation() {
        // given
        val numTickets = 5
        event.reserveTickets(numTickets)
        
        // when
        event.cancelTicketReservation(numTickets)

        // then
        assertEquals(1000, event.availableSeats)
    }

    @Test
    @DisplayName("이벤트 판매 시작 여부 테스트")
    fun isOnSale() {
        // given
        val pastEvent = testDataFactory.createEvent(
            saleStartDateTime = LocalDateTime.now().minusDays(5),
            saleEndDateTime = LocalDateTime.now().plusDays(5)
        )
        
        val futureEvent = testDataFactory.createEvent(
            saleStartDateTime = LocalDateTime.now().plusDays(5),
            saleEndDateTime = LocalDateTime.now().plusDays(10)
        )
        
        val endedEvent = testDataFactory.createEvent(
            saleStartDateTime = LocalDateTime.now().minusDays(10),
            saleEndDateTime = LocalDateTime.now().minusDays(5)
        )

        // when & then
        assertTrue(pastEvent.isOnSale())
        assertFalse(futureEvent.isOnSale())
        assertFalse(endedEvent.isOnSale())
    }

    @Test
    @DisplayName("매진 상태 테스트")
    fun isSoldOut() {
        // given
        val soldOutEvent = testDataFactory.createEvent(
            totalSeats = 100,
            availableSeats = 0
        )
        
        val availableEvent = testDataFactory.createEvent(
            totalSeats = 100,
            availableSeats = 50
        )

        // when & then
        assertTrue(soldOutEvent.isSoldOut())
        assertFalse(availableEvent.isSoldOut())
    }
} 