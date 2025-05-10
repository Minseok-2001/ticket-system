package ticket.be.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ticket.be.repository.MemberRepository

@Service
class MemberDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val member = memberRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")
        
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${member.memberRole.name}"))
        
        return User.builder()
            .username(member.email)
            .password(member.password)
            .authorities(authorities)
            .build()
    }
} 