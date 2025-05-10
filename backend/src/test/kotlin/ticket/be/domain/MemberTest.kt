package ticket.be.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ticket.be.BaseTest

@DisplayName("Member 도메인 모델 테스트")
class MemberTest : BaseTest() {

    @Test
    @DisplayName("회원 생성 테스트 - 기본 정보로 생성 시 성공해야 함")
    fun createMemberWithBasicInfo() {
        // given
        val email = TEST_EMAIL
        val password = TEST_PASSWORD
        val name = TEST_NAME
        val phone = TEST_PHONE
        val role = MemberRole.USER

        // when
        val member = Member(
            email = email,
            password = password,
            name = name,
            phone = phone,
            memberRole = role
        )

        // then
        assertEquals(email, member.email)
        assertEquals(password, member.password)
        assertEquals(name, member.name)
        assertEquals(phone, member.phone)
        assertEquals(role, member.memberRole)
    }

    @Test
    @DisplayName("회원 권한 확인 테스트 - USER 권한이 부여되어야 함")
    fun checkMemberRole() {
        // given
        val member = testDataFactory.createMember(role = MemberRole.USER)

        // when & then
        assertTrue(member.hasRole(MemberRole.USER))
        assertFalse(member.hasRole(MemberRole.ADMIN))
    }

    @Test
    @DisplayName("회원 정보 업데이트 테스트 - 이름과 전화번호가 업데이트되어야 함")
    fun updateMemberInfo() {
        // given
        val member = testDataFactory.createMember()
        val newName = "새로운 이름"
        val newPhone = "010-9876-5432"

        // when
        member.updateProfile(newName, newPhone)

        // then
        assertEquals(newName, member.name)
        assertEquals(newPhone, member.phone)
    }

    @ParameterizedTest
    @ValueSource(strings = ["USER", "ADMIN"])
    @DisplayName("회원 권한에 따른 동작 테스트")
    fun memberRoleTest(role: String) {
        // given
        val memberRole = MemberRole.valueOf(role)
        val member = testDataFactory.createMember(role = memberRole)

        // when & then
        if (memberRole == MemberRole.ADMIN) {
            assertTrue(member.hasRole(MemberRole.ADMIN))
            assertTrue(member.canAccessAdminFeatures())
        } else {
            assertFalse(member.hasRole(MemberRole.ADMIN))
            assertFalse(member.canAccessAdminFeatures())
        }
    }

    // 회원 도메인 모델의 비즈니스 메서드 테스트를 위한 확장 메서드
    private fun Member.canAccessAdminFeatures(): Boolean {
        return this.hasRole(MemberRole.ADMIN)
    }
} 