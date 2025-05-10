package ticket.be.service

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import ticket.be.dto.CancelTicketCommand
import ticket.be.dto.ConfirmTicketCommand
import ticket.be.dto.ReserveTicketCommand

@Component
class TicketCommandListener(
    private val ticketCommandService: TicketCommandService
) {

    @KafkaListener(topics = ["ticket-commands"], groupId = "ticket-command-processor")
    fun processCommands(@Payload payload: Any) {
        when (payload) {
            is ReserveTicketCommand -> {
                ticketCommandService.processReserveTicket(payload)
            }

            is ConfirmTicketCommand -> {
                ticketCommandService.processConfirmTicket(payload)
            }

            is CancelTicketCommand -> {
                ticketCommandService.processCancelTicket(payload)
            }

            else -> {
                throw IllegalArgumentException("Unknown command type: ${payload::class.java.name}")
            }
        }
    }
} 