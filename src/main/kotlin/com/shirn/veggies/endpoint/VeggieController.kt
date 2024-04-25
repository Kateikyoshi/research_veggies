package com.shirn.veggies.endpoint

import kotlinx.coroutines.flow.flowOf
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok

@Component
class VeggieController {

    suspend fun getVeggie(request: ServerRequest): ServerResponse {

        return ok().bodyValueAndAwait("yeah")
    }

    suspend fun getAllVeggies(request: ServerRequest): ServerResponse {

        return ok().bodyAndAwait(flowOf("this", "That"))
    }
}