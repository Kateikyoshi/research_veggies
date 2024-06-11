package com.shirn.veggies.security

import com.shirn.veggies.db.UserRepository
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class R2dbcUserDetailsService(private val userRepository: UserRepository): ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        return userRepository.findByName(username).map {
            User.builder()
                .username(it.name)
                .password(it.password)
                .authorities("SCOPE_BASIC")
                .build()
        }
    }
}