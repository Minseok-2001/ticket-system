package ticket.be.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import ticket.be.domain.Member
import ticket.be.domain.MemberRole
import ticket.be.domain.Notification
import ticket.be.domain.NotificationStatus
import ticket.be.dto.NotificationEvent
import ticket.be.repository.MemberRepository
import ticket.be.repository.NotificationRepository
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class NotificationCommandServiceTest {

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var notificationRepository: NotificationRepository

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>
    
    @Mock
    private lateinit var objectMapper: ObjectMapper

    @InjectMocks
    private lateinit var notificationCommandService: NotificationCommandService

    @Captor
    private lateinit var memberCaptor: ArgumentCaptor<Member>

    @Captor
    private lateinit var notificationCaptor: ArgumentCaptor<Notification>
    
    @Captor
    private lateinit var kafkaTopicCaptor: ArgumentCaptor<String>
    
    @Captor
    private lateinit var kafkaMessageCaptor: ArgumentCaptor<String>

    @Test
    fun `registerDeviceToken should update member's device token`() {
        // given
        val email = "user@example.com"
        val deviceToken = "device-token-123"
        val member = Member(
            id = 1L,
            email = email,
            password = "password",
            name = "Test User",
            deviceToken = null
        )
        
        `when`(memberRepository.findByEmail(email)).thenReturn(member)
        
        // when
        notificationCommandService.registerDeviceToken(email, deviceToken)
        
        // then
        verify(memberRepository).save(capture(memberCaptor))
        assertEquals(deviceToken, memberCaptor.value.deviceToken)
    }
    
    @Test
    fun `registerDeviceToken should throw exception when member not found`() {
        // given
        val email = "nonexistent@example.com"
        val deviceToken = "device-token-123"
        
        `when`(memberRepository.findByEmail(email)).thenReturn(null)
        
        // when, then
        assertFailsWith<IllegalArgumentException> {
            notificationCommandService.registerDeviceToken(email, deviceToken)
        }
    }
    
    @Test
    fun `markAsRead should update notification status`() {
        // given
        val email = "user@example.com"
        val notificationId = 1L
        val notification = Notification(
            id = notificationId,
            member = Member(id = 1L, email = email, password = "password", name = "Test User"),
            type = "TEST",
            title = "Test Notification",
            content = "This is a test notification",
            status = NotificationStatus.SENT
        )
        
        `when`(notificationRepository.findByMemberEmailAndId(email, notificationId)).thenReturn(notification)
        
        // when
        notificationCommandService.markAsRead(email, notificationId)
        
        // then
        verify(notificationRepository).save(capture(notificationCaptor))
        assertEquals(NotificationStatus.READ, notificationCaptor.value.status)
    }
    
    @Test
    fun `markAsRead should throw exception when notification not found`() {
        // given
        val email = "user@example.com"
        val notificationId = 1L
        
        `when`(notificationRepository.findByMemberEmailAndId(email, notificationId)).thenReturn(null)
        
        // when, then
        assertFailsWith<IllegalArgumentException> {
            notificationCommandService.markAsRead(email, notificationId)
        }
    }
    
    @Test
    fun `sendNotification should publish event to Kafka`() {
        // given
        val memberId = 1L
        val type = "TEST"
        val title = "Test Notification"
        val content = "This is a test notification"
        val link = "/test/123"
        
        val member = Member(
            id = memberId,
            email = "user@example.com",
            password = "password",
            name = "Test User"
        )
        
        val eventJson = """{"memberId":1,"type":"TEST","title":"Test Notification","content":"This is a test notification","link":"/test/123"}"""
        
        `when`(memberRepository.findById(memberId)).thenReturn(Optional.of(member))
        `when`(objectMapper.writeValueAsString(any(NotificationEvent::class.java))).thenReturn(eventJson)
        
        // when
        notificationCommandService.sendNotification(memberId, type, title, content, link)
        
        // then
        verify(kafkaTemplate).send(capture(kafkaTopicCaptor), capture(kafkaMessageCaptor))
        assertEquals("notification-events", kafkaTopicCaptor.value)
        assertEquals(eventJson, kafkaMessageCaptor.value)
    }
    
    @Test
    fun `sendNotification should throw exception when member not found`() {
        // given
        val memberId = 1L
        val type = "TEST"
        val title = "Test Notification"
        val content = "This is a test notification"
        
        `when`(memberRepository.findById(memberId)).thenReturn(Optional.empty())
        
        // when, then
        assertFailsWith<IllegalArgumentException> {
            notificationCommandService.sendNotification(memberId, type, title, content)
        }
    }
} 