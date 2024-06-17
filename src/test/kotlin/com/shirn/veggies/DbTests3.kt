package com.shirn.veggies

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
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles

@ComponentScan
@Configuration
class ConfigForTest

/**
 * This test uses different approach to implement r2dbc interfaces.
 */
@SpringBootTest(
    classes = [
        ConfigForTest::class
    ],
    properties = [
        "spring.r2dbc.url=r2dbc:h2:mem:///dbTestDb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    ]
)
@AutoConfigureDataR2dbc
@ActiveProfiles("test")
class DbTests3 {

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
}