package no.nav.ba.e2e.autotest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class AutotestApplication


fun main(args: Array<String>) {
	runApplication<AutotestApplication>(*args)
}