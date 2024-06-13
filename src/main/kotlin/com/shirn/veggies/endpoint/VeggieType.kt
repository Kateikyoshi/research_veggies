package com.shirn.veggies.endpoint

enum class VeggieType(val dbId: Long) {
    TOMATO(0),
    CUCUMBER(1),
    BANANA(2),
    ONION(3);

    companion object {
        fun fromName(name: String): VeggieType {
            return entries.find { it.name == name } ?: throw IllegalStateException("Can't find a suitable veggie with $name")
        }
        fun fromId(id: Long): VeggieType {
            return entries.find { it.dbId == id } ?: throw IllegalStateException("Can't find a suitable veggie with $id")
        }
    }
}