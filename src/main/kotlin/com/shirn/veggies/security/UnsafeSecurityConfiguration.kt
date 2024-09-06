package com.shirn.veggies.security

import com.shirn.veggies.db.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebFluxSecurity
class UnsafeSecurityConfiguration {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            //https://docs.spring.io/spring-security/reference/reactive/authorization/authorize-http-requests.html
            authorizeExchange {
                authorize("/api/**", hasAnyAuthority(UserRole.SCOPE_ADMIN.name, UserRole.SCOPE_BASIC.name))
                authorize(pathMatchers("/", "/register"), permitAll)

                authorize(pathMatchers(
                    "/veggie-manage", "/veggie", "/veggieForm", "/veggie-manage"
                ), hasAnyAuthority(UserRole.SCOPE_ADMIN.name, UserRole.SCOPE_BASIC.name))

                authorize(anyExchange, denyAll)
            }
            oauth2ResourceServer {
                jwt {  }
            }
            formLogin { }
            httpBasic { }
            logout {  }
            csrf { disable() }
        }
    }

    class JwtAudienceValidator: OAuth2TokenValidator<Jwt> {
        private val error: OAuth2Error = OAuth2Error("invalid_token", "The required audience is missing", null)

        override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
            return if (jwt.audience.contains("veggies")) {
                OAuth2TokenValidatorResult.success()
            } else {
                OAuth2TokenValidatorResult.failure(error)
            }
        }
    }

    //https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html#webflux-oauth2resourceserver-jwt-authorization
    @Bean
    fun jwtDecoder(jwtConfig: JwtConfig): ReactiveJwtDecoder {

        val secVal = jwtConfig.secret?.value ?: throw IllegalStateException("jwt secret value can't be null")
        val secAlg = jwtConfig.secret?.alg ?: throw IllegalStateException("jwt secret alg can't be null")

        val jwtDecoder = NimbusReactiveJwtDecoder.withSecretKey(
            SecretKeySpec(secVal.toByteArray(), secAlg)
        ).build()
        val delegating: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(
            JwtAudienceValidator()
        )
        jwtDecoder.setJwtValidator(delegating)
        return jwtDecoder
    }

}