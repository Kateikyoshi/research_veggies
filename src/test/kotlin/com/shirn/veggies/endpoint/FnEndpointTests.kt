package com.shirn.veggies.endpoint

import com.shirn.veggies.db.*
import com.shirn.veggies.endpoint.model.VeggieType
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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@Import(EndpointDatabaseConfig::class)
@AutoConfigureWebTestClient
@SpringBootTest(properties = ["controller.functional.enabled=true"])
@EnableAutoConfiguration
@ActiveProfiles("test")
class FnEndpointTests {

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

            veggieRepository.deleteAll()
        }
    }

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var veggieTypeRepository: VeggieTypeRepository

    @Autowired
    private lateinit var veggieRepository: VeggieRepository

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

        val result = webTestClient.post().uri("/api/veggie")
            .headers { it.setBearerAuth(admin?.jwt ?: "") }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .exchange()
            .expectStatus().isOk
            .expectBody<String>()
            .returnResult()

        assertThat(result.responseBody?.contains("[0-9]".toRegex())).isTrue()
    }
}