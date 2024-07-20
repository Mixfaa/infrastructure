package com.mixfa.infrastructure

import arrow.fx.coroutines.fixedThreadPoolContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mixfa.infrastructure.misc.ClientContext
import com.mixfa.infrastructure.misc.ClientContextManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.core.CoroutinesUtils
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import kotlin.coroutines.CoroutineContext


@SpringBootApplication(
    exclude = [
        SecurityAutoConfiguration::class,
    ]
)
@EnableScheduling
class InfrastructureApplication {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun logger(injectionPoint: InjectionPoint): Logger {
        return LoggerFactory.getLogger(injectionPoint.methodParameter!!.containingClass)
    }

    @Bean
    fun clientContext(mngr: ClientContextManager) = ClientContext(mngr)

    @Bean
    fun clientContextManager() = ClientContextManager()

    @Bean
    fun coroutineScope(): CoroutineContext = Dispatchers.IO + Job()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Bean
    fun asyncServerSocketChannel(@Value("\${application.port}") port: Int): AsynchronousServerSocketChannel =
        AsynchronousServerSocketChannel.open().apply {
            this.bind(InetSocketAddress("127.0.0.1", port))
        }


}

fun main(args: Array<String>) {
    runApplication<InfrastructureApplication>(*args)
}