package com.shirn.veggies.db

import com.shirn.veggies.endpoint.model.VeggieType
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class VeggieInitializer(
    private val veggieTypeRepository: VeggieTypeRepository
) {

    @EventListener(ApplicationReadyEvent::class)
    suspend fun initVeggies() {
        if (veggieTypeRepository.count() == 0L) {
            val types = VeggieType.entries.map {
                VeggieTypeDb(
                    //id = it.dbId,
                    name = it.name
                )
            }
            veggieTypeRepository.saveAll(types.asFlow()).collect()
        }
    }
}