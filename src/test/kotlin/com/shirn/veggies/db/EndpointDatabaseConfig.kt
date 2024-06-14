package com.shirn.veggies.db

import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

/**
 * conflicts w/ another @Configuration in tests. So you can actually disable both and just import directly.
 * https://stackoverflow.com/a/59090558
 *
 * This DB is used both by mvc and fn endpoint tests.
 */
@EnableR2dbcRepositories
class EndpointDatabaseConfig : AbstractR2dbcConfiguration() {

    @Bean("endpointConnFactory")
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryBuilder.withUrl("r2dbc:h2:mem:///test2?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
            .build()
    }
}