package ticket.be.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.dto.*
import ticket.be.repository.EventRepository
import ticket.be.repository.MemberRepository
import ticket.be.repository.TicketTypeRepository

@Service
class TicketCommandService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val ticketTypeRepository: TicketTypeRepository,
    private val ticketStockService: TicketStockService
) {
    private val logger = LoggerFactory.getLogger(TicketCommandService::class.java)
    
    /**
     * 티켓 예매 요청을 처리하고 Kafka로 이벤트 발행
     */
    @Transactional
    fun reserveTicket(command: ReserveTicketCommand) {
        logger.info("티켓 예매 요청: eventId={}, ticketTypeId={}, memberId={}, quantity={}", 
            command.eventId, command.ticketTypeId, command.memberId, command.quantity)
        
        // 유효성 검증
        val event = eventRepository.findById(command.eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다: id=${command.eventId}") }
        
        val member = memberRepository.findById(command.memberId)
            .orElseThrow { IllegalArgumentException("회원을 찾을 수 없습니다: id=${command.memberId}") }
        
        val ticketType = ticketTypeRepository.findById(command.ticketTypeId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: id=${command.ticketTypeId}") }
        
        // 재고 확인 및 감소 (Redis)
        try {
            val success = ticketStockService.tryDecreaseStock(command.ticketTypeId, command.quantity)
            if (!success) {
                throw IllegalStateException("재고 감소에 실패했습니다.")
            }
        } catch (e: Exception) {
            logger.error("재고 감소 실패: ticketTypeId={}, error={}", command.ticketTypeId, e.message)
            throw IllegalStateException("재고 처리 중 오류가 발생했습니다: ${e.message}")
        }
        
        // Kafka로 이벤트 발행
        val ticketEvent = TicketEvent(
            eventType = "RESERVE_TICKET",
            eventId = command.eventId,
            ticketTypeId = command.ticketTypeId,
            memberId = command.memberId,
            quantity = command.quantity,
            paymentMethod = command.paymentMethod,
            status = "PENDING"
        )
        
        try {
            val eventJson = objectMapper.writeValueAsString(ticketEvent)
            kafkaTemplate.send("ticket-events", eventJson)
            logger.info("티켓 예매 이벤트 발행 완료: eventId={}, memberId={}", command.eventId, command.memberId)
        } catch (e: Exception) {
            // Kafka 발행 실패 시 재고 원복
            logger.error("티켓 예매 이벤트 발행 실패: error={}", e.message, e)
            ticketStockService.restoreStock(command.ticketTypeId, command.quantity)
            throw IllegalStateException("티켓 예매 이벤트 발행에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 티켓 예매 취소 요청을 처리하고 Kafka로 이벤트 발행
     */
    @Transactional
    fun cancelTicket(command: CancelTicketCommand) {
        logger.info("티켓 취소 요청: reservationId={}", command.reservationId)
        
        // Kafka로 이벤트 발행
        val ticketEvent = mapOf(
            "eventType" to "CANCEL_TICKET",
            "timestamp" to System.currentTimeMillis(),
            "reservationId" to command.reservationId,
            "reason" to (command.reason ?: "사용자 요청"),
            "status" to "PENDING"
        )
        
        try {
            val eventJson = objectMapper.writeValueAsString(ticketEvent)
            kafkaTemplate.send("ticket-commands", eventJson)
            logger.info("티켓 취소 이벤트 발행 완료: reservationId={}", command.reservationId)
        } catch (e: Exception) {
            logger.error("티켓 취소 이벤트 발행 실패: error={}", e.message, e)
            throw IllegalStateException("티켓 취소 이벤트 발행에 실패했습니다: ${e.message}")
        }
    }
    
    /**
     * 예약 티켓 처리 (Kafka Listener에서 호출)
     */
    @Transactional
    fun processReserveTicket(command: ReserveTicketCommand) {
        logger.info("예약 티켓 처리: eventId={}, memberId={}", command.eventId, command.memberId)
        // 실제 티켓 예약 처리 구현
    }
    
    /**
     * 티켓 확인 처리 (Kafka Listener에서 호출)
     */
    @Transactional
    fun processConfirmTicket(command: ConfirmTicketCommand) {
        logger.info("티켓 확인 처리: reservationId={}", command.reservationId)
        // 실제 티켓 확인 처리 구현
    }
    
    /**
     * 티켓 취소 처리 (Kafka Listener에서 호출)
     */
    @Transactional
    fun processCancelTicket(command: CancelTicketCommand) {
        logger.info("티켓 취소 처리: reservationId={}", command.reservationId)
        // 실제 티켓 취소 처리 구현
    }
} 