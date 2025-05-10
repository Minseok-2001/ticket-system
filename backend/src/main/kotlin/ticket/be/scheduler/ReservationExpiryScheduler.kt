package ticket.be.scheduler

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ticket.be.domain.ReservationStatus
import ticket.be.repository.ReservationRepository
import ticket.be.service.TicketStockService
import java.time.LocalDateTime

@Component
class ReservationExpiryScheduler(
    private val reservationRepository: ReservationRepository,
    private val ticketStockService: TicketStockService
) {
    private val logger = LoggerFactory.getLogger(ReservationExpiryScheduler::class.java)

    /**
     * 30분마다 만료된 예약을 처리하는 스케줄러
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 1,800,000ms
    @Transactional
    fun processExpiredReservations() {
        logger.info("만료된 예약 처리 스케줄러 실행")

        // 30분 이상 지난 PENDING 상태의 예약을 만료로 처리
        val expiryTime = LocalDateTime.now().minusMinutes(30)
        val expiredReservations = reservationRepository.findExpiredReservations(
            ReservationStatus.PENDING, expiryTime, PageRequest.of(0, 100)
        )

        var processedCount = 0

        expiredReservations.forEach { reservation ->
            try {
                // 예약 및 티켓 상태 업데이트
                reservation.cancel("예약 시간 만료로 자동 취소")
                reservation.ticket?.cancel()

                // 재고 복구
                if (reservation.ticket != null) {
                    ticketStockService.restoreStock(reservation.ticket!!.ticketType.id)
                }

                processedCount++
                logger.info(
                    "만료된 예약 처리 완료: reservationId={}, ticketId={}",
                    reservation.id, reservation.ticket?.id
                )
            } catch (e: Exception) {
                logger.error(
                    "만료된 예약 처리 중 오류 발생: reservationId={}, error={}",
                    reservation.id, e.message, e
                )
            }
        }

        if (processedCount > 0) {
            logger.info("총 {}개의 만료된 예약이 처리되었습니다.", processedCount)
        }
    }
} 