package ticket.be.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import ticket.be.BaseTest
import ticket.be.config.JwtConfig
import ticket.be.domain.Member
import ticket.be.dto.LoginRequest
import ticket.be.dto.SignupRequest
import ticket.be.repository.MemberRepository
import javax.servlet.http.HttpSession
import java.util.Optional

@ExtendWith(MockitoExtension::class)
@DisplayName("AuthService 테스트")
class AuthServiceTest : BaseTest() {

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var authenticationManager: AuthenticationManager

    @Mock
    private lateinit var jwtConfig: JwtConfig

    @Mock
    private lateinit var memberDetailsService: MemberDetailsService

    @Mock
    private lateinit var sessionService: SessionService

    @Mock
    private lateinit var httpSession: HttpSession

    @Mock
    private lateinit var authentication: Authentication

    @InjectMocks
    private lateinit var authService: AuthService

    private lateinit var testMember: Member

    @BeforeEach
    fun setUp() {
        testMember = testDataFactory.createMember()
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    fun signupSuccess() {
        // given
        val signupRequest = SignupRequest(
            email = TEST_EMAIL,
            password = TEST_PASSWORD,
            name = TEST_NAME,
            phone = TEST_PHONE
        )

        `when`(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(null)
        `when`(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encodedPassword")
        `when`(memberRepository.save(any(Member::class.java))).thenReturn(testMember)

        // when
        val result = authService.signup(signupRequest)

        // then
        verify(memberRepository).findByEmail(TEST_EMAIL)
        verify(passwordEncoder).encode(TEST_PASSWORD)
        verify(memberRepository).save(any(Member::class.java))
        
        assert(result.email == TEST_EMAIL)
        assert(result.name == TEST_NAME)
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 이미 존재하는 이메일")
    fun signupFailWithExistingEmail() {
        // given
        val signupRequest = SignupRequest(
            email = TEST_EMAIL,
            password = TEST_PASSWORD,
            name = TEST_NAME,
            phone = TEST_PHONE
        )

        `when`(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(testMember)

        // when & then
        assertThrows<IllegalArgumentException> {
            authService.signup(signupRequest)
        }
        
        verify(memberRepository).findByEmail(TEST_EMAIL)
        verify(memberRepository, never()).save(any(Member::class.java))
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    fun loginSuccess() {
        // given
        val loginRequest = LoginRequest(
            email = TEST_EMAIL,
            password = TEST_PASSWORD
        )
        
        val sessionId = "test-session-id"
        val token = "test-jwt-token"

        `when`(authenticationManager.authenticate(any())).thenReturn(authentication)
        `when`(memberDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(mock())
        `when`(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(testMember)
        `when`(jwtConfig.generateToken(any(), any())).thenReturn(token)
        `when`(httpSession.id).thenReturn(sessionId)

        // when
        val result = authService.login(loginRequest, httpSession)

        // then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken::class.java))
        verify(memberDetailsService).loadUserByUsername(TEST_EMAIL)
        verify(memberRepository).findByEmail(TEST_EMAIL)
        verify(jwtConfig).generateToken(any(), any())
        verify(sessionService).setAttribute(eq(sessionId), eq("memberId"), eq(testMember.id))
        verify(sessionService).setAttribute(eq(sessionId), eq("email"), eq(testMember.email))
        verify(sessionService).setAttribute(eq(sessionId), eq("role"), eq(testMember.memberRole.name))
        verify(sessionService).setAttribute(eq(sessionId), eq("authenticated"), eq(true))
        
        assert(result.accessToken == token)
        assert(result.sessionId == sessionId)
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 인증정보")
    fun loginFailWithInvalidCredentials() {
        // given
        val loginRequest = LoginRequest(
            email = TEST_EMAIL,
            password = "wrongPassword"
        )

        `when`(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException("잘못된 인증정보"))

        // when & then
        assertThrows<BadCredentialsException> {
            authService.login(loginRequest, httpSession)
        }
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken::class.java))
        verify(memberDetailsService, never()).loadUserByUsername(anyString())
        verify(sessionService, never()).setAttribute(anyString(), anyString(), any())
    }

    @Test
    @DisplayName("로그아웃 테스트")
    fun logout() {
        // given
        val sessionId = "test-session-id"

        // when
        authService.logout(sessionId)

        // then
        verify(sessionService).invalidateSession(sessionId)
    }

    @Test
    @DisplayName("이메일로 회원 조회 테스트")
    fun getMemberByEmail() {
        // given
        `when`(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(testMember)

        // when
        val result = authService.getMemberByEmail(TEST_EMAIL)

        // then
        verify(memberRepository).findByEmail(TEST_EMAIL)
        assert(result.email == TEST_EMAIL)
        assert(result.id == testMember.id)
    }

    @Test
    @DisplayName("세션 검증 테스트")
    fun validateSession() {
        // given
        val sessionId = "test-session-id"
        `when`(sessionService.getAttribute(sessionId, "authenticated")).thenReturn(true)

        // when
        val result = authService.validateSession(sessionId)

        // then
        verify(sessionService).getAttribute(sessionId, "authenticated")
        assert(result)
    }

    @Test
    @DisplayName("세션에서 회원 ID 조회 테스트")
    fun getMemberIdFromSession() {
        // given
        val sessionId = "test-session-id"
        val memberId = 1L
        `when`(sessionService.getMemberIdFromSession(sessionId)).thenReturn(memberId)

        // when
        val result = authService.getMemberIdFromSession(sessionId)

        // then
        verify(sessionService).getMemberIdFromSession(sessionId)
        assert(result == memberId)
    }

    @Test
    @DisplayName("세션 연장 테스트")
    fun extendSession() {
        // given
        val sessionId = "test-session-id"

        // when
        authService.extendSession(sessionId)

        // then
        verify(sessionService).extendSession(sessionId)
    }
} 