package com.shirn.veggies.db

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("veggies")
data class VeggieDb(
    @Column("n_id")
    private val id: Long? = null,

    @Column("type")
    private val type: VeggieType? = null,

    @Column("name")
    private val name: String? = null
)