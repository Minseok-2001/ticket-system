package ticket.be.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.ToDoubleFunction

/**
 * 커스텀 메트릭 설정
 * Prometheus에 수집될 애플리케이션 메트릭 정의
 */
@Configuration
class MetricsConfig(private val registry: MeterRegistry) {

    // 동시 접속자 수를 관리하는 AtomicInteger
    private val concurrentUsers = AtomicInteger(0)

    /**
     * 대기열 진입 카운터
     */
    @Bean
    fun queueEntranceCounter(): Counter {
        return Counter.builder("ticket.queue.entrance")
            .description("대기열 진입 횟수")
            .register(registry)
    }

    /**
     * 대기열 이탈 카운터
     */
    @Bean
    fun queueExitCounter(): Counter {
        return Counter.builder("ticket.queue.exit")
            .description("대기열 이탈 횟수")
            .register(registry)
    }

    /**
     * 티켓 예매 성공 카운터
     */
    @Bean
    fun ticketReservationSuccessCounter(): Counter {
        return Counter.builder("ticket.reservation.success")
            .description("티켓 예매 성공 횟수")
            .register(registry)
    }

    /**
     * 티켓 예매 실패 카운터
     */
    @Bean
    fun ticketReservationFailureCounter(): Counter {
        return Counter.builder("ticket.reservation.failure")
            .description("티켓 예매 실패 횟수")
            .register(registry)
    }

    /**
     * 결제 성공 카운터
     */
    @Bean
    fun paymentSuccessCounter(): Counter {
        return Counter.builder("ticket.payment.success")
            .description("결제 성공 횟수")
            .register(registry)
    }

    /**
     * 결제 실패 카운터
     */
    @Bean
    fun paymentFailureCounter(): Counter {
        return Counter.builder("ticket.payment.failure")
            .description("결제 실패 횟수")
            .register(registry)
    }

    /**
     * 티켓 예매 타이머
     * 예매 프로세스의 수행 시간 측정
     */
    @Bean
    fun reservationTimer(): Timer {
        return Timer.builder("ticket.reservation.duration")
            .description("티켓 예매 프로세스 소요 시간")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry)
    }

    /**
     * 동시 접속자 수
     */
    @Bean
    fun concurrentUsersGauge(): AtomicInteger {
        // 레지스트리에 게이지 등록
        registry.gauge("ticket.users.concurrent", concurrentUsers)
        return concurrentUsers
    }
} 