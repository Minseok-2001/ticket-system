package ticket.be.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * 테스트 전용 설정 클래스
 * 테스트 환경에서 사용할 특별한 빈 설정을 제공
 */
@TestConfiguration
@Profile("test")
class TestConfig {

    /**
     * 테스트용 패스워드 인코더
     * 테스트 시 빠른 해싱을 위해 약한 알고리즘 사용
     */
    @Bean
    @Primary
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(4) // 반복횟수를 4로 설정하여 테스트 속도 향상
    }

    /**
     * 테스트용 비동기 실행기
     * 테스트에서 비동기 작업을 동기화하기 위해 사용
     */
    @Bean
    @Primary
    fun testTaskExecutor(): Executor {
        return Executors.newFixedThreadPool(2)
    }
} 