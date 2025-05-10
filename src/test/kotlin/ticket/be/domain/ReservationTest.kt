package ticket.be.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import ticket.be.BaseTest
import java.math.BigDecimal

@DisplayName("Reservation 도메인 모델 테스트")
class ReservationTest : BaseTest() {

    private lateinit var member: Member
    private lateinit var event: Event
    
    @BeforeEach
    fun setUp() {
        member = testDataFactory.createMember()
        event = testDataFactory.createEvent()
    }

    @Test
    @DisplayName("예약 생성 테스트 - 기본 정보로 생성 시 성공해야 함")
    fun createReservationWithBasicInfo() {
        // given
        val numTickets = 2
        val totalAmount = event.ticketPrice.multiply(BigDecimal(numTickets))
        val paymentMethod = "CREDIT_CARD"

        // when
        val reservation = Reservation(
            member = member,
            event = event,
            numTickets = numTickets,
            totalAmount = totalAmount,
            status = ReservationStatus.PENDING,
            paymentMethod = paymentMethod
        )

        // then
        assertEquals(member, reservation.member)
        assertEquals(event, reservation.event)
        assertEquals(numTickets, reservation.numTickets)
        assertEquals(totalAmount, reservation.totalAmount)
        assertEquals(ReservationStatus.PENDING, reservation.status)
        assertEquals(paymentMethod, reservation.paymentMethod)
        assertNull(reservation.paymentId)
    }

    @Test
    @DisplayName("예약 상태 변경 테스트 - 확인 상태로 변경되어야 함")
    fun confirmReservation() {
        // given
        val reservation = testDataFactory.createReservation(
            member = member,
            event = event,
            status = ReservationStatus.PENDING
        )

        // when
        reservation.confirm()

        // then
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
    }

    @Test
    @DisplayName("예약 완료 테스트 - 결제 ID가 설정되고 상태가 COMPLETED로 변경되어야 함")
    fun completeReservation() {
        // given
        val reservation = testDataFactory.createReservation(
            member = member,
            event = event,
            status = ReservationStatus.CONFIRMED
        )
        val paymentId = "test-payment-id"

        // when
        reservation.complete(paymentId)

        // then
        assertEquals(ReservationStatus.COMPLETED, reservation.status)
        assertEquals(paymentId, reservation.paymentId)
    }

    @Test
    @DisplayName("예약 취소 테스트 - 상태가 CANCELLED로 변경되어야 함")
    fun cancelReservation() {
        // given
        val reservation = testDataFactory.createReservation(
            member = member,
            event = event,
            status = ReservationStatus.PENDING
        )
        val cancelReason = "테스트 취소 사유"

        // when
        reservation.cancel(cancelReason)

        // then
        assertEquals(ReservationStatus.CANCELLED, reservation.status)
    }

    @Test
    @DisplayName("환불 테스트 - 상태가 REFUNDED로 변경되어야 함")
    fun refundReservation() {
        // given
        val reservation = testDataFactory.createReservation(
            member = member,
            event = event,
            status = ReservationStatus.COMPLETED,
            paymentId = "test-payment-id"
        )
        val refundReason = "테스트 환불 사유"

        // when
        reservation.refund(refundReason)

        // then
        assertEquals(ReservationStatus.REFUNDED, reservation.status)
    }

    @Test
    @DisplayName("유효한 상태 전이 테스트 - 상태 변경이 허용되는 경우만 성공해야 함")
    fun validStatusTransition() {
        // given
        val pendingReservation = testDataFactory.createReservation(status = ReservationStatus.PENDING)
        val confirmedReservation = testDataFactory.createReservation(status = ReservationStatus.CONFIRMED)
        val completedReservation = testDataFactory.createReservation(status = ReservationStatus.COMPLETED)
        val cancelledReservation = testDataFactory.createReservation(status = ReservationStatus.CANCELLED)

        // when & then
        // PENDING -> CONFIRMED 가능
        pendingReservation.confirm()
        assertEquals(ReservationStatus.CONFIRMED, pendingReservation.status)

        // CONFIRMED -> COMPLETED 가능
        confirmedReservation.complete("test-payment-id")
        assertEquals(ReservationStatus.COMPLETED, confirmedReservation.status)

        // COMPLETED -> REFUNDED 가능
        completedReservation.refund("환불 사유")
        assertEquals(ReservationStatus.REFUNDED, completedReservation.status)

        // CANCELLED 상태에서는 다른 상태로 변경 불가
        assertThrows(IllegalStateException::class.java) {
            cancelledReservation.confirm()
        }
    }
} 