package ticket.be.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.Reservation
import ticket.be.domain.ReservationStatus
import ticket.be.domain.Ticket
import ticket.be.domain.TicketStatus
import ticket.be.dto.TicketEvent
import ticket.be.repository.*
import java.time.LocalDateTime
import java.util.*

@Service
class TicketEventListener(
    private val ticketRepository: TicketRepository,
    private val ticketTypeRepository: TicketTypeRepository,
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val reservationRepository: ReservationRepository,
    private val ticketStockService: TicketStockService,
    private val notificationCommandService: NotificationCommandService,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(TicketEventListener::class.java)

    /**
     * 티켓 이벤트 수신 및 처리
     */
    @KafkaListener(topics = ["ticket-events"], groupId = "ticket-consumer")
    @Transactional
    fun processTicketEvent(message: String) {
        try {
            logger.info("티켓 이벤트 수신: {}", message)

            val event = objectMapper.readValue(message, TicketEvent::class.java)

            when (event.eventType) {
                "RESERVE_TICKET" -> processReserveTicket(event)
                "CANCEL_TICKET" -> processCancelTicket(event)
                else -> logger.warn("알 수 없는 이벤트 타입: {}", event.eventType)
            }
        } catch (e: Exception) {
            logger.error("티켓 이벤트 처리 실패: {}", e.message, e)
        }
    }

    /**
     * 티켓 예매 이벤트 처리
     */
    @Transactional
    fun processReserveTicket(event: TicketEvent) {
        logger.info(
            "티켓 예매 이벤트 처리: eventId={}, memberId={}, quantity={}",
            event.eventId, event.memberId, event.quantity
        )

        try {
            // 엔티티 조회
            val eventEntity = eventRepository.findById(event.eventId)
                .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다: id=${event.eventId}") }

            val member = memberRepository.findById(event.memberId)
                .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다: id=${event.memberId}") }

            val ticketType = ticketTypeRepository.findById(event.ticketTypeId)
                .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: id=${event.ticketTypeId}") }

            // 티켓 생성 (하나씩 처리하도록 단순화)
            val seatNumber = generateSeatNumber(event.ticketTypeId)
            val ticket = Ticket(
                event = eventEntity,
                ticketType = ticketType,
                seatNumber = seatNumber,
                price = ticketType.price,
                status = TicketStatus.RESERVED,
                reservedByMember = member,
                reservedAt = LocalDateTime.now()
            )

            val savedTicket = ticketRepository.save(ticket)

            // 예약 생성
            val reservation = Reservation(
                member = member,
                event = eventEntity,
                ticket = savedTicket,
                totalAmount = ticket.price,
                status = ReservationStatus.PENDING,
                paymentMethod = event.paymentMethod ?: "CREDIT_CARD"
            )

            val savedReservation = reservationRepository.save(reservation)

            // Redis의 임시 재고를 DB에 확정
            ticketStockService.commitStock(event.ticketTypeId, event.quantity)

            // 알림 전송
            notificationCommandService.sendNotification(
                memberId = event.memberId,
                type = "RESERVATION_CREATED",
                title = "${eventEntity.name} 예매 접수 완료",
                content = "${ticketType.name} 좌석이 예매 접수되었습니다. 30분 이내에 결제를 완료해주세요.",
                link = "/reservations/${savedReservation.id}"
            )

            logger.info(
                "티켓 예매 처리 성공: reservationId={}, ticketId={}",
                savedReservation.id, savedTicket.id
            )

        } catch (e: Exception) {
            logger.error("티켓 예매 처리 중 오류 발생: {}", e.message, e)

            // 오류 발생 시 재고 복구
            try {
                ticketStockService.restoreStock(event.ticketTypeId, event.quantity)
            } catch (restoreEx: Exception) {
                logger.error("재고 복구 중 오류 발생: {}", restoreEx.message, restoreEx)
            }

            // 실패 알림 전송
            notificationCommandService.sendNotification(
                memberId = event.memberId,
                type = "RESERVATION_FAILED",
                title = "예매 처리 실패",
                content = "예매 처리 중 오류가 발생했습니다. 다시 시도해주세요."
            )

            throw IllegalStateException("티켓 예매 처리 실패: ${e.message}")
        }
    }

    /**
     * 티켓 취소 이벤트 처리
     */
    @Transactional
    fun processCancelTicket(event: TicketEvent) {
        logger.info("티켓 취소 이벤트 처리: reservationId={}", event.reservationId)

        try {
            if (event.reservationId == null) {
                throw IllegalArgumentException("예약 ID가 없습니다.")
            }

            val reservation = reservationRepository.findById(event.reservationId)
                .orElseThrow { IllegalArgumentException("예약을 찾을 수 없습니다: id=${event.reservationId}") }

            val ticket = reservation.ticket

            // 상태 확인
            if (reservation.status != ReservationStatus.PENDING &&
                reservation.status != ReservationStatus.CONFIRMED
            ) {
                throw IllegalStateException("취소할 수 없는 예약 상태입니다: ${reservation.status}")
            }

            // 예약 및 티켓 상태 변경
            reservation.cancel("사용자 요청에 의한 취소")
            ticket?.cancel()

            if (ticket == null) {
                throw IllegalStateException("티켓을 찾을 수 없습니다.")
            }
            reservationRepository.save(reservation)
            ticketRepository.save(ticket)

            // 재고 복구
            ticketStockService.restoreStock(ticket.ticketType.id)

            // 알림 전송
            notificationCommandService.sendNotification(
                memberId = reservation.member.id,
                type = "RESERVATION_CANCELLED",
                title = "예매 취소 완료",
                content = "${reservation.event.name} 예매가 취소되었습니다.",
                link = "/reservations/${reservation.id}"
            )

            logger.info(
                "티켓 취소 처리 성공: reservationId={}, ticketId={}",
                reservation.id, ticket.id
            )

        } catch (e: Exception) {
            logger.error("티켓 취소 처리 중 오류 발생: {}", e.message, e)
            throw IllegalStateException("티켓 취소 처리 실패: ${e.message}")
        }
    }

    /**
     * 임시 좌석 번호 생성 (실제 구현에서는 좌석 배치도에 따라 구현)
     */
    private fun generateSeatNumber(ticketTypeId: Long): String {
        // 실제 구현에서는 좌석 배치도 및 예매 가능 좌석을 조회하여 처리
        val random = Random()
        val section = when (ticketTypeId % 3) {
            0L -> "A"
            1L -> "B"
            else -> "C"
        }
        val row = random.nextInt(20) + 1
        val col = random.nextInt(30) + 1

        return "$section-$row-$col"
    }
} 