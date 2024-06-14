package com.shirn.veggies.endpoint

enum class VeggieType(val dbId: Long) {
    TOMATO(1),
    CUCUMBER(2),
    BANANA(3),
    ONION(4);

    companion object {
        fun fromName(name: String): VeggieType {
            return entries.find { it.name == name } ?: throw IllegalStateException("Can't find a suitable veggie with $name")
        }
        fun fromId(id: Long): VeggieType {
            return entries.find { it.dbId == id } ?: throw IllegalStateException("Can't find a suitable veggie with $id")
        }
    }
}