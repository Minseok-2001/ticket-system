package ticket.be.scheduler

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.QueueStatus
import ticket.be.repository.EventRepository
import ticket.be.repository.QueueEntryRepository
import ticket.be.service.EventQueueService

@Component
class QueueCleanupScheduler(
    private val eventRepository: EventRepository,
    private val queueEntryRepository: QueueEntryRepository,
    private val eventQueueService: EventQueueService
) {
    private val logger = LoggerFactory.getLogger(QueueCleanupScheduler::class.java)
    
    /**
     * 5분마다 모든 이벤트의 만료된 대기열 항목 정리
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    @Transactional
    fun cleanupExpiredQueueEntries() {
        logger.info("만료된 대기열 항목 정리 스케줄러 실행")
        
        // 활성화된 대기열이 있는 이벤트 조회
        val events = eventRepository.findByIsQueueActiveTrue()
        
        for (event in events) {
            try {
                val expiredCount = eventQueueService.cleanupExpiredEntries(event.id)
                if (expiredCount > 0) {
                    logger.info("이벤트에서 만료된 항목 정리 완료: eventId={}, expiredCount={}", event.id, expiredCount)
                }
            } catch (e: Exception) {
                logger.error("이벤트 대기열 정리 중 오류 발생: eventId={}, error={}", event.id, e.message, e)
            }
        }
        
        // DB에서도 만료된 항목 확인 및 처리
        try {
            val expiredEntries = queueEntryRepository.findExpiredEntries(QueueStatus.NOTIFIED, PageRequest.of(0, 100))
            
            for (entry in expiredEntries) {
                entry.expire()
                queueEntryRepository.save(entry)
                logger.info("만료된 대기열 항목 상태 업데이트: entryId={}, eventId={}, memberId={}", 
                    entry.id, entry.event.id, entry.member.id)
            }
        } catch (e: Exception) {
            logger.error("만료된 대기열 항목 DB 처리 중 오류 발생: error={}", e.message, e)
        }
    }
} 