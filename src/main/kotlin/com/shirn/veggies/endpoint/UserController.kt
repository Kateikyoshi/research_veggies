package com.shirn.veggies.endpoint

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.shirn.veggies.db.UserDb
import com.shirn.veggies.db.UserRepository
import com.shirn.veggies.db.UserRole
import com.shirn.veggies.security.JwtConfig
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok

@DependsOnDatabaseInitialization
@Component
class UserController(
    private val userRepository: UserRepository,
    //https://docs.spring.io/spring-framework/reference/web/webflux-functional.html#webflux-fn-handler-validation
    //https://stackoverflow.com/questions/62118831/spring-webflux-and-thymeleaf-form-validation-messages
    private val validator: Validator,
    private val passwordEncoder: PasswordEncoder,
    private val jwtConfig: JwtConfig,
    private val mapper: ObjectMapper
) {

    @PostConstruct
    fun createAdmin() {
        userRepository.save(
            UserDb(
                name = "admin",
                password = passwordEncoder.encode("admin"),
                role = UserRole.SCOPE_ADMIN.name,
                jwt = generateJwt(UserRole.SCOPE_ADMIN.name.removePrefix("SCOPE_"))
            )
        ).subscribe()
    }

    suspend fun getUserApi(request: ServerRequest): ServerResponse {

        val body = request.awaitBodyOrNull<String>() ?: throw IllegalStateException("Body can't be null")
        val node = mapper.readTree(body) ?: throw IllegalStateException("Tree can't be null")
        val name = node.get("name").textValue() ?: throw IllegalStateException("Name can't be null")

        val user = userRepository.findByName(name).awaitSingleOrNull() ?: throw IllegalStateException("User doesn't exist")

        return ok().bodyValueAndAwait(user)
    }

    suspend fun provideRegisterPage(request: ServerRequest): ServerResponse {

        val model: MutableMap<String, Any> = hashMapOf("regUser" to UserDb())

        return ok().renderAndAwait("register", model)
    }

    suspend fun register(request: ServerRequest): ServerResponse {

        val model: MutableMap<String, Any> = hashMapOf("regUser" to UserDb())

        val formData = request.awaitFormData()
        val regUser = UserDb(
            name = formData["name"]?.firstOrNull(),
            password = formData["password"]?.firstOrNull(),
            role = UserRole.SCOPE_BASIC.name
        )

        val errors: Errors = BeanPropertyBindingResult(regUser, "regUser")
        validator.validate(regUser, errors)
        if (errors.hasErrors()) {
            model["${BindingResult::class.java.name}.regUser"] = errors
            return ok().renderAndAwait("register", model)
        }

        val saved = userRepository.save(
            regUser.copy(
                password = passwordEncoder.encode(regUser.password ?: ""),
                jwt = generateJwt(regUser.role?.removePrefix("SCOPE_") ?: "")
            )
        ).awaitSingleOrNull()
        model["globalAlert"] = saved?.name ?: ""

        return ok().renderAndAwait("register", model)
    }

    private fun generateJwt(scp: String): String {

        val secret = jwtConfig.secret ?: throw IllegalStateException("jwt config secret can't be null")
        val secretValue = secret.value ?: throw IllegalStateException("jwt config secret value can't be null")
        val secretAlg = secret.alg ?: throw IllegalStateException("jwt config secret alg can't be null")

        val alg = if (secretAlg == "HS256") {
            Algorithm.HMAC256(secretValue)
        } else throw IllegalStateException("jwt secret alg $secretAlg is not supported")

        return JWT.create()
            .withIssuer("senechal")
            .withAudience("veggies")
            .withClaim("scp", scp) //by default will be mapped automatically into SCOPE_<name> authority
            .sign(alg)
    }
}