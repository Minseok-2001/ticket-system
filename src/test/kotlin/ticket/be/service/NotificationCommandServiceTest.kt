package ticket.be.service

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

    @InjectMocks
    private lateinit var notificationCommandService: NotificationCommandService

    @Captor
    private lateinit var memberCaptor: ArgumentCaptor<Member>

    @Captor
    private lateinit var notificationCaptor: ArgumentCaptor<Notification>

    @Test
    fun `registerDeviceToken should update member device token`() {
        val email = "user@example.com"
        val deviceToken = "device-token-123"
        
        val member = Member(
            id = 1L,
            email = email,
            password = "password",
            name = "Test User",
            memberRole = MemberRole.USER
        )
        
        `when`(memberRepository.findByEmail(email)).thenReturn(member)
        
        notificationCommandService.registerDeviceToken(email, deviceToken)
        
        verify(memberRepository).findByEmail(email)
        verify(memberRepository).save(memberCaptor.capture())
        
        val savedMember = memberCaptor.value
        assertEquals(deviceToken, savedMember.deviceToken)
    }
    
    @Test
    fun `registerDeviceToken should throw exception when member not found`() {
        val email = "nonexistent@example.com"
        val deviceToken = "device-token-123"
        
        `when`(memberRepository.findByEmail(email)).thenReturn(null)
        
        assertFailsWith<IllegalArgumentException> {
            notificationCommandService.registerDeviceToken(email, deviceToken)
        }
    }
    
    @Test
    fun `markAsRead should mark notification as read`() {
        val email = "user@example.com"
        val notificationId = 1L
        
        val member = Member(
            id = 1L,
            email = email,
            password = "password",
            name = "Test User",
            memberRole = MemberRole.USER
        )
        
        val notification = Notification(
            id = notificationId,
            member = member,
            type = "TICKET_RESERVED",
            title = "티켓 예약 완료",
            content = "VIP-A3 좌석이 예약되었습니다.",
            status = NotificationStatus.SENT
        )
        
        `when`(notificationRepository.findByMemberEmailAndId(email, notificationId)).thenReturn(notification)
        
        notificationCommandService.markAsRead(email, notificationId)
        
        verify(notificationRepository).findByMemberEmailAndId(email, notificationId)
        verify(notificationRepository).save(notificationCaptor.capture())
        
        val savedNotification = notificationCaptor.value
        assertEquals(NotificationStatus.READ, savedNotification.status)
    }
    
    @Test
    fun `markAsRead should throw exception when notification not found`() {
        val email = "user@example.com"
        val notificationId = 999L
        
        `when`(notificationRepository.findByMemberEmailAndId(email, notificationId)).thenReturn(null)
        
        assertFailsWith<IllegalArgumentException> {
            notificationCommandService.markAsRead(email, notificationId)
        }
    }
    
    @Test
    fun `sendNotification should throw exception when member not found`() {
        val memberId = 999L
        
        `when`(memberRepository.findById(memberId)).thenReturn(Optional.empty())
        
        assertFailsWith<IllegalArgumentException> {
            notificationCommandService.sendNotification(
                memberId = memberId,
                type = "TEST",
                title = "Test Title",
                content = "Test Content"
            )
        }
    }
} 