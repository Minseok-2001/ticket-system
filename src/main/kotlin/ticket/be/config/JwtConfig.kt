package ticket.be.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.crypto.SecretKey

@Configuration
class JwtConfig {
    
    @Value("\${ticket.auth.jwt.secret-key}")
    private lateinit var secretKeyString: String
    
    @Value("\${ticket.auth.jwt.token-validity-in-seconds}")
    private val tokenValidityInSeconds: Long = 86400 // 기본값 24시간
    
    @Bean
    fun secretKey(): SecretKey {
        // String을 적절한 길이의 바이트 배열로 변환하여 키 생성
        return Keys.hmacShaKeyFor(secretKeyString.toByteArray())
    }
    
    fun generateToken(userDetails: UserDetails, additionalClaims: Map<String, Any> = emptyMap()): String {
        val claims = HashMap<String, Any>(additionalClaims)
        claims["sub"] = userDetails.username
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + tokenValidityInSeconds * 1000))
            .signWith(secretKey(), SignatureAlgorithm.HS256)
            .compact()
    }
    
    fun extractAllClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey())
            .build()
            .parseClaimsJws(token)
            .body
    }
    
    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }
    
    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
    
    fun isTokenExpired(token: String): Boolean {
        return extractAllClaims(token).expiration.before(Date())
    }
} 