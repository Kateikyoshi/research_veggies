package com.shirn.veggies

import com.shirn.veggies.db.*
import com.shirn.veggies.endpoint.model.VeggieType
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
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.test.context.ActiveProfiles

//conflicts w/ another @Configuration in tests. So you can actually disable both and just import directly.
//https://stackoverflow.com/a/59090558
//@Configuration

//@EnableR2dbcRepositories
class DatabaseConfig : AbstractR2dbcConfiguration() {

    //@Bean("dbtestsConnFactory")
    override fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryBuilder.withUrl("r2dbc:h2:mem:///test?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE").build()
    }
}

//@Import(DatabaseConfig::class) //actually does nothing lol

//this variant works cuz @EnableAutoConfiguration takes a guess what to load and uses tons of AutoConfigs
//to check what it uses you can use 'app.yml', add 'debug: true' and it will show in console
//most certainly you will see R2DBC-related autos
//@SpringBootTest(properties = ["spring.r2dbc.url=r2dbc:h2:mem:///dbTestDb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"])
//@EnableAutoConfiguration

@DataR2dbcTest(properties = ["spring.r2dbc.url=r2dbc:h2:mem:///dbTestDb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"])
@ActiveProfiles("test")
class DbTests {

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
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var veggieRepository: VeggieRepository

    @Autowired
    private lateinit var veggieTypeRepository: VeggieTypeRepository

    @Test
    fun `user repo test`() = runTest {

        //https://docs.spring.io/spring-boot/how-to/data-initialization.html

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