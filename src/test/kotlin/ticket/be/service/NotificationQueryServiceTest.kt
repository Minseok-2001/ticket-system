package ticket.be.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import ticket.be.dto.NotificationDetailResponse
import ticket.be.dto.NotificationSummaryDto
import ticket.be.repository.NotificationQueryRepository
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class NotificationQueryServiceTest {

    @Mock
    private lateinit var notificationQueryRepository: NotificationQueryRepository

    @InjectMocks
    private lateinit var notificationQueryService: NotificationQueryService

    @Test
    fun `getNotifications should return NotificationListResponse with correct values`() {
        // given
        val email = "user@example.com"
        val page = 0
        val size = 10
        val pageable = PageRequest.of(page, size)
        
        val notifications = listOf(
            NotificationSummaryDto(
                id = 1L,
                type = "TICKET_RESERVED",
                title = "티켓 예약 완료",
                content = "VIP-A3 좌석이 예약되었습니다.",
                link = "/reservations/3",
                status = "SENT",
                sentAt = LocalDateTime.now(),
                readAt = null,
                isRead = false
            ),
            NotificationSummaryDto(
                id = 2L,
                type = "PAYMENT_COMPLETED",
                title = "결제 완료",
                content = "결제가 성공적으로 완료되었습니다.",
                link = "/tickets/10",
                status = "SENT",
                sentAt = LocalDateTime.now(),
                readAt = LocalDateTime.now(),
                isRead = true
            )
        )
        
        val pageResult = PageImpl(notifications, pageable, 2)
        
        `when`(notificationQueryRepository.findNotificationsByEmail(email, pageable))
            .thenReturn(pageResult)
        
        // when
        val result = notificationQueryService.getNotifications(email, page, size)
        
        // then
        assertEquals(2, result.notifications.size)
        assertEquals(2, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals(0, result.currentPage)
        assertEquals(10, result.size)
        assertEquals(false, result.hasNext)
        
        assertEquals("TICKET_RESERVED", result.notifications[0].type)
        assertEquals("PAYMENT_COMPLETED", result.notifications[1].type)
    }
    
    @Test
    fun `getNotificationDetail should return notification detail when found`() {
        // given
        val email = "user@example.com"
        val id = 1L
        
        val notificationDetail = NotificationDetailResponse(
            id = id,
            type = "TICKET_RESERVED",
            title = "티켓 예약 완료",
            content = "VIP-A3 좌석이 예약되었습니다.",
            link = "/reservations/3",
            status = "SENT",
            sentAt = LocalDateTime.now(),
            readAt = null,
            createdAt = LocalDateTime.now(),
            errorMessage = null
        )
        
        `when`(notificationQueryRepository.findNotificationDetailByEmailAndId(email, id))
            .thenReturn(notificationDetail)
        
        // when
        val result = notificationQueryService.getNotificationDetail(email, id)
        
        // then
        assertEquals(id, result.id)
        assertEquals("TICKET_RESERVED", result.type)
        assertEquals("티켓 예약 완료", result.title)
    }
    
    @Test
    fun `getNotificationDetail should throw exception when notification not found`() {
        // given
        val email = "user@example.com"
        val id = 999L
        
        `when`(notificationQueryRepository.findNotificationDetailByEmailAndId(email, id))
            .thenReturn(null)
        
        // when & then
        assertFailsWith<IllegalArgumentException> {
            notificationQueryService.getNotificationDetail(email, id)
        }
    }
} 