package ticket.be.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedissonConfig {

    @Value("\${spring.data.redis.host:localhost}")
    private lateinit var host: String
    
    @Value("\${spring.data.redis.port:6379}")
    private val port: Int = 6379
    
    @Value("\${spring.data.redis.timeout:10000}")
    private val timeout: Int = 10000
    
    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setAddress("redis://$host:$port")
            .setTimeout(timeout)
        
        return Redisson.create(config)
    }
} 