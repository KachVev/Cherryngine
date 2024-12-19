package ru.cherryngine.engine.core

import jakarta.inject.Singleton

@Singleton
class EngineConfig {

    val data = Data()

    fun get() = data

    data class Data(
        val address: String = "0.0.0.0",
        val port: Int = 25565,
        val sentry: SentryConf? = null,
    ) {
        data class SentryConf(
            val dsn: String,
            val env: String = "dev"
        )
    }
}
