package ticket.be.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import java.net.URI

@Configuration
class AwsConfig {
    
    @Value("\${spring.cloud.aws.credentials.access-key}")
    private lateinit var accessKey: String
    
    @Value("\${spring.cloud.aws.credentials.secret-key}")
    private lateinit var secretKey: String
    
    @Value("\${spring.cloud.aws.region.static}")
    private lateinit var region: String
    
    @Value("\${spring.cloud.aws.endpoint:#{null}}")
    private var endpoint: String? = null
    
    @Bean
    fun snsClient(): SnsClient {
        val credentialsProvider = StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)
        )
        
        val builder = SnsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(credentialsProvider)
        
        // 로컬 개발 환경에서는 LocalStack 등의 에뮬레이터 엔드포인트 사용
        endpoint?.let { 
            builder.endpointOverride(URI.create(it))
        }
        
        return builder.build()
    }
} 