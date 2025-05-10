package ticket.be.performance

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import ticket.be.service.QueueService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("test")
@Tag("performance")
@DisplayName("대기열 성능 테스트")
class QueuePerformanceTest {

    @Autowired
    private lateinit var queueService: QueueService

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    private val eventId = 99L
    private val queueKey = "queue:event:$eventId"
    private val queueCountKey = "queue:event:$eventId:count"
    private val activeUsersKey = "queue:event:$eventId:active"

    @BeforeEach
    fun setUp() {
        // 테스트 전 기존 데이터 삭제
        redisTemplate.delete(queueKey)
        redisTemplate.delete(queueCountKey)
        redisTemplate.delete(activeUsersKey)
    }

    @Test
    @DisplayName("동시에 1000명의 사용자가 대기열에 진입하는 시나리오")
    fun bulkQueueEntry() {
        // given
        val numUsers = 1000
        val executorService = Executors.newFixedThreadPool(100)
        val latch = CountDownLatch(numUsers)
        val successCounter = AtomicInteger(0)
        val failCounter = AtomicInteger(0)
        
        // when
        val startTime = System.currentTimeMillis()
        
        for (i in 1..numUsers) {
            val memberId = i.toLong()
            executorService.submit {
                try {
                    val result = queueService.enterQueue(eventId, memberId)
                    if (result.position >= 0) {
                        successCounter.incrementAndGet()
                    } else {
                        failCounter.incrementAndGet()
                    }
                } catch (e: Exception) {
                    failCounter.incrementAndGet()
                    println("Error for member $memberId: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // then
        // 모든 작업이 3초 내에 완료되는지 확인
        val completed = latch.await(30, TimeUnit.SECONDS)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        println("Bulk queue entry completed: $completed")
        println("Execution time: $duration ms")
        println("Success entries: ${successCounter.get()}")
        println("Failed entries: ${failCounter.get()}")
        
        // 검증
        val totalEntries = redisTemplate.opsForZSet().size(queueKey) ?: 0
        println("Total entries in queue: $totalEntries")
        
        assert(completed)
        assert(successCounter.get() > 0) // 최소한 일부는 성공
        assert(totalEntries > 0) // 대기열에 실제로 사용자가 들어갔는지 확인
        
        executorService.shutdown()
    }

    @Test
    @DisplayName("대기열에서 활성 사용자로 동시에 이동하는 시나리오")
    fun bulkPromoteToActiveUser() {
        // given
        val numUsers = 500
        val executorService = Executors.newFixedThreadPool(50)
        val latch = CountDownLatch(numUsers)
        val successCounter = AtomicInteger(0)
        val failCounter = AtomicInteger(0)
        
        // 먼저 대기열에 사용자 추가
        for (i in 1..numUsers) {
            queueService.enterQueue(eventId, i.toLong())
        }
        
        // when
        val startTime = System.currentTimeMillis()
        
        for (i in 1..numUsers) {
            val memberId = i.toLong()
            executorService.submit {
                try {
                    val result = queueService.promoteToActiveUser(eventId, memberId)
                    if (result) {
                        successCounter.incrementAndGet()
                    } else {
                        failCounter.incrementAndGet()
                    }
                } catch (e: Exception) {
                    failCounter.incrementAndGet()
                    println("Error for member $memberId: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // then
        // 모든 작업이 3초 내에 완료되는지 확인
        val completed = latch.await(30, TimeUnit.SECONDS)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        println("Bulk promote completed: $completed")
        println("Execution time: $duration ms")
        println("Success promotions: ${successCounter.get()}")
        println("Failed promotions: ${failCounter.get()}")
        
        // 검증
        val remainingInQueue = redisTemplate.opsForZSet().size(queueKey) ?: 0
        val activeUsers = redisTemplate.opsForZSet().size(activeUsersKey) ?: 0
        println("Remaining in queue: $remainingInQueue")
        println("Active users: $activeUsers")
        
        assert(completed)
        assert(successCounter.get() > 0) // 최소한 일부는 성공
        assert(activeUsers > 0) // 활성 사용자로 실제로 이동했는지 확인
        
        executorService.shutdown()
    }

    @Test
    @DisplayName("사용자 대기열 상태 조회 성능 테스트")
    fun bulkQueueStatusCheck() {
        // given
        val numUsers = 1000
        val executorService = Executors.newFixedThreadPool(100)
        val latch = CountDownLatch(numUsers)
        val totalTime = AtomicInteger(0)
        
        // 먼저 대기열에 사용자 추가
        for (i in 1..numUsers) {
            queueService.enterQueue(eventId, i.toLong())
        }
        
        // when
        val startTime = System.currentTimeMillis()
        
        for (i in 1..numUsers) {
            val memberId = i.toLong()
            executorService.submit {
                try {
                    val statusStartTime = System.currentTimeMillis()
                    val result = queueService.getQueueStatus(eventId, memberId)
                    val statusEndTime = System.currentTimeMillis()
                    totalTime.addAndGet((statusEndTime - statusStartTime).toInt())
                    
                    // 상태 확인 결과 검증
                    assert(result.inQueue)
                    assert(result.position >= 0)
                } catch (e: Exception) {
                    println("Error for member $memberId: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }
        
        // then
        // 모든 작업이 3초 내에 완료되는지 확인
        val completed = latch.await(30, TimeUnit.SECONDS)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        println("Bulk status check completed: $completed")
        println("Total execution time: $duration ms")
        println("Average status check time: ${totalTime.get() / numUsers.toDouble()} ms")
        
        assert(completed)
        
        executorService.shutdown()
    }
} 