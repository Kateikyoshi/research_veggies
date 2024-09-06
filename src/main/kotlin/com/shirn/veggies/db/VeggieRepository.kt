package com.shirn.veggies.db

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface VeggieRepository: CoroutineCrudRepository<VeggieDb, Long>