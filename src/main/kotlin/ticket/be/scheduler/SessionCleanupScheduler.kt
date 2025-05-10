package ticket.be.scheduler

import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.stereotype.Component
import ticket.be.service.SessionService
import java.util.concurrent.TimeUnit

/**
 * 세션 정리 스케줄러
 * 만료된 세션을 주기적으로 정리하는 스케줄러
 */
@Component
class SessionCleanupScheduler(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val sessionService: SessionService
) {
    private val logger = LoggerFactory.getLogger(SessionCleanupScheduler::class.java)
    
    /**
     * 매일 자정에 실행되는 만료된 세션 정리 작업
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    fun cleanupExpiredSessions() {
        logger.info("만료된 세션 정리 작업 시작")
        
        try {
            // Redis에서 만료된 세션 키 스캔
            val sessionKeyPattern = "spring:session:*"
            val keys = redisTemplate.keys(sessionKeyPattern) ?: emptySet()
            
            logger.info("총 세션 수: {}", keys.size)
            
            var expiredCount = 0
            
            // 각 세션에 대해 만료 여부 확인
            for (key in keys) {
                val keyStr = key.toString()
                val sessionId = keyStr.substringAfter("spring:session:")
                
                // 세션 만료 여부 확인 (Redis TTL 확인)
                val ttl = redisTemplate.getExpire(keyStr, TimeUnit.SECONDS)
                
                // TTL이 0 이하인 경우 이미 만료되었거나 만료 예정인 세션
                if (ttl != null && ttl <= 0) {
                    try {
                        // 세션 무효화
                        sessionService.invalidateSession(sessionId)
                        expiredCount++
                    } catch (e: Exception) {
                        logger.error("세션 무효화 중 오류 발생: sessionId={}, error={}", sessionId, e.message)
                    }
                }
            }
            
            logger.info("세션 정리 완료: 처리된 만료 세션 수={}", expiredCount)
        } catch (e: Exception) {
            logger.error("세션 정리 작업 중 오류 발생: {}", e.message, e)
        }
    }
} 