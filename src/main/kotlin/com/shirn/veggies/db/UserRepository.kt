package com.shirn.veggies.db

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface UserRepository: ReactiveCrudRepository<UserDb, Long> {

    fun findByName(name: String): Mono<UserDb>
}