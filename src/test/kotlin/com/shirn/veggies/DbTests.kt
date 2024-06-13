package com.shirn.veggies

import com.shirn.veggies.db.*
import com.shirn.veggies.endpoint.VeggieType
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories
class DatabaseConfig : AbstractR2dbcConfiguration() {
    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryBuilder.withUrl("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE").build()
    }
}

@Import(DatabaseConfig::class)
@SpringBootTest
class DbTests {

    @BeforeEach
    fun populateDb() {
        runBlocking {
            val types = VeggieType.entries.map {
                VeggieTypeDb(
                    id = it.dbId,
                    name = it.name
                )
            }

            if (veggieTypeRepository.count() == 0L) {
                veggieTypeRepository.saveAll(types.asFlow())
            }
        }
    }

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var veggieRepository: VeggieRepository

    @Autowired
    private lateinit var veggieTypeRepository: VeggieTypeRepository

    @Test
    fun `user repo test`() = runTest {

        val saved = userRepository.save(UserDb(name = "bilbo", password = "baggins")).awaitSingleOrNull()
        assertThat(saved).isNotNull

        val found = userRepository.findById(saved?.id ?: -1).awaitSingleOrNull()
        assertThat(found).isNotNull
        assertThat(found?.id).isNotNull()
        assertThat(found?.name).isEqualTo("bilbo")
        assertThat(found?.password).isEqualTo("baggins")
    }

    @Test
    fun `veggie repo test`() = runTest {

        val saved = veggieRepository.save(VeggieDb(type = VeggieType.TOMATO.dbId, name = "tomatoRed"))
        assertThat(saved).isNotNull

        val found = veggieRepository.findById(saved.id ?: -1)
        assertThat(found).isNotNull
        assertThat(found?.id).isNotNull()
        assertThat(found?.type).isEqualTo(VeggieType.TOMATO.dbId)
        assertThat(found?.name).isEqualTo("tomatoRed")
    }

}