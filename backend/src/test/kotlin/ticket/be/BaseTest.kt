package ticket.be

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import ticket.be.util.TestDataFactory

/**
 * 모든 테스트 클래스의 기본 클래스
 * 테스트에 필요한 공통 설정과 유틸리티를 제공
 */
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
abstract class BaseTest {

    // 테스트 데이터 팩토리 접근을 위한 프로퍼티
    protected val testDataFactory: TestDataFactory = TestDataFactory

    // 테스트에 필요한 공통 상수
    companion object {
        const val TEST_EMAIL = "test@example.com"
        const val TEST_PASSWORD = "password123"
        const val TEST_NAME = "테스트 사용자"
        const val TEST_PHONE = "010-1234-5678"
    }
} 