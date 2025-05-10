package ticket.be.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.Event
import ticket.be.domain.Member
import ticket.be.domain.QueueEntry
import ticket.be.domain.QueueStatus
import ticket.be.dto.QueuePositionResponse
import ticket.be.dto.QueueStatusResponse
import ticket.be.repository.EventRepository
import ticket.be.repository.MemberRepository
import ticket.be.repository.QueueEntryRepository
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@Service
class EventQueueService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val queueEntryRepository: QueueEntryRepository,
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val notificationCommandService: NotificationCommandService
) {
    private val logger = LoggerFactory.getLogger(EventQueueService::class.java)
    
    @Value("\${ticket.queue.wait-timeout-seconds:1800}")
    private val queueTimeoutSeconds: Long = 1800 // 기본값 30분
    
    @Value("\${ticket.queue.max-active-users:1000}")
    private val maxActiveUsers: Int = 1000
    
    // Redis 키 생성 헬퍼 메서드
    private fun getQueueKey(eventId: Long): String = "queue:event:$eventId"
    private fun getActiveUsersKey(eventId: Long): String = "queue:active:$eventId"
    private fun getUserKey(eventId: Long, memberId: Long): String = "$eventId:$memberId"
    
    /**
     * 사용자를 대기열에 추가
     */
    @Transactional
    fun addToQueue(eventId: Long, memberId: Long): QueuePositionResponse {
        // 이벤트 및 사용자 확인
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다: id=$eventId") }
        
        // 이벤트가 대기열을 사용하는지 확인
        if (!event.isQueueActive) {
            throw IllegalStateException("대기열이 활성화되지 않은 이벤트입니다.")
        }
        
        val member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: id=$memberId") }
        
        // 이미 대기열에 있는지 확인
        val userKey = getUserKey(eventId, memberId)
        val queueKey = getQueueKey(eventId)
        val position = redisTemplate.opsForZSet().rank(queueKey, userKey)
        
        if (position != null) {
            // 이미 대기열에 있음
            return QueuePositionResponse(
                eventId = eventId,
                memberId = memberId,
                position = position.toInt() + 1, // Redis는 0부터 시작하므로 +1
                estimatedWaitTimeSeconds = calculateEstimatedWaitTime(position.toInt() + 1),
                timestamp = LocalDateTime.now()
            )
        }
        
        // Redis Sorted Set에 추가: score는 현재 타임스탬프(밀리초)
        val currentTime = System.currentTimeMillis()
        redisTemplate.opsForZSet().add(queueKey, userKey, currentTime.toDouble())
        
        // 대기열 만료 시간 설정
        redisTemplate.expire(queueKey, queueTimeoutSeconds, TimeUnit.SECONDS)
        
        // 대기열 위치 조회
        val newPosition = redisTemplate.opsForZSet().rank(queueKey, userKey)?.toInt() ?: 0
        
        // DB에 대기열 항목 저장
        val queueEntry = QueueEntry(
            event = event,
            member = member,
            queuePosition = newPosition + 1,
            status = QueueStatus.WAITING,
            expiresAt = LocalDateTime.now().plusSeconds(queueTimeoutSeconds)
        )
        queueEntryRepository.save(queueEntry)
        
        logger.info("사용자가 대기열에 추가됨: eventId={}, memberId={}, position={}", eventId, memberId, newPosition + 1)
        
        return QueuePositionResponse(
            eventId = eventId,
            memberId = memberId,
            position = newPosition + 1,
            estimatedWaitTimeSeconds = calculateEstimatedWaitTime(newPosition + 1),
            timestamp = LocalDateTime.now()
        )
    }
    
    /**
     * 현재 대기열 위치 조회
     */
    fun getQueuePosition(eventId: Long, memberId: Long): QueuePositionResponse {
        val queueKey = getQueueKey(eventId)
        val userKey = getUserKey(eventId, memberId)
        
        val position = redisTemplate.opsForZSet().rank(queueKey, userKey)
        
        if (position == null) {
            throw IllegalStateException("대기열에 존재하지 않는 사용자입니다.")
        }
        
        return QueuePositionResponse(
            eventId = eventId,
            memberId = memberId,
            position = position.toInt() + 1,
            estimatedWaitTimeSeconds = calculateEstimatedWaitTime(position.toInt() + 1),
            timestamp = LocalDateTime.now()
        )
    }
    
    /**
     * 대기열 상태 확인
     */
    fun getQueueStatus(eventId: Long): QueueStatusResponse {
        val queueKey = getQueueKey(eventId)
        val activeUsersKey = getActiveUsersKey(eventId)
        
        val totalWaiting = redisTemplate.opsForZSet().size(queueKey) ?: 0
        val activeUsers = redisTemplate.opsForSet().size(activeUsersKey) ?: 0
        
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다: id=$eventId") }
        
        return QueueStatusResponse(
            eventId = eventId,
            totalWaiting = totalWaiting,
            activeUsers = activeUsers,
            maxActiveUsers = maxActiveUsers,
            isQueueActive = event.isQueueActive,
            timestamp = LocalDateTime.now()
        )
    }
    
    /**
     * 사용자를 활성 상태로 표시 (대기열 입장 허가)
     */
    @Transactional
    fun admitUser(eventId: Long, memberId: Long) {
        val queueKey = getQueueKey(eventId)
        val activeUsersKey = getActiveUsersKey(eventId)
        val userKey = getUserKey(eventId, memberId)
        
        // 대기열에 있는지 확인
        val position = redisTemplate.opsForZSet().rank(queueKey, userKey)
        
        if (position == null) {
            throw IllegalStateException("대기열에 존재하지 않는 사용자입니다.")
        }
        
        // 활성 사용자 수 확인
        val activeUsers = redisTemplate.opsForSet().size(activeUsersKey) ?: 0
        
        if (activeUsers >= maxActiveUsers) {
            throw IllegalStateException("현재 활성 사용자 수가 최대치에 도달했습니다.")
        }
        
        // 대기열에서 제거하고 활성 사용자에 추가
        redisTemplate.opsForZSet().remove(queueKey, userKey)
        redisTemplate.opsForSet().add(activeUsersKey, userKey)
        
        // 만료 시간 설정
        redisTemplate.expire(activeUsersKey, queueTimeoutSeconds, TimeUnit.SECONDS)
        
        // DB 업데이트
        val queueEntry = queueEntryRepository.findByEventIdAndMemberId(eventId, memberId)
        
        if (queueEntry != null) {
            queueEntry.getNotify()
            queueEntryRepository.save(queueEntry)
        }
        
        // 알림 전송
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다: id=$eventId") }
        
        notificationCommandService.sendNotification(
            memberId = memberId,
            type = "QUEUE_READY",
            title = "${event.name} 입장 안내",
            content = "대기열에서 귀하의 차례가 되었습니다. 30분 이내에 입장해주세요.",
            link = "/events/${eventId}"
        )
        
        logger.info("사용자 입장 허가: eventId={}, memberId={}", eventId, memberId)
    }
    
    /**
     * 다음 대기 사용자 N명 입장 허가
     */
    @Transactional
    fun admitNextUsers(eventId: Long, count: Int = 10): Int {
        val queueKey = getQueueKey(eventId)
        val activeUsersKey = getActiveUsersKey(eventId)
        
        // 활성 사용자 수 확인
        val activeUsers = redisTemplate.opsForSet().size(activeUsersKey) ?: 0
        val availableSlots = maxActiveUsers - activeUsers.toInt()
        
        if (availableSlots <= 0) {
            logger.info("활성 사용자 슬롯이 없습니다: eventId={}", eventId)
            return 0
        }
        
        val admitCount = minOf(count, availableSlots)
        
        // 다음 N명의 사용자 가져오기
        val nextUsers = redisTemplate.opsForZSet().range(queueKey, 0, admitCount - 1L) ?: emptySet()
        
        var admitted = 0
        
        nextUsers.forEach { userKey ->
            val parts = userKey.toString().split(":")
            if (parts.size == 2) {
                val eventIdFromKey = parts[0].toLong()
                val memberId = parts[1].toLong()
                
                try {
                    admitUser(eventIdFromKey, memberId)
                    admitted++
                } catch (e: Exception) {
                    logger.error("사용자 입장 허가 실패: userKey={}, error={}", userKey, e.message)
                }
            }
        }
        
        return admitted
    }
    
    /**
     * 만료된 항목 정리
     */
    @Transactional
    fun cleanupExpiredEntries(eventId: Long): Int {
        val activeUsersKey = getActiveUsersKey(eventId)
        
        val allActiveUsers = redisTemplate.opsForSet().members(activeUsersKey) ?: emptySet()
        var expired = 0
        
        allActiveUsers.forEach { userKey ->
            val parts = userKey.toString().split(":")
            if (parts.size == 2) {
                val eventIdFromKey = parts[0].toLong()
                val memberId = parts[1].toLong()
                
                val queueEntry = queueEntryRepository.findByEventIdAndMemberId(eventIdFromKey, memberId)
                
                if (queueEntry != null && queueEntry.isExpired()) {
                    // Redis에서 제거
                    redisTemplate.opsForSet().remove(activeUsersKey, userKey)
                    
                    // DB 업데이트
                    queueEntry.expire()
                    queueEntryRepository.save(queueEntry)
                    
                    expired++
                    logger.info("만료된 대기열 항목 정리: eventId={}, memberId={}", eventIdFromKey, memberId)
                }
            }
        }
        
        return expired
    }
    
    /**
     * 대기열 활성화/비활성화
     */
    @Transactional
    fun toggleQueueStatus(eventId: Long, active: Boolean) {
        val event = eventRepository.findById(eventId)
            .orElseThrow { IllegalArgumentException("이벤트를 찾을 수 없습니다: id=$eventId") }
        
        if (active) {
            event.activateQueue()
        } else {
            event.deactivateQueue()
        }
        
        eventRepository.save(event)
        logger.info("대기열 상태 변경: eventId={}, active={}", eventId, active)
    }
    
    /**
     * 예상 대기 시간 계산 (초 단위)
     * 이 알고리즘은 실제 시스템 상황에 맞게 조정 필요
     */
    private fun calculateEstimatedWaitTime(position: Int): Long {
        // 간단한 알고리즘: 각 위치당 30초의 처리 시간 가정
        return position.toLong() * 30
    }
} 