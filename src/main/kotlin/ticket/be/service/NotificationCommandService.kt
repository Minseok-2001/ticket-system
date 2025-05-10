package ticket.be.service

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.dto.NotificationEvent
import ticket.be.repository.MemberRepository
import ticket.be.repository.NotificationRepository
import java.time.LocalDateTime

@Service
class NotificationCommandService(
    private val memberRepository: MemberRepository,
    private val notificationRepository: NotificationRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(NotificationCommandService::class.java)
    
    @Transactional
    fun registerDeviceToken(email: String, deviceToken: String) {
        val member = memberRepository.findByEmail(email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        
        member.updateDeviceToken(deviceToken)
        memberRepository.save(member)
        logger.info("디바이스 토큰 등록 완료: memberId={}, deviceToken={}", member.id, deviceToken)
    }
    
    @Transactional
    fun markAsRead(email: String, notificationId: Long) {
        val notification = notificationRepository.findByMemberEmailAndId(email, notificationId)
            ?: throw IllegalArgumentException("알림을 찾을 수 없습니다.")
        
        notification.markAsRead()
        notificationRepository.save(notification)
        logger.info("알림 읽음 처리 완료: notificationId={}", notificationId)
    }
    
    @Transactional
    fun sendNotification(memberId: Long, type: String, title: String, content: String, link: String? = null) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 알림 이벤트 발행 (Kafka 메시지 큐로 전송)
        val notificationEvent = NotificationEvent(
            memberId = memberId,
            type = type,
            title = title,
            content = content,
            link = link
        )
        
        try {
            // JSON 직렬화 코드 추가 필요
            // kafkaTemplate.send("notification-events", ObjectMapper().writeValueAsString(notificationEvent))
            logger.info("알림 이벤트 발행: memberId={}, type={}", memberId, type)
        } catch (e: Exception) {
            logger.error("알림 이벤트 발행 실패: memberId={}, type={}, error={}", memberId, type, e.message, e)
        }
    }
} 