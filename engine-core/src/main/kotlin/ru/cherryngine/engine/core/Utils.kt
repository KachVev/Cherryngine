package ru.cherryngine.engine.core

import net.minestom.server.registry.DynamicRegistry

fun <T : Any> DynamicRegistry<T>.getId(value: T): Int? = getKey(value)?.let { getId(it) }