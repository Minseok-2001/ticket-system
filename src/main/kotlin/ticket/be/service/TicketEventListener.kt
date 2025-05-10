package ticket.be.service

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import ticket.be.dto.TicketReservedEvent
import ticket.be.dto.TicketConfirmedEvent
import ticket.be.dto.TicketCancelledEvent
import org.slf4j.LoggerFactory

@Component
class TicketEventListener {
    
    private val logger = LoggerFactory.getLogger(TicketEventListener::class.java)
    
    @KafkaListener(topics = ["ticket-events"], groupId = "ticket-event-processor")
    fun processEvents(@Payload payload: Any) {
        when (payload) {
            is TicketReservedEvent -> {
                handleTicketReserved(payload)
            }
            is TicketConfirmedEvent -> {
                handleTicketConfirmed(payload)
            }
            is TicketCancelledEvent -> {
                handleTicketCancelled(payload)
            }
            else -> {
                logger.warn("Unknown event type: ${payload::class.java.name}")
            }
        }
    }
    
    private fun handleTicketReserved(event: TicketReservedEvent) {
        logger.info("Ticket reserved: ${event.ticketId} for user: ${event.userId}")
        // 여기서 읽기 전용 모델 업데이트
        // 알림 서비스 호출 등
    }
    
    private fun handleTicketConfirmed(event: TicketConfirmedEvent) {
        logger.info("Ticket confirmed: ${event.ticketId} for user: ${event.userId}")
        // 여기서 읽기 전용 모델 업데이트
    }
    
    private fun handleTicketCancelled(event: TicketCancelledEvent) {
        logger.info("Ticket cancelled: ${event.ticketId} for user: ${event.userId}")
        // 여기서 읽기 전용 모델 업데이트
    }
} 