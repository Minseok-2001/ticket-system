package ticket.be.service

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.config.JwtConfig
import ticket.be.domain.Member
import ticket.be.domain.MemberRole
import ticket.be.dto.LoginRequest
import ticket.be.dto.MemberResponse
import ticket.be.dto.SignupRequest
import ticket.be.dto.TokenResponse
import ticket.be.repository.MemberRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtConfig: JwtConfig,
    private val memberDetailsService: MemberDetailsService,
    private val sessionService: SessionService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    fun signup(request: SignupRequest): MemberResponse {
        if (memberRepository.findByEmail(request.email) != null) {
            throw IllegalArgumentException("이미 등록된 이메일입니다.")
        }
        
        val member = Member(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            phone = request.phone,
            memberRole = MemberRole.USER
        )
        
        val savedMember = memberRepository.save(member)
        
        return MemberResponse(
            id = savedMember.id,
            email = savedMember.email,
            name = savedMember.name,
            phone = savedMember.phone,
            role = savedMember.memberRole.name
        )
    }
    
    fun login(request: LoginRequest, httpSession: HttpSession): TokenResponse {
        // 인증 시도
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.email,
                request.password
            )
        )
        
        // 인증 성공 시 JWT 토큰 발급
        val userDetails = memberDetailsService.loadUserByUsername(request.email)
        val member = memberRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        
        val claims = mapOf(
            "id" to member.id,
            "role" to member.memberRole.name
        )
        
        val token = jwtConfig.generateToken(userDetails, claims)
        
        // 세션에 사용자 정보 저장
        val sessionId = httpSession.id
        sessionService.setAttribute(sessionId, "memberId", member.id)
        sessionService.setAttribute(sessionId, "email", member.email)
        sessionService.setAttribute(sessionId, "role", member.memberRole.name)
        sessionService.setAttribute(sessionId, "authenticated", true)
        
        logger.info("사용자 로그인 및 세션 생성: email={}, sessionId={}", member.email, sessionId)
        
        return TokenResponse(
            accessToken = token,
            sessionId = sessionId
        )
    }
    
    fun logout(sessionId: String) {
        sessionService.invalidateSession(sessionId)
        logger.info("사용자 로그아웃 및 세션 무효화: sessionId={}", sessionId)
    }
    
    fun getMemberByEmail(email: String): MemberResponse {
        val member = memberRepository.findByEmail(email) 
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다.")
        
        return MemberResponse(
            id = member.id,
            email = member.email,
            name = member.name,
            phone = member.phone,
            role = member.memberRole.name
        )
    }
    
    fun validateSession(sessionId: String): Boolean {
        val authenticated = sessionService.getAttribute(sessionId, "authenticated")
        return authenticated as? Boolean ?: false
    }
    
    fun getMemberIdFromSession(sessionId: String): Long? {
        return sessionService.getMemberIdFromSession(sessionId)
    }
    
    fun extendSession(sessionId: String) {
        sessionService.extendSession(sessionId)
    }
} 