package com.shirn.veggies

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VeggiesApplication

fun main(args: Array<String>) {
	runApplication<VeggiesApplication>(*args)
}
