package com.shirn.veggies.endpoint

import org.slf4j.LoggerFactory
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
@Configuration(proxyBeanMethods = false)
class MyRoutingConfiguration {

    @Bean
    fun monoRouterFunction(veggieController: VeggieController, userController: UserController) = coRouter {
        GET("api/veggie", queryParam("id") { true }, veggieController::getVeggieApi2)
        accept(APPLICATION_JSON).nest {
            POST("api/veggie", veggieController::createVeggieApi)
            POST("api/user", userController::getUserApi)
        }
        GET("/register", userController::provideRegisterPage)
        POST("/register", userController::register)

        GET("/", veggieController::provideIndex)
        GET("/veggie", veggieController::getAllVeggies)
        POST("/veggieForm", veggieController::createVeggieForm)
        GET("/veggie-manage", veggieController::provideVeggiePage)

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