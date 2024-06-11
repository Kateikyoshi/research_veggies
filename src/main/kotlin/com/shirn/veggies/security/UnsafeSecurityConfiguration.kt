package com.shirn.veggies.security

import com.shirn.veggies.db.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers

@Configuration
@EnableWebFluxSecurity
class HelloWebfluxSecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            //https://docs.spring.io/spring-security/reference/reactive/authorization/authorize-http-requests.html
            authorizeExchange {
                authorize("api/**", permitAll)
                authorize(pathMatchers("/", "/register"), permitAll)

                authorize(pathMatchers(
                    "/veggie-manage", "/veggie", "/veggieForm", "/veggie-manage"
                ), hasAnyAuthority(UserRole.SCOPE_ADMIN.name, UserRole.SCOPE_BASIC.name))

                authorize(anyExchange, denyAll)
            }
            formLogin { }
            httpBasic { }
            csrf { disable() }
        }
    }

}