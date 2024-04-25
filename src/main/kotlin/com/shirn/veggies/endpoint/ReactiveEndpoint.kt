package com.shirn.veggies.endpoint

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.RequestPredicates.accept
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
    fun monoRouterFunction(veggieController: VeggieController) = coRouter {
        accept(APPLICATION_JSON).nest {
            GET("/veggie/{id}", veggieController::getVeggie)
            GET("/veggie", veggieController::getAllVeggies)
        }
        //POST("/veggie", veggieController::createVeggie)

        onError<Throwable> { throwable, serverRequest ->

            log.error("Wow, that was bad at ${serverRequest.uri()}")

            ok().bodyValueAndAwait("""
                {"message":"something went wrong"}
            """.trimIndent())
        }
    }


    companion object {
        private val log = LoggerFactory.getLogger(MyRoutingConfiguration::class.java)

        private val ACCEPT_JSON = accept(MediaType.APPLICATION_JSON)
    }
}