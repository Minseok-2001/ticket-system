package ticket.be.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import ticket.be.BaseTest
import ticket.be.config.QueueProperties
import ticket.be.domain.Member
import ticket.be.dto.QueueEntryResponse
import ticket.be.dto.QueueStatusResponse
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@ExtendWith(MockitoExtension::class)
@DisplayName("QueueService 테스트")
class QueueServiceTest : BaseTest() {

    @Mock
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    private lateinit var zSetOperations: ZSetOperations<String, String>

    @Mock
    private lateinit var redissonClient: RedissonClient

    @Mock
    private lateinit var lock: RLock

    @Mock
    private lateinit var queueProperties: QueueProperties

    @InjectMocks
    private lateinit var queueService: QueueService

    private lateinit var testMember: Member
    private val eventId = 1L
    private val queueKey = "queue:event:1"
    private val queueCountKey = "queue:event:1:count"
    private val activeUsersKey = "queue:event:1:active"
    private val waitTimeoutSeconds = 1800L

    @BeforeEach
    fun setUp() {
        testMember = testDataFactory.createMember()
        
        `when`(redisTemplate.opsForZSet()).thenReturn(zSetOperations)
        `when`(redissonClient.getLock(anyString())).thenReturn(lock)
        `when`(queueProperties.waitTimeoutSeconds).thenReturn(waitTimeoutSeconds)
        `when`(queueProperties.maxActiveUsers).thenReturn(100)
    }

    @Test
    @DisplayName("대기열 진입 테스트 - 성공")
    fun enterQueueSuccess() {
        // given
        val memberId = testMember.id
        val currentMillis = System.currentTimeMillis()
        val queueCount = 42L

        `when`(redisTemplate.hasKey(queueCountKey)).thenReturn(true)
        `when`(redisTemplate.opsForValue().get(queueCountKey)).thenReturn(queueCount.toString())
        `when`(redisTemplate.opsForValue().increment(queueCountKey)).thenReturn(queueCount + 1)
        `when`(zSetOperations.rank(queueKey, memberId.toString())).thenReturn(null) // 아직 대기열에 없음
        `when`(lock.tryLock(anyLong(), anyLong(), any())).thenReturn(true)

        // when
        val result = queueService.enterQueue(eventId, memberId)

        // then
        verify(redisTemplate.opsForValue()).increment(queueCountKey)
        verify(zSetOperations).add(queueKey, memberId.toString(), currentMillis.toDouble() ± 10000) // 약간의 시간 오차 허용
        verify(redisTemplate).expire(queueKey, waitTimeoutSeconds, TimeUnit.SECONDS)
        verify(lock).unlock()
        
        assert(result.position == queueCount)
        assert(result.totalWaitingCount == queueCount)
        assert(result.estimatedWaitTimeMinutes > 0)
    }

    @Test
    @DisplayName("대기열 상태 조회 테스트")
    fun getQueueStatus() {
        // given
        val memberId = testMember.id
        val position = 5L
        val totalCount = 42L
        val activeCount = 10

        `when`(zSetOperations.rank(queueKey, memberId.toString())).thenReturn(position)
        `when`(zSetOperations.size(queueKey)).thenReturn(totalCount)
        `when`(zSetOperations.size(activeUsersKey)).thenReturn(activeCount.toLong())

        // when
        val result = queueService.getQueueStatus(eventId, memberId)

        // then
        verify(zSetOperations).rank(queueKey, memberId.toString())
        verify(zSetOperations).size(queueKey)
        verify(zSetOperations).size(activeUsersKey)
        
        assert(result.position == position)
        assert(result.totalWaitingCount == totalCount)
        assert(result.activeUserCount == activeCount)
        assert(result.inQueue)
    }

    @Test
    @DisplayName("대기열에 없는 경우 상태 조회 테스트")
    fun getQueueStatusWhenNotInQueue() {
        // given
        val memberId = testMember.id
        val totalCount = 42L
        val activeCount = 10

        `when`(zSetOperations.rank(queueKey, memberId.toString())).thenReturn(null)
        `when`(zSetOperations.size(queueKey)).thenReturn(totalCount)
        `when`(zSetOperations.size(activeUsersKey)).thenReturn(activeCount.toLong())

        // when
        val result = queueService.getQueueStatus(eventId, memberId)

        // then
        verify(zSetOperations).rank(queueKey, memberId.toString())
        verify(zSetOperations).size(queueKey)
        verify(zSetOperations).size(activeUsersKey)
        
        assert(result.position == -1L)
        assert(result.totalWaitingCount == totalCount)
        assert(result.activeUserCount == activeCount)
        assert(!result.inQueue)
    }

    @Test
    @DisplayName("대기열에서 사용자 제거 테스트")
    fun leaveQueue() {
        // given
        val memberId = testMember.id

        // when
        queueService.leaveQueue(eventId, memberId)

        // then
        verify(zSetOperations).remove(queueKey, memberId.toString())
    }

    @Test
    @DisplayName("대기열에서 활성 사용자로 이동 테스트")
    fun promoteToActiveUser() {
        // given
        val memberId = testMember.id
        val currentActiveUsers = 90

        `when`(zSetOperations.size(activeUsersKey)).thenReturn(currentActiveUsers.toLong())
        `when`(zSetOperations.rank(queueKey, memberId.toString())).thenReturn(0L) // 대기열의 맨 앞
        `when`(lock.tryLock(anyLong(), anyLong(), any())).thenReturn(true)

        // when
        val result = queueService.promoteToActiveUser(eventId, memberId)

        // then
        verify(zSetOperations).remove(queueKey, memberId.toString())
        verify(zSetOperations).add(activeUsersKey, memberId.toString(), System.currentTimeMillis().toDouble() ± 10000)
        verify(redisTemplate).expire(activeUsersKey, waitTimeoutSeconds, TimeUnit.SECONDS)
        verify(lock).unlock()
        
        assert(result)
    }

    @Test
    @DisplayName("대기열에서 활성 사용자로 이동 실패 테스트 - 최대 활성 사용자 수 초과")
    fun promoteToActiveUserFailWhenMaxReached() {
        // given
        val memberId = testMember.id
        val currentActiveUsers = 100 // 최대치

        `when`(zSetOperations.size(activeUsersKey)).thenReturn(currentActiveUsers.toLong())
        `when`(lock.tryLock(anyLong(), anyLong(), any())).thenReturn(true)

        // when
        val result = queueService.promoteToActiveUser(eventId, memberId)

        // then
        verify(zSetOperations, never()).remove(queueKey, memberId.toString())
        verify(zSetOperations, never()).add(eq(activeUsersKey), anyString(), anyDouble())
        verify(lock).unlock()
        
        assert(!result)
    }

    @Test
    @DisplayName("대기 예상 시간 계산 테스트")
    fun calculateEstimatedWaitTime() {
        // given
        val position = 20L
        val processingRate = 5.0 // 분당 5명 처리

        // when
        val waitTimeMinutes = QueueService.calculateEstimatedWaitTime(position, processingRate)

        // then
        assert(waitTimeMinutes == position / processingRate)
    }

    // 부가적인 확장 함수 - 근사값 비교를 위한 메소드
    private infix fun Double.±(tolerance: Int): DoubleRange {
        return DoubleRange(this - tolerance, this + tolerance)
    }

    private class DoubleRange(private val from: Double, private val to: Double) {
        operator fun contains(value: Double): Boolean = value in from..to
    }
} 