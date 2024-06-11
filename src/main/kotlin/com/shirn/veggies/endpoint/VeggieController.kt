package com.shirn.veggies.endpoint

import com.fasterxml.jackson.databind.ObjectMapper
import com.shirn.veggies.db.VeggieDb
import com.shirn.veggies.db.VeggieRepository
import io.klogging.Klogging
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
import kotlin.jvm.optionals.getOrNull

@Component
class VeggieController(
    private val veggieRepository: VeggieRepository,
    private val mapper: ObjectMapper
) : Klogging {

    suspend fun provideIndex(request: ServerRequest): ServerResponse {
        return ok().renderAndAwait("index")
    }

    suspend fun provideVeggiePage(request: ServerRequest): ServerResponse {

        return ok().renderAndAwait(
            "veggie",
            hashMapOf(
                "veggiesSelect" to VeggieType.entries.map { it.name },
                "veggieDb" to VeggieDb()
            )
        )
    }

    suspend fun getVeggieApi2(request: ServerRequest): ServerResponse {

        val id = request.queryParam("id").getOrNull() ?: throw IllegalArgumentException("Id can't be null")

        return ok().bodyValueAndAwait(veggieRepository.findById(id.toLong()) ?: VeggieDb())
    }

    suspend fun getVeggieApi(request: ServerRequest): ServerResponse {
        val body = request.awaitBodyOrNull<String>() ?: throw IllegalStateException("Body can't be null")
        val node = mapper.readTree(body) ?: throw IllegalStateException("Tree can't be null")
        val id = node.get("id").longValue()

        return ok().bodyValueAndAwait(veggieRepository.findById(id) ?: VeggieDb())
    }

    suspend fun getAllVeggies(request: ServerRequest): ServerResponse {

        return ok().bodyAndAwait(veggieRepository.findAll().map { VeggieHtml.fromVeggieDb(it) })
    }

    suspend fun createVeggieForm(request: ServerRequest): ServerResponse {

        val form = request.awaitFormData()
        val formName = form.getValue("name").firstOrNull()
            ?: throw IllegalStateException("No name provided for a veggie in a form?")
        val formType = form.getValue("type").firstOrNull()
            ?: throw IllegalStateException("No type provided for a veggie in a form?")

        val saved = veggieRepository.save(
            VeggieDb(
                type = VeggieType.fromName(formType).dbId,
                name = formName
            )
        )
        saved.id ?: throw IllegalStateException("Id can't be null! Is DB down?")

        return ok().renderAndAwait(
            "veggie",
            hashMapOf(
                "savedName" to saved.name,
                "veggiesSelect" to VeggieType.entries.map { it.name },
                "veggieDb" to VeggieDb()
            )
        )
    }

    suspend fun createVeggieApi(request: ServerRequest): ServerResponse {

        val body = request.awaitBodyOrNull<String>() ?: throw IllegalStateException("Body can't be null")
        val node = mapper.readTree(body) ?: throw IllegalStateException("Tree can't be null")
        val type = node.get("type").textValue() ?: throw IllegalStateException("Type can't be null")
        val name = node.get("name").textValue() ?: throw IllegalStateException("Name can't be null")

        val saved = veggieRepository.save(
            VeggieDb(
                type = VeggieType.fromName(type).dbId,
                name = name
            )
        )
        val id = saved.id ?: throw IllegalStateException("Id can't be null! Is DB down...?")

        return ok().bodyValueAndAwait(id)
    }
}