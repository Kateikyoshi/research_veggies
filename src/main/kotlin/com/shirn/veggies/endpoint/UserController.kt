package com.shirn.veggies.endpoint

import com.shirn.veggies.db.UserDb
import com.shirn.veggies.db.UserRepository
import com.shirn.veggies.db.UserRole
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.renderAndAwait

@DependsOnDatabaseInitialization
@Component
class UserController(
    private val userRepository: UserRepository,
    //https://docs.spring.io/spring-framework/reference/web/webflux-functional.html#webflux-fn-handler-validation
    //https://stackoverflow.com/questions/62118831/spring-webflux-and-thymeleaf-form-validation-messages
    private val validator: Validator,
    private val passwordEncoder: PasswordEncoder
) {

    @PostConstruct
    fun createAdmin() {
        userRepository.save(
            UserDb(
                name = "admin",
                password = passwordEncoder.encode("admin"),
                role = UserRole.SCOPE_ADMIN.name
            )
        ).subscribe()
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
            password = formData["password"]?.firstOrNull()
        )

        val errors: Errors = BeanPropertyBindingResult(regUser, "regUser")
        validator.validate(regUser, errors)
        if (errors.hasErrors()) {
            model["${BindingResult::class.java.name}.regUser"] = errors
            return ok().renderAndAwait("register", model)
        }

        val saved = userRepository.save(
            regUser.copy(password = passwordEncoder.encode(regUser.password ?: ""))
        ).awaitSingleOrNull()
        model["globalAlert"] = saved?.name ?: ""

        return ok().renderAndAwait("register", model)
    }
}