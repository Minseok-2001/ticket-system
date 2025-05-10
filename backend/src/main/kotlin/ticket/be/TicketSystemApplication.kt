package ticket.be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TicketSystemApplication

fun main(args: Array<String>) {
    runApplication<TicketSystemApplication>(*args)
}
