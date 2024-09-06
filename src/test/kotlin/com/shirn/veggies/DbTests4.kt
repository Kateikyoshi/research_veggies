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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.test.context.ActiveProfiles

//https://stackoverflow.com/questions/69921762/spring-testconfiguration-affecting-other-test-classes
//@TestConfiguration //this configuration is being used by others automatically, which is bad
//so just import it manually
@ComponentScan(
    basePackageClasses = [UserDb::class] //this narrows down the scan and actually leaves us without automatic configs
)
@EnableR2dbcRepositories
class ConfigForTest2 {

    @Bean
    fun connectionFactory(): ConnectionFactory {
        return ConnectionFactoryBuilder.withUrl("r2dbc:h2:mem:///unhingedDb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
            .build()
    }

    @Bean
    fun r2dbcEntityTemplate(connectionFactory: ConnectionFactory): R2dbcEntityTemplate {
        return R2dbcEntityTemplate(connectionFactory)
    }

    @Bean
    fun initializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(connectionFactory)

        val populator = CompositeDatabasePopulator()
        populator.setPopulators(ResourceDatabasePopulator(ClassPathResource("schema-test-h2.sql")))

        initializer.setDatabasePopulator(populator)
        return initializer
    }
}

/**
 * This test uses different approach to implement r2dbc interfaces.
 */
//@Import(ConfigForTest2::class)
@SpringBootTest(
    classes = [
        ConfigForTest2::class
    ],
    properties = [
        "spring.r2dbc.url=r2dbc:h2:mem:///dbTestDb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    ]
)
@ActiveProfiles("test")
class DbTests4 {

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