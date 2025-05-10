package ticket.be.monitoring

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 성능 모니터링 AOP
 * 주요 메서드의 실행 시간을 측정하고 메트릭으로 기록
 */
@Aspect
@Component
class PerformanceMonitoringAspect(private val meterRegistry: MeterRegistry) {
    
    private val logger = LoggerFactory.getLogger(PerformanceMonitoringAspect::class.java)
    
    /**
     * 티켓 예매 메서드 실행 시간 측정
     */
    @Around("execution(* ticket.be.service.TicketReservationService.reserve(..))")
    fun monitorReservationTime(joinPoint: ProceedingJoinPoint): Any {
        val startTime = System.nanoTime()
        
        try {
            return joinPoint.proceed()
        } finally {
            val elapsedTime = System.nanoTime() - startTime
            val elapsedTimeMs = TimeUnit.NANOSECONDS.toMillis(elapsedTime)
            
            // 타이머에 기록
            Timer.builder("method.execution.time")
                .tag("class", joinPoint.signature.declaringTypeName)
                .tag("method", joinPoint.signature.name)
                .description("메서드 실행 시간")
                .register(meterRegistry)
                .record(elapsedTime, TimeUnit.NANOSECONDS)
            
            logger.info("메서드 실행 시간: {}.{} - {}ms", 
                joinPoint.signature.declaringTypeName,
                joinPoint.signature.name,
                elapsedTimeMs)
        }
    }
    
    /**
     * 결제 메서드 실행 시간 측정
     */
    @Around("execution(* ticket.be.service.PaymentService.processPayment(..))")
    fun monitorPaymentTime(joinPoint: ProceedingJoinPoint): Any {
        val startTime = System.nanoTime()
        
        try {
            return joinPoint.proceed()
        } finally {
            val elapsedTime = System.nanoTime() - startTime
            val elapsedTimeMs = TimeUnit.NANOSECONDS.toMillis(elapsedTime)
            
            // 타이머에 기록
            Timer.builder("method.execution.time")
                .tag("class", joinPoint.signature.declaringTypeName)
                .tag("method", joinPoint.signature.name)
                .description("메서드 실행 시간")
                .register(meterRegistry)
                .record(elapsedTime, TimeUnit.NANOSECONDS)
            
            logger.info("결제 처리 시간: {}.{} - {}ms", 
                joinPoint.signature.declaringTypeName,
                joinPoint.signature.name,
                elapsedTimeMs)
        }
    }
    
    /**
     * 대기열 관련 메서드 실행 시간 측정
     */
    @Around("execution(* ticket.be.service.QueueService.*(..))")
    fun monitorQueueTime(joinPoint: ProceedingJoinPoint): Any {
        val startTime = System.nanoTime()
        
        try {
            return joinPoint.proceed()
        } finally {
            val elapsedTime = System.nanoTime() - startTime
            
            // 타이머에 기록
            Timer.builder("method.execution.time")
                .tag("class", joinPoint.signature.declaringTypeName)
                .tag("method", joinPoint.signature.name)
                .description("메서드 실행 시간")
                .register(meterRegistry)
                .record(elapsedTime, TimeUnit.NANOSECONDS)
        }
    }
} 