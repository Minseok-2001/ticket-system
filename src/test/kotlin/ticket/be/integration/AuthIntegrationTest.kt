package ticket.be.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import ticket.be.dto.LoginRequest
import ticket.be.dto.SignupRequest
import ticket.be.repository.MemberRepository
import ticket.be.util.TestDataFactory

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("인증 API 통합 테스트")
class AuthIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("회원가입 및 로그인 통합 테스트")
    fun signupAndLogin() {
        // 1. 회원가입 테스트
        val email = "integration-test@example.com"
        val password = "password123"
        val name = "통합테스트 사용자"
        val phone = "010-1234-5678"

        val signupRequest = SignupRequest(
            email = email,
            password = password,
            name = name,
            phone = phone
        )

        val signupResult = mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.name").value(name))
            .andReturn()

        // 2. 로그인 테스트
        val loginRequest = LoginRequest(
            email = email,
            password = password
        )

        val loginResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.sessionId").exists())
            .andReturn()

        // 3. 토큰을 이용한 인증 요청 테스트
        val response = objectMapper.readTree(loginResult.response.contentAsString)
        val token = response.get("accessToken").asText()
        val sessionId = response.get("sessionId").asText()

        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer $token")
                .header("X-Session-ID", sessionId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.name").value(name))

        // 4. 로그아웃 테스트
        mockMvc.perform(
            post("/api/auth/logout")
                .header("Authorization", "Bearer $token")
                .header("X-Session-ID", sessionId)
        )
            .andExpect(status().isOk)

        // 5. 로그아웃 후 인증 요청 테스트 (실패해야 함)
        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer $token")
                .header("X-Session-ID", sessionId)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패 테스트")
    fun loginFailWithWrongPassword() {
        // given
        val email = "integration-test-fail@example.com"
        val password = "password123"
        val wrongPassword = "wrongPassword123"
        val name = "실패 테스트 사용자"
        val phone = "010-9876-5432"

        val signupRequest = SignupRequest(
            email = email,
            password = password,
            name = name,
            phone = phone
        )

        // 1. 회원가입
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isOk)

        // 2. 잘못된 비밀번호로 로그인 시도
        val loginRequest = LoginRequest(
            email = email,
            password = wrongPassword
        )

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 실패 테스트")
    fun signupFailWithDuplicateEmail() {
        // given
        val email = "integration-test-duplicate@example.com"
        val password = "password123"
        val name = "중복 테스트 사용자"
        val phone = "010-1111-2222"

        val signupRequest = SignupRequest(
            email = email,
            password = password,
            name = name,
            phone = phone
        )

        // 1. 첫 번째 회원가입
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isOk)

        // 2. 같은 이메일로 두 번째 회원가입 시도
        mockMvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @DisplayName("인증되지 않은 요청 테스트")
    fun unauthenticatedRequest() {
        // 인증 없이 보호된 리소스 접근 시도
        mockMvc.perform(
            get("/api/auth/me")
        )
            .andExpect(status().isUnauthorized)
    }
} 