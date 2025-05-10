package ticket.be.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ticket.be.config.TestSecurityConfig
import ticket.be.dto.DeviceTokenRequest
import ticket.be.dto.NotificationDetailResponse
import ticket.be.dto.NotificationListResponse
import ticket.be.dto.NotificationSummaryDto
import ticket.be.service.NotificationCommandService
import ticket.be.service.NotificationQueryService
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@WebMvcTest(NotificationController::class)
@Import(TestSecurityConfig::class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["spring.jpa.hibernate.ddl-auto=none", "spring.flyway.enabled=false"])
class NotificationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var notificationCommandService: NotificationCommandService

    @MockitoBean
    private lateinit var notificationQueryService: NotificationQueryService

    @Test
    @WithMockUser(username = "user@example.com")
    fun `getNotifications should return notification list`() {
        val email = "user@example.com"
        val page = 0
        val size = 10

        val notification1 = NotificationSummaryDto(
            id = 1L,
            type = "TICKET_RESERVED",
            title = "티켓 예약 완료",
            content = "VIP-A3 좌석이 예약되었습니다.",
            link = "/reservations/3",
            status = "SENT",
            sentAt = LocalDateTime.now(),
            readAt = null,
            isRead = false
        )

        val notification2 = NotificationSummaryDto(
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

        val response = NotificationListResponse(
            notifications = listOf(notification1, notification2),
            totalElements = 2,
            totalPages = 1,
            currentPage = 0,
            size = 10,
            hasNext = false
        )

        `when`(notificationQueryService.getNotifications(email, page, size)).thenReturn(response)

        mockMvc.perform(
            get("/api/notifications")
                .param("page", page.toString())
                .param("size", size.toString())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.notifications.length()").value(2))
            .andExpect(jsonPath("$.notifications[0].id").value(1))
            .andExpect(jsonPath("$.notifications[0].type").value("TICKET_RESERVED"))
            .andExpect(jsonPath("$.notifications[1].id").value(2))
            .andExpect(jsonPath("$.notifications[1].type").value("PAYMENT_COMPLETED"))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    @WithMockUser(username = "user@example.com")
    fun `getNotification should return notification detail`() {
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

        `when`(notificationQueryService.getNotificationDetail(email, id)).thenReturn(
            notificationDetail
        )

        mockMvc.perform(get("/api/notifications/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.type").value("TICKET_RESERVED"))
            .andExpect(jsonPath("$.title").value("티켓 예약 완료"))
            .andExpect(jsonPath("$.content").value("VIP-A3 좌석이 예약되었습니다."))
    }

    @Test
    @WithMockUser(username = "user@example.com")
    fun `registerDeviceToken should register device token`() {
        val email = "user@example.com"
        val request = DeviceTokenRequest(deviceToken = "device-token-123")

        mockMvc.perform(
            post("/api/notifications/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)

        verify(notificationCommandService).registerDeviceToken(email, request.deviceToken)
    }

    @Test
    @WithMockUser(username = "user@example.com")
    fun `markAsRead should mark notification as read`() {
        val email = "user@example.com"
        val id = 1L

        mockMvc.perform(
            patch("/api/notifications/{id}/read", id)
                .with(csrf())
        )
            .andExpect(status().isOk)

        verify(notificationCommandService).markAsRead(email, id)
    }
} 