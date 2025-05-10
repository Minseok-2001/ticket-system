package ticket.be.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ticket.be.domain.Member

@Repository
interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?
} 