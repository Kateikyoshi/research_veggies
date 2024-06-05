package com.shirn.veggies.db

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("veggies")
data class VeggieDb(
    @Id
    @Column("n_id")
    val id: Long? = null,

    @Column("c_type")
    val type: Int? = null,

    @Column("c_name")
    val name: String? = null
)