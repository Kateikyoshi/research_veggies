package com.shirn.veggies.db

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("uzer")
data class UserDb(
    @Id
    @Column("n_id")
    val id: Long? = null,
    @get:NotBlank
    @get:Length(min = 3, max = 20)
    @Column("c_name")
    val name: String? = null,
    @get:NotBlank
    @get:Length(min = 3, max = 20)
    @Column("c_password")
    val password: String? = null,
    @Column("c_role")
    val role: String? = null
)