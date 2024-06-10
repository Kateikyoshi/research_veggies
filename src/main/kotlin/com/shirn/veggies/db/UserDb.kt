package com.shirn.veggies.db

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("uzer")
data class UserDb(
    @Id
    @Column("n_id")
    val id: Long? = null,
    @get:NotNull
    @get:Min(3)
    @get:Max(20)
    @Column("c_name")
    val name: String? = null,
    @get:NotNull
    @get:Min(5)
    @get:Max(20)
    @Column("c_password")
    val password: String? = null
)