package com.pos.PDPdb

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication
class PdPdbApplication

fun main(args: Array<String>) {
	runApplication<PdPdbApplication>(*args)
}
