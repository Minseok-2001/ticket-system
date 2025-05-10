package ticket.be.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,
    
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    val password: String
)

data class SignupRequest(
    @field:NotBlank(message = "이메일은 필수 입력값입니다.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,
    
    @field:NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    val password: String,
    
    @field:NotBlank(message = "이름은 필수 입력값입니다.")
    val name: String,
    
    val phone: String?
)

data class TokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)

data class MemberResponse(
    val id: Long,
    val email: String,
    val name: String,
    val phone: String?,
    val role: String
) 