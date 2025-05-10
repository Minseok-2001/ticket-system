package ticket.be.service

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.dto.*
import ticket.be.repository.MemberRepository
import ticket.be.repository.TicketRepository
import java.time.LocalDateTime
import java.util.*

@Service
class TicketCommandService(
    private val ticketRepository: TicketRepository,
    private val memberRepository: MemberRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    // 비동기 처리 - Kafka로 명령 발행
    fun reserveTicket(command: ReserveTicketCommand) {
        // 명령 ID 생성 및 Kafka로 명령 발행
        val commandId = UUID.randomUUID().toString()
        kafkaTemplate.send("ticket-commands", commandId, command)
    }

    fun confirmTicket(command: ConfirmTicketCommand) {
        val commandId = UUID.randomUUID().toString()
        kafkaTemplate.send("ticket-commands", commandId, command)
    }

    fun cancelTicket(command: CancelTicketCommand) {
        val commandId = UUID.randomUUID().toString()
        kafkaTemplate.send("ticket-commands", commandId, command)
    }

    // 명령 처리 - Kafka 리스너에서 호출
    @Transactional
    fun processReserveTicket(command: ReserveTicketCommand) {
        // 단순화를 위해 1개씩만 처리
        val ticket = ticketRepository.findFirstAvailableTicketForEvent(command.eventId)
            .orElseThrow { IllegalStateException("No available tickets for event ${command.eventId}") }
        
        val member = memberRepository.findById(command.memberId)
            .orElseThrow { IllegalStateException("Member not found: ${command.memberId}") }

        if (ticket.reserve(member)) {
            ticketRepository.save(ticket)

            // 이벤트 발행
            val event = TicketReservedEvent(
                ticketId = ticket.id,
                memberId = command.memberId,
                eventId = command.eventId,
                reservedAt = LocalDateTime.now()
            )
            kafkaTemplate.send("ticket-events", ticket.id.toString(), event)
        }
    }

    @Transactional
    fun processConfirmTicket(command: ConfirmTicketCommand) {
        val ticket = ticketRepository.findByIdForUpdate(command.ticketId)
            .orElseThrow { IllegalStateException("Ticket not found: ${command.ticketId}") }

        // 예약한 사용자만 확정 가능
        if (ticket.reservedByMember?.id != command.memberId) {
            throw IllegalStateException("Ticket not reserved by member: ${command.memberId}")
        }

        if (ticket.confirm()) {
            ticketRepository.save(ticket)

            // 이벤트 발행
            val event = TicketConfirmedEvent(
                ticketId = ticket.id,
                memberId = command.memberId,
                confirmedAt = LocalDateTime.now()
            )
            kafkaTemplate.send("ticket-events", ticket.id.toString(), event)
        }
    }

    @Transactional
    fun processCancelTicket(command: CancelTicketCommand) {
        val ticket = ticketRepository.findByIdForUpdate(command.ticketId)
            .orElseThrow { IllegalStateException("Ticket not found: ${command.ticketId}") }

        // 예약한 사용자만 취소 가능
        if (ticket.reservedByMember?.id != command.memberId) {
            throw IllegalStateException("Ticket not reserved by member: ${command.memberId}")
        }

        if (ticket.cancel()) {
            ticketRepository.save(ticket)

            // 이벤트 발행
            val event = TicketCancelledEvent(
                ticketId = ticket.id,
                memberId = command.memberId,
                cancelledAt = LocalDateTime.now()
            )
            kafkaTemplate.send("ticket-events", ticket.id.toString(), event)
        }
    }
} 