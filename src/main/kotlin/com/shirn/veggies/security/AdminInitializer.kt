package com.shirn.veggies.security

import com.shirn.veggies.db.UserDb
import com.shirn.veggies.db.UserRepository
import com.shirn.veggies.db.UserRole
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtHelper: JwtHelper
) {

    /**
     * @PostConstruct sucks
     */
    @EventListener(ApplicationReadyEvent::class)
    suspend fun createAdmin() {
        val exists = userRepository.existsByName("admin").awaitSingle()
        if (exists) return

        userRepository.save(
            UserDb(
                name = "admin",
                password = passwordEncoder.encode("admin"),
                role = UserRole.SCOPE_ADMIN.name,
                jwt = jwtHelper.generateJwt(UserRole.SCOPE_ADMIN.name.removePrefix("SCOPE_"))
            )
        ).awaitSingleOrNull()
    }
}