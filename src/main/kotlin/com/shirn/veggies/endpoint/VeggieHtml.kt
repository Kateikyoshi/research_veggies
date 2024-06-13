package com.shirn.veggies.endpoint

import com.shirn.veggies.db.VeggieDb

/**
 * Displays type as string
 */
data class VeggieHtml(
    val id: Long? = null,
    val type: VeggieType? = null,
    val name: String? = null
) {

    companion object {
        fun fromVeggieDb(veggieDb: VeggieDb) = VeggieHtml(
            id = veggieDb.id,
            type = VeggieType.fromId(veggieDb.type ?: 0L),
            name = veggieDb.name
        )
    }
}