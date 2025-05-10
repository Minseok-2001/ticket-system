package ticket.be.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.session.Session
import org.springframework.session.SessionRepository
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * 세션 관리 서비스
 * Redis를 사용하여 세션 관리 기능을 제공
 */
@Service
class SessionService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val sessionRepository: SessionRepository<out Session>
) {
    private val logger = LoggerFactory.getLogger(SessionService::class.java)

    /**
     * 사용자 세션에 속성 저장
     */
    fun setAttribute(sessionId: String, key: String, value: Any, expireSeconds: Long = 3600) {
        val sessionKey = getSessionKey(sessionId)
        val attributeKey = "$sessionKey:$key"
        redisTemplate.opsForValue().set(attributeKey, value)
        redisTemplate.expire(attributeKey, expireSeconds, TimeUnit.SECONDS)
        logger.debug("세션 속성 저장: sessionId={}, key={}", sessionId, key)
    }

    /**
     * 사용자 세션에서 속성 조회
     */
    fun getAttribute(sessionId: String, key: String): Any? {
        val sessionKey = getSessionKey(sessionId)
        val attributeKey = "$sessionKey:$key"
        val value = redisTemplate.opsForValue().get(attributeKey)
        logger.debug("세션 속성 조회: sessionId={}, key={}, exists={}", sessionId, key, value != null)
        return value
    }

    /**
     * 사용자 세션에서 속성 삭제
     */
    fun removeAttribute(sessionId: String, key: String) {
        val sessionKey = getSessionKey(sessionId)
        val attributeKey = "$sessionKey:$key"
        redisTemplate.delete(attributeKey)
        logger.debug("세션 속성 삭제: sessionId={}, key={}", sessionId, key)
    }

    /**
     * 사용자 세션 무효화
     */
    fun invalidateSession(sessionId: String) {
        try {
            sessionRepository.deleteById(sessionId)
            // 세션 관련 모든 키 삭제
            val sessionKey = getSessionKey(sessionId)
            val keys = redisTemplate.keys("$sessionKey*")
            if (keys.isNotEmpty()) {
                redisTemplate.delete(keys)
            }
            logger.info("세션 무효화: sessionId={}", sessionId)
        } catch (e: Exception) {
            logger.error("세션 무효화 실패: sessionId={}, error={}", sessionId, e.message, e)
        }
    }

    /**
     * 세션 만료 시간 갱신
     */
    fun extendSession(sessionId: String, expireSeconds: Long = 3600) {
        try {
            // 직접 Redis에서 만료 시간 설정
            val sessionKey = getSessionKey(sessionId)
            redisTemplate.expire(sessionKey, expireSeconds, TimeUnit.SECONDS)
            logger.debug("세션 만료 시간 갱신: sessionId={}, expireSeconds={}", sessionId, expireSeconds)
        } catch (e: Exception) {
            logger.error("세션 만료 시간 갱신 실패: sessionId={}, error={}", sessionId, e.message, e)
        }
    }

    /**
     * 세션 ID로 사용자 ID 조회
     */
    fun getMemberIdFromSession(sessionId: String): Long? {
        val memberId = getAttribute(sessionId, "memberId")
        return memberId as? Long
    }

    /**
     * 세션 키 생성
     */
    private fun getSessionKey(sessionId: String): String {
        return "spring:session:$sessionId"
    }
} 