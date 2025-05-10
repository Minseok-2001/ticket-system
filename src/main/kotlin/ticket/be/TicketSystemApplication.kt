package ticket.be

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TicketSystemApplication

fun main(args: Array<String>) {
    runApplication<TicketSystemApplication>(*args)
}
