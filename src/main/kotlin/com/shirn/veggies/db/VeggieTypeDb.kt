package com.shirn.veggies.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("veggie_type")
data class VeggieTypeDb(
    @Id
    @Column("n_id")
    val id: Long? = null,

    @Column("c_name")
    val name: String? = null
)