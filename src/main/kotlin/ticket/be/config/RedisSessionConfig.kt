package ticket.be.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.web.http.CookieSerializer
import org.springframework.session.web.http.DefaultCookieSerializer
import org.springframework.beans.factory.annotation.Value

/**
 * Redis 기반 세션 저장소 설정
 * Spring Session과 Redis를 통합하여 세션 관리를 구현
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // 세션 만료 시간 1시간
class RedisSessionConfig {

    @Value("\${server.servlet.session.cookie.secure}")
    private val secureCookie: Boolean = false

    /**
     * 세션 직렬화 설정
     * Redis에 세션을 저장할 때 사용할 직렬화 방식 설정
     */
    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
        return GenericJackson2JsonRedisSerializer()
    }

    /**
     * 쿠키 설정
     * 세션 쿠키 관련 설정 (이름, 도메인, 경로, 보안 등)
     */
    @Bean
    fun cookieSerializer(): CookieSerializer {
        val serializer = DefaultCookieSerializer()
        serializer.setCookieName("TICKET_SESSION")
        serializer.setCookiePath("/")
        serializer.setUseSecureCookie(secureCookie) // HTTPS 사용 시 true로 설정
        serializer.setUseHttpOnlyCookie(true) // JavaScript에서 쿠키 접근 방지
        serializer.setSameSite("Lax") // CSRF 방어를 위한 SameSite 설정
        return serializer
    }
} 