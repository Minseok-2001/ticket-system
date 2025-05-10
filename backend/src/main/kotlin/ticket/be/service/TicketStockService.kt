package ticket.be.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.TicketType
import ticket.be.repository.TicketTypeRepository
import ticket.be.service.exception.StockException
import java.util.concurrent.TimeUnit

@Service
class TicketStockService(
    private val ticketTypeRepository: TicketTypeRepository,
    @Qualifier("customStringRedisTemplate")
    private val redisTemplate: RedisTemplate<String, String>,
    private val distributedLockService: DistributedLockService
) {
    private val logger = LoggerFactory.getLogger(TicketStockService::class.java)
    
    // Redis 키 생성 헬퍼 메서드
    private fun getStockKey(ticketTypeId: Long): String = "stock:ticket_type:$ticketTypeId"
    
    /**
     * 초기 재고를 Redis에 캐싱
     */
    @Transactional(readOnly = true)
    fun initializeStock(ticketTypeId: Long) {
        val stockKey = getStockKey(ticketTypeId)
        
        // 이미 캐싱되어 있는지 확인
        if (redisTemplate.hasKey(stockKey)) {
            return
        }
        
        // Redis에 초기 재고 설정
        val ticketType = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: id=$ticketTypeId") }
        
        redisTemplate.opsForValue().set(stockKey, ticketType.availableQuantity.toString())
        redisTemplate.expire(stockKey, 1, TimeUnit.DAYS) // 1일 후 만료
        
        logger.info("티켓 타입 재고 초기화: ticketTypeId={}, availableQuantity={}", 
            ticketTypeId, ticketType.availableQuantity)
    }
    
    /**
     * Redis에서 현재 재고 조회 (캐시 미스 시 DB에서 조회하여 캐싱)
     */
    fun getCurrentStock(ticketTypeId: Long): Int {
        val stockKey = getStockKey(ticketTypeId)
        
        // Redis에서 재고 조회
        val stockStr = redisTemplate.opsForValue().get(stockKey)
        
        if (stockStr != null) {
            return stockStr.toInt()
        }
        
        // 캐시 미스: DB에서 조회하여 캐싱
        val ticketType = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: id=$ticketTypeId") }
        
        redisTemplate.opsForValue().set(stockKey, ticketType.availableQuantity.toString())
        redisTemplate.expire(stockKey, 1, TimeUnit.DAYS) // 1일 후 만료
        
        return ticketType.availableQuantity
    }
    
    /**
     * 재고 감소 시도 (분산 락 사용하여 동시성 제어)
     * @return 재고 감소 성공 여부
     */
    fun tryDecreaseStock(ticketTypeId: Long, count: Int = 1): Boolean {
        // 락 사용하여 동시성 제어
        val lockName = "lock:stock:$ticketTypeId"
        
        return distributedLockService.executeWithLock(
            lockName = lockName,
            waitTime = 5000, // 5초 대기
            leaseTime = 10000, // 10초 유지
            supplier = { 
                decreaseStockInternal(ticketTypeId, count)
                true
            }
        )
    }
    
    /**
     * 재고 감소 내부 구현 (락 취득 후 호출)
     */
    private fun decreaseStockInternal(ticketTypeId: Long, count: Int) {
        val stockKey = getStockKey(ticketTypeId)
        
        // Redis에서 현재 재고 확인
        val currentStockStr = redisTemplate.opsForValue().get(stockKey)
        val currentStock = currentStockStr?.toInt() ?: getCurrentStock(ticketTypeId)
        
        if (currentStock < count) {
            throw StockException("재고가 부족합니다: 현재=$currentStock, 요청=$count")
        }
        
        // Redis 재고 감소
        val newStock = currentStock - count
        redisTemplate.opsForValue().set(stockKey, newStock.toString())
        
        logger.info("티켓 타입 재고 감소: ticketTypeId={}, before={}, after={}", 
            ticketTypeId, currentStock, newStock)
    }
    
    /**
     * DB에 재고 적용 (실제 예매 확정 시 호출)
     */
    @Transactional
    fun commitStock(ticketTypeId: Long, count: Int = 1): TicketType {
        val ticketType = ticketTypeRepository.findById(ticketTypeId)
            .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: id=$ticketTypeId") }
        
        // 재고 업데이트
        ticketType.decreaseAvailableQuantity(count)
        
        ticketTypeRepository.save(ticketType)
        
        // Redis 재고 업데이트 (DB 값과 동기화)
        val stockKey = getStockKey(ticketTypeId)
        redisTemplate.opsForValue().set(stockKey, ticketType.availableQuantity.toString())
        
        logger.info("티켓 타입 DB 재고 확정: ticketTypeId={}, availableQuantity={}", 
            ticketTypeId, ticketType.availableQuantity)
        
        return ticketType
    }
    
    /**
     * 재고 복구 (예매 취소 등의 이유로 재고를 되돌릴 때 호출)
     */
    @Transactional
    fun restoreStock(ticketTypeId: Long, count: Int = 1): TicketType {
        // 락 사용하여 동시성 제어
        val lockName = "lock:stock:$ticketTypeId"
        
        return distributedLockService.executeWithLock(
            lockName = lockName,
            supplier = {
                val ticketType = ticketTypeRepository.findById(ticketTypeId)
                    .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: id=$ticketTypeId") }
                
                // 재고 복구
                ticketType.increaseAvailableQuantity(count)
                
                val savedTicketType = ticketTypeRepository.save(ticketType)
                
                // Redis 재고 업데이트 (DB 값과 동기화)
                val stockKey = getStockKey(ticketTypeId)
                redisTemplate.opsForValue().set(stockKey, ticketType.availableQuantity.toString())
                
                logger.info("티켓 타입 재고 복구: ticketTypeId={}, availableQuantity={}", 
                    ticketTypeId, ticketType.availableQuantity)
                
                savedTicketType
            }
        )
    }
    
    /**
     * 재고 동기화 (Redis와 DB 간 재고 정보 동기화)
     */
    @Transactional
    fun synchronizeStock(ticketTypeId: Long) {
        // 락 사용하여 동시성 제어
        val lockName = "lock:stock:$ticketTypeId"
        
        distributedLockService.executeWithLock(
            lockName = lockName,
            supplier = {
                val ticketType = ticketTypeRepository.findById(ticketTypeId)
                    .orElseThrow { IllegalArgumentException("티켓 타입을 찾을 수 없습니다: id=$ticketTypeId") }
                
                // Redis 재고 업데이트 (DB 값과 동기화)
                val stockKey = getStockKey(ticketTypeId)
                redisTemplate.opsForValue().set(stockKey, ticketType.availableQuantity.toString())
                
                logger.info("티켓 타입 재고 동기화: ticketTypeId={}, availableQuantity={}", 
                    ticketTypeId, ticketType.availableQuantity)
                
                Unit
            }
        )
    }
} 