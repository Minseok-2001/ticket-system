package ticket.be.domain

import jakarta.persistence.*
import ticket.be.domain.base.BaseTimeEntity

@Entity
@Table(name = "member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false, length = 100, unique = true)
    var email: String,
    
    @Column(nullable = false, length = 100)
    var password: String,
    
    @Column(nullable = false, length = 50)
    var name: String,
    
    @Column(length = 20)
    var phone: String? = null,
    
    @Column(length = 255)
    var deviceToken: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: MemberRole = MemberRole.USER
) : BaseTimeEntity() {

    fun updateDeviceToken(deviceToken: String?) {
        this.deviceToken = deviceToken
    }
    
    fun updateProfile(name: String, phone: String?) {
        this.name = name
        this.phone = phone
    }
}

enum class MemberRole {
    USER, ADMIN
} 