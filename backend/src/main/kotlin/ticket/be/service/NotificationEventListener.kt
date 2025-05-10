package ticket.be.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import ticket.be.domain.Member
import ticket.be.domain.Notification
import ticket.be.domain.NotificationStatus
import ticket.be.dto.NotificationEvent
import ticket.be.repository.MemberRepository
import ticket.be.repository.NotificationRepository

@Component
class NotificationEventListener(
    private val notificationRepository: NotificationRepository,
    private val memberRepository: MemberRepository,
    private val objectMapper: ObjectMapper,
    private val snsClient: SnsClient
) {
    private val logger = LoggerFactory.getLogger(NotificationEventListener::class.java)
    
    @KafkaListener(topics = ["notification-events"], groupId = "notification-consumer")
    @Transactional
    fun handleNotificationEvent(message: String) {
        try {
            logger.info("알림 이벤트 수신: {}", message)
            val event = objectMapper.readValue(message, NotificationEvent::class.java)
            processNotificationEvent(event)
        } catch (e: Exception) {
            logger.error("알림 이벤트 처리 중 오류 발생: {}", e.message, e)
        }
    }
    
    private fun processNotificationEvent(event: NotificationEvent) {
        val member = memberRepository.findById(event.memberId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: id=${event.memberId}") }
        
        // 알림 엔티티 생성 및 저장
        val notification = createNotification(member, event)
        notificationRepository.save(notification)
        
        // 디바이스 토큰이 있으면 푸시 알림 전송
        member.deviceToken?.let { token ->
            try {
                sendPushNotification(token, notification)
                notification.markAsSent()
            } catch (e: Exception) {
                logger.error("푸시 알림 전송 실패: notificationId={}, error={}", notification.id, e.message, e)
                notification.markAsFailed(e.message ?: "푸시 알림 전송 실패")
            }
        }
        
        notificationRepository.save(notification)
    }
    
    private fun createNotification(member: Member, event: NotificationEvent): Notification {
        return Notification(
            member = member,
            type = event.type,
            title = event.title,
            content = event.content,
            link = event.link,
            status = NotificationStatus.PENDING
        )
    }
    
    private fun sendPushNotification(deviceToken: String, notification: Notification) {
        // AWS SNS를 사용하여 푸시 알림 전송
        val message = objectMapper.writeValueAsString(
            mapOf(
                "default" to notification.content,
                "APNS" to objectMapper.writeValueAsString(
                    mapOf(
                        "aps" to mapOf(
                            "alert" to mapOf(
                                "title" to notification.title,
                                "body" to notification.content
                            ),
                            "sound" to "default"
                        ),
                        "notificationId" to notification.id,
                        "type" to notification.type,
                        "link" to (notification.link ?: "")
                    )
                ),
                "GCM" to objectMapper.writeValueAsString(
                    mapOf(
                        "notification" to mapOf(
                            "title" to notification.title,
                            "body" to notification.content
                        ),
                        "data" to mapOf(
                            "notificationId" to notification.id,
                            "type" to notification.type,
                            "link" to (notification.link ?: "")
                        )
                    )
                )
            )
        )
        
        val request = PublishRequest.builder()
            .targetArn(deviceToken)
            .message(message)
            .messageStructure("json")
            .build()
        
        snsClient.publish(request)
        logger.info("푸시 알림 전송 완료: notificationId={}, deviceToken={}", notification.id, deviceToken)
    }
} 