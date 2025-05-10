package ticket.be.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ticket.be.config.CurrentMember
import ticket.be.dto.LoginRequest
import ticket.be.dto.MemberResponse
import ticket.be.dto.SignupRequest
import ticket.be.dto.TokenResponse
import ticket.be.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "인증 관련 API")
class AuthController(
    private val authService: AuthService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/signup")
    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다.")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<MemberResponse> {
        logger.info("회원가입 요청: email={}, name={}", request.email, request.name)
        return ResponseEntity.ok(authService.signup(request))
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰 및 세션을 생성합니다.")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpSession: HttpSession
    ): ResponseEntity<TokenResponse> {
        logger.info("로그인 요청: email={}", request.email)
        return ResponseEntity.ok(authService.login(request, httpSession))
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 세션을 만료시킵니다.")
    fun logout(
        httpSession: HttpSession,
        @RequestHeader(value = "X-Session-Id", required = false) sessionId: String?
    ): ResponseEntity<Map<String, String>> {
        val targetSessionId = sessionId ?: httpSession.id
        logger.info("로그아웃 요청: sessionId={}", targetSessionId)
        
        authService.logout(targetSessionId)
        httpSession.invalidate()
        
        return ResponseEntity.ok(mapOf("message" to "로그아웃 되었습니다."))
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    fun getMyInfo(
        @CurrentMember email: String,
        httpSession: HttpSession
    ): ResponseEntity<MemberResponse> {
        logger.info("내 정보 조회: email={}, sessionId={}", email, httpSession.id)
        
        // 세션 갱신
        authService.extendSession(httpSession.id)
        
        return ResponseEntity.ok(authService.getMemberByEmail(email))
    }
    
    @GetMapping("/validate-session")
    @Operation(summary = "세션 검증", description = "세션의 유효성을 검증합니다.")
    fun validateSession(
        @RequestHeader(value = "X-Session-Id", required = false) sessionId: String?,
        httpSession: HttpSession
    ): ResponseEntity<Map<String, Any>> {
        val targetSessionId = sessionId ?: httpSession.id
        logger.info("세션 검증 요청: sessionId={}", targetSessionId)
        
        val isValid = authService.validateSession(targetSessionId)
        
        if (isValid) {
            authService.extendSession(targetSessionId)
        }
        
        return ResponseEntity.ok(mapOf(
            "valid" to isValid,
            "sessionId" to targetSessionId
        ))
    }
} 