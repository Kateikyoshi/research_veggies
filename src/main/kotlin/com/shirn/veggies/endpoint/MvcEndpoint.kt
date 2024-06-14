package com.shirn.veggies.endpoint

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.shirn.veggies.db.*
import com.shirn.veggies.endpoint.model.VeggieHtml
import com.shirn.veggies.endpoint.model.VeggieType
import com.shirn.veggies.security.JwtConfig
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

/**
 * For MVC stuff, html, you know...
 */
@ConditionalOnProperty(
    name = ["controller.mvc.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@Controller
class CoroutinesViewController(
    private val veggieRepository: VeggieRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtConfig: JwtConfig
) {

    /**
     * Any fun annotated w/ @ModelAttribute will modify
     * all the models. Not very useful in this case.
     */
    //@ModelAttribute
    fun addRegUser(model: Model) {
        model["regUser"] = UserDb(name = "josh")
    }

    @GetMapping("/register")
    suspend fun provideRegisterPage(model: Model): String {
        model["regUser"] = UserDb(name = "Please")
        return "register"
    }

    /**
     * Not used directly, called via form
     */
    @PostMapping("/register")
    suspend fun registerUser(
        @ModelAttribute("regUser") @Valid userForm: UserDb,
        errors: BindingResult,
        model: Model
    ): String {
        if (errors.hasErrors()) return "register"

        val userFormComplete = userForm.copy(role = UserRole.SCOPE_BASIC.name)

        val saved = userRepository.save(
            userFormComplete.copy(
                role = UserRole.SCOPE_BASIC.name,
                password = passwordEncoder.encode(userFormComplete.password ?: ""),
                jwt = generateJwt(userFormComplete.role?.removePrefix("SCOPE_") ?: "")
            )
        ).awaitSingleOrNull()
        model["globalAlert"] = saved?.name ?: ""

        return "register"
    }

    @GetMapping("/veggie-manage")
    suspend fun provideVeggieManagePage(model: Model): String {
        model["veggieForm"] = VeggieHtml()
        model["veggiesSelect"] = VeggieType.entries.map { it.name }
        return "veggie"
    }

    /**
     * Not used directly, called via form
     */
    @PostMapping("/veggieForm")
    suspend fun createVeggieForm(
        @ModelAttribute("veggieForm") @Valid veggieHtml: VeggieHtml,
        errors: BindingResult,
        model: Model
    ): String {
        if (errors.hasErrors()) return "veggie"

        val saved = veggieRepository.save(
            VeggieDb(
                type = veggieHtml.type?.dbId ?: throw IllegalStateException("No type provided for a veggie in a form?"),
                name = veggieHtml.name ?: throw IllegalStateException("No name provided for a veggie in a form?"),
            )
        )
        saved.id ?: throw IllegalStateException("Id can't be null! Is DB down?")
        model["veggieForm"] = VeggieHtml()
        model["veggiesSelect"] = VeggieType.entries.map { it.name }
        model["savedName"] = saved.name

        return "veggie"
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

/**
 * For REST
 */
//coroutines + @RestController
//https://docs.spring.io/spring-framework/reference/languages/kotlin/coroutines.html#controllers
//mapping body
//https://docs.spring.io/spring-framework/reference/web/webflux/controller/ann-methods/requestbody.html
@ConditionalOnProperty(
    name = ["controller.mvc.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@RestController
class NotFnEndpoint(
    private val veggieRepository: VeggieRepository,
    private val userRepository: UserRepository
) {

    @GetMapping("/api/veggie")
    suspend fun apiVeggieGet(@RequestParam(required = false) id: Long): VeggieDb {
        return veggieRepository.findById(id) ?: VeggieDb()
    }

    @GetMapping("/api/veggies")
    fun apiVeggieGetAll(model: Model): Flow<VeggieHtml> {
        return veggieRepository.findAll().map { VeggieHtml.fromVeggieDb(it) }
    }

    @PostMapping("/api/veggie", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun apiVeggieCreate(@RequestBody veggieHtml: VeggieHtml): String {

        val saved = veggieRepository.save(
            VeggieDb(
                type = veggieHtml.type?.dbId ?: throw IllegalStateException("Type can't be null"),
                name = veggieHtml.name ?: throw IllegalStateException("Name can't be null")
            )
        )
        val id = saved.id ?: throw IllegalStateException("Id can't be null! Is DB down...?")

        return """{ "id":"$id" }"""
    }

    @PostMapping("/api/user", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun apiUserGet(@RequestBody userDb: UserDb): UserDb {
        val name = userDb.name ?: throw IllegalStateException("Name can't be null")

        return userRepository.findByName(name).awaitSingleOrNull() ?: throw IllegalStateException("User doesn't exist")
    }
}