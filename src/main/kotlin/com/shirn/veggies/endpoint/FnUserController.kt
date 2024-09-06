package com.shirn.veggies.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.shirn.veggies.db.UserDb
import com.shirn.veggies.db.UserRepository
import com.shirn.veggies.db.UserRole
import com.shirn.veggies.security.JwtHelper
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Controller
class FnUserController(
    private val userRepository: UserRepository,
    //https://docs.spring.io/spring-framework/reference/web/webflux-functional.html#webflux-fn-handler-validation
    //https://stackoverflow.com/questions/62118831/spring-webflux-and-thymeleaf-form-validation-messages
    private val validator: Validator,
    private val passwordEncoder: PasswordEncoder,
    private val jwtHelper: JwtHelper,
    private val mapper: ObjectMapper
) {

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
                jwt = jwtHelper.generateJwt(regUser.role?.removePrefix("SCOPE_") ?: "")
            )
        ).awaitSingleOrNull()
        model["globalAlert"] = saved?.name ?: ""

        return ok().renderAndAwait("register", model)
    }


}