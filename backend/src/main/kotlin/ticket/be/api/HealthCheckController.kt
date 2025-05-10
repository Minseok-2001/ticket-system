package ticket.be.api

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicInteger

/**
 * 헬스 체크 컨트롤러
 * 서버 상태 확인 및 모니터링용 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/health")
class HealthCheckController(
    private val environment: Environment,
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(HealthCheckController::class.java)
    private val activeRequests = AtomicInteger(0)
    
    init {
        // 동시 접속자 수 게이지 등록
        meterRegistry.gauge("http.requests.active", activeRequests)
    }

    /**
     * 기본 헬스 체크
     * 서버가 정상 동작 중인지 확인
     */
    @GetMapping("/check")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        logger.info("헬스 체크 요청 수신")
        
        val hostname = InetAddress.getLocalHost().hostName
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        
        activeRequests.incrementAndGet()
        
        try {
            return ResponseEntity.ok(mapOf(
                "status" to "UP",
                "timestamp" to now.format(formatter),
                "hostname" to hostname,
                "profiles" to (environment.activeProfiles.toList().ifEmpty { listOf("default") })
            ))
        } finally {
            activeRequests.decrementAndGet()
        }
    }

    /**
     * 상세 헬스 체크
     * 서버의 상세 상태 정보 제공
     */
    @GetMapping("/details")
    fun healthDetails(): ResponseEntity<Map<String, Any>> {
        logger.info("상세 헬스 정보 요청 수신")
        
        activeRequests.incrementAndGet()
        
        try {
            val runtime = Runtime.getRuntime()
            val mb = 1024 * 1024
            
            val status = mapOf(
                "status" to "UP",
                "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                "hostname" to InetAddress.getLocalHost().hostName,
                "profiles" to (environment.activeProfiles.toList().ifEmpty { listOf("default") }),
                "memory" to mapOf(
                    "total" to "${runtime.totalMemory() / mb} MB",
                    "free" to "${runtime.freeMemory() / mb} MB",
                    "used" to "${(runtime.totalMemory() - runtime.freeMemory()) / mb} MB"
                ),
                "processors" to runtime.availableProcessors(),
                "threads" to Thread.activeCount()
            )
            
            return ResponseEntity.ok(status)
        } finally {
            activeRequests.decrementAndGet()
        }
    }
} 