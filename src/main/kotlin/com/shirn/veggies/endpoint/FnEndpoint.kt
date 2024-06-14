package com.shirn.veggies.endpoint

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

/**
 * Built using
 * https://docs.spring.io/spring-framework/reference/web/webflux-functional.html
 * https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#web.reactive.webflux.template-engines
 */
@ConditionalOnProperty(
    name = ["controller.functional.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@Configuration(proxyBeanMethods = false)
class MyRoutingConfiguration {

    @Bean
    fun monoRouterFunction(fnVeggieController: FnVeggieController, fnUserController: FnUserController) = coRouter {
        GET("/api/veggie", queryParam("id") { true }, fnVeggieController::getVeggieApi2)
        GET("/api/veggies", fnVeggieController::getAllVeggies)
        accept(APPLICATION_JSON).nest {
            POST("/api/veggie", fnVeggieController::createVeggieApi)
            POST("/api/user", fnUserController::getUserApi)
        }
        GET("/register", fnUserController::provideRegisterPage)
        POST("/register", fnUserController::register)

        GET("/", fnVeggieController::provideIndex)

        POST("/veggieForm", fnVeggieController::createVeggieForm)
        GET("/veggie-manage", fnVeggieController::provideVeggiePage)

        onError<Throwable> { throwable, serverRequest ->

            log.error("Wow, that was bad at ${serverRequest.uri()} w/ ${throwable.message}")

            ok().bodyValueAndAwait("""
                {"message":"something went wrong"}
            """.trimIndent())
        }
    }


    companion object {
        private val log = LoggerFactory.getLogger(MyRoutingConfiguration::class.java)
    }
}