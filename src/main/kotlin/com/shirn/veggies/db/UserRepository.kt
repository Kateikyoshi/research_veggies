package com.shirn.veggies.db

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserRepository: CoroutineCrudRepository<UserDb, Long> {
}