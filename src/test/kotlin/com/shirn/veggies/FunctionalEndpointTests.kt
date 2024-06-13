package com.shirn.veggies

import com.shirn.veggies.db.UserRepository
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@AutoConfigureWebTestClient
@SpringBootTest
class FunctionalEndpointTests {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var userRepository: UserRepository

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