package ticket.be.util

import ticket.be.domain.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 테스트 데이터 생성 팩토리
 * 테스트에 필요한 객체들을 쉽게 생성할 수 있는 유틸리티 클래스
 */
object TestDataFactory {

    /**
     * 테스트용 회원 생성
     */
    fun createMember(
        id: Long = 1L,
        email: String = "test@example.com",
        password: String = "password123",
        name: String = "테스트 사용자",
        phone: String = "010-1234-5678",
        role: MemberRole = MemberRole.USER
    ): Member {
        val member = Member(
            email = email,
            password = password,
            name = name,
            phone = phone,
            memberRole = role
        )
        
        // ID 값 설정을 위한 리플렉션 사용
        val field = Member::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(member, id)
        
        return member
    }

    /**
     * 테스트용 이벤트 생성
     */
    fun createEvent(
        id: Long = 1L,
        name: String = "테스트 콘서트",
        description: String = "테스트 콘서트 설명",
        startDateTime: LocalDateTime = LocalDateTime.now().plusDays(10),
        endDateTime: LocalDateTime = LocalDateTime.now().plusDays(10).plusHours(3),
        location: String = "테스트 공연장",
        totalSeats: Int = 1000,
        availableSeats: Int = 1000,
        ticketPrice: BigDecimal = BigDecimal("50000"),
        saleStartDateTime: LocalDateTime = LocalDateTime.now().minusDays(1),
        saleEndDateTime: LocalDateTime = LocalDateTime.now().plusDays(5)
    ): Event {
        val event = Event(
            name = name,
            description = description,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
            location = location,
            totalSeats = totalSeats,
            availableSeats = availableSeats,
            ticketPrice = ticketPrice,
            saleStartDateTime = saleStartDateTime,
            saleEndDateTime = saleEndDateTime
        )
        
        // ID 값 설정을 위한 리플렉션 사용
        val field = Event::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(event, id)
        
        return event
    }

    /**
     * 테스트용 예약 생성
     */
    fun createReservation(
        id: Long = 1L,
        member: Member = createMember(),
        event: Event = createEvent(),
        numTickets: Int = 2,
        totalAmount: BigDecimal = event.ticketPrice.multiply(BigDecimal(numTickets)),
        status: ReservationStatus = ReservationStatus.PENDING,
        paymentMethod: String = "CREDIT_CARD",
        paymentId: String? = null,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Reservation {
        val reservation = Reservation(
            member = member,
            event = event,
            numTickets = numTickets,
            totalAmount = totalAmount,
            status = status,
            paymentMethod = paymentMethod
        )
        
        // ID 값 설정을 위한 리플렉션 사용
        val field = Reservation::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(reservation, id)
        
        // paymentId 설정
        if (paymentId != null) {
            val paymentIdField = Reservation::class.java.getDeclaredField("paymentId")
            paymentIdField.isAccessible = true
            paymentIdField.set(reservation, paymentId)
        }
        
        // 생성 시간 설정
        val createdAtField = Reservation::class.java.getDeclaredField("createdAt")
        createdAtField.isAccessible = true
        createdAtField.set(reservation, createdAt)
        
        return reservation
    }

    /**
     * 테스트용 결제 생성
     */
    fun createPayment(
        id: Long = 1L,
        member: Member = createMember(),
        reservation: Reservation = createReservation(member = member),
        amount: BigDecimal = reservation.totalAmount,
        status: PaymentStatus = PaymentStatus.COMPLETED,
        paymentMethod: String = "CREDIT_CARD",
        transactionId: String = "test-transaction-id-${System.currentTimeMillis()}",
        paidAt: LocalDateTime = LocalDateTime.now()
    ): Payment {
        val payment = Payment(
            member = member,
            reservation = reservation,
            amount = amount,
            status = status,
            paymentMethod = paymentMethod,
            transactionId = transactionId,
            paidAt = paidAt
        )
        
        // ID 값 설정을 위한 리플렉션 사용
        val field = Payment::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(payment, id)
        
        return payment
    }

    /**
     * 테스트용 알림 생성
     */
    fun createNotification(
        id: Long = 1L,
        member: Member = createMember(),
        type: String = "RESERVATION_CONFIRMATION",
        title: String = "예약 확인",
        content: String = "예약이 확인되었습니다",
        status: NotificationStatus = NotificationStatus.SENT,
        link: String = "/reservations/1",
        sentAt: LocalDateTime = LocalDateTime.now()
    ): Notification {
        val notification = Notification(
            member = member,
            type = type,
            title = title,
            content = content,
            status = status,
            link = link
        )
        
        // ID 값 설정을 위한 리플렉션 사용
        val field = Notification::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(notification, id)
        
        // 전송 시간 설정
        val sentAtField = Notification::class.java.getDeclaredField("sentAt")
        sentAtField.isAccessible = true
        sentAtField.set(notification, sentAt)
        
        return notification
    }
} 