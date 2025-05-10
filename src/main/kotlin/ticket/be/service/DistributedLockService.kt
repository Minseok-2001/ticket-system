package ticket.be.service

import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

@Service
class DistributedLockService(
    private val redissonClient: RedissonClient
) {
    private val logger = LoggerFactory.getLogger(DistributedLockService::class.java)
    
    /**
     * 분산 락을 획득하고 작업을 실행한 후 락을 해제합니다.
     * @param lockName 락 이름
     * @param waitTime 락 획득 대기 시간 (밀리초)
     * @param leaseTime 락 유지 시간 (밀리초), -1이면 락을 해제할 때까지 유지
     * @param supplier 락 획득 후 실행할 작업
     * @return 작업 결과
     */
    fun <T> executeWithLock(
        lockName: String,
        waitTime: Long = 10000,
        leaseTime: Long = 30000,
        supplier: Supplier<T>
    ): T {
        val lock: RLock = redissonClient.getLock(lockName)
        
        try {
            logger.debug("락 획득 시도: {}", lockName)
            val acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS)
            
            if (!acquired) {
                throw IllegalStateException("락 획득 실패: $lockName")
            }
            
            logger.debug("락 획득 성공: {}", lockName)
            return supplier.get()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("락 획득 중 인터럽트 발생: $lockName", e)
        } finally {
            if (lock.isHeldByCurrentThread) {
                logger.debug("락 해제: {}", lockName)
                lock.unlock()
            }
        }
    }
    
    /**
     * 분산 락을 획득합니다.
     * @param lockName 락 이름
     * @param waitTime 락 획득 대기 시간 (밀리초)
     * @param leaseTime 락 유지 시간 (밀리초), -1이면 락을 해제할 때까지 유지
     * @return 획득한 락 객체
     */
    fun acquireLock(lockName: String, waitTime: Long = 10000, leaseTime: Long = 30000): RLock {
        val lock: RLock = redissonClient.getLock(lockName)
        
        try {
            logger.debug("락 획득 시도: {}", lockName)
            val acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS)
            
            if (!acquired) {
                throw IllegalStateException("락 획득 실패: $lockName")
            }
            
            logger.debug("락 획득 성공: {}", lockName)
            return lock
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("락 획득 중 인터럽트 발생: $lockName", e)
        }
    }
    
    /**
     * 분산 락을 해제합니다.
     * @param lock 락 객체
     */
    fun releaseLock(lock: RLock) {
        if (lock.isHeldByCurrentThread) {
            logger.debug("락 해제: {}", lock.name)
            lock.unlock()
        }
    }
} 