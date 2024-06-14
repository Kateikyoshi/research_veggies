package com.shirn.veggies

import com.shirn.veggies.db.UserRepository
import com.shirn.veggies.db.VeggieTypeDb
import com.shirn.veggies.db.VeggieTypeRepository
import com.shirn.veggies.endpoint.VeggieType
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

//@Configuration
@EnableR2dbcRepositories
class DatabaseConfig2 : AbstractR2dbcConfiguration() {
    @Bean("god")
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryBuilder.withUrl("r2dbc:h2:mem:///test2?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE").build()
    }
}

@Import(DatabaseConfig2::class)
@AutoConfigureWebTestClient
@SpringBootTest
@EnableAutoConfiguration
@ActiveProfiles("test")
class FunctionalEndpointTests {

    /**
     * This is needed if VEGGIE_TYPE population
     * doesn't happen in schema-test-h2.sql.
     * For example when same DB is used in all tests.
     * And it is just reset between each.
     */
    @BeforeEach
    fun populateDb() {
        runBlocking {
            val types = VeggieType.entries.map {
                VeggieTypeDb(
                    //id = it.dbId,
                    name = it.name
                )
            }

            if (veggieTypeRepository.count() == 0L) {
                veggieTypeRepository.saveAll(types.asFlow()).collect()
            }
        }
    }

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var veggieTypeRepository: VeggieTypeRepository

    //https://docs.spring.io/spring-framework/reference/testing/webtestclient.html
    @Test
    fun `create veggie`() = runTest {

        val admin = userRepository.findByName("admin").awaitSingleOrNull()
        assertThat(admin).isNotNull

        val body = """
            {
                "type": "TOMATO",
                "name": "tomm1"
            }
        """.trimIndent()

        webTestClient.post().uri("/api/veggie")
            .headers { it.setBearerAuth(admin?.jwt ?: "") }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .json("""{"id":"1"}""")
    }
}