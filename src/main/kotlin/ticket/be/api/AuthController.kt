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
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        logger.info("로그인 요청: email={}", request.email)
        return ResponseEntity.ok(authService.login(request))
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    fun getMyInfo(@CurrentMember email: String): ResponseEntity<MemberResponse> {
        logger.info("내 정보 조회: email={}", email)
        return ResponseEntity.ok(authService.getMemberByEmail(email))
    }
} 