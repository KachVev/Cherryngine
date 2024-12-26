package ru.cherryngine.engine.scenes

import io.micronaut.context.ApplicationContext
import ru.cherryngine.engine.scenes.modules.TransformModule
import java.util.*
import kotlin.reflect.KClass

class GameObject(
    private val applicationContext: ApplicationContext,
    val scene: Scene,
) {
    val id: UUID = UUID.randomUUID()

    val transform: TransformModule = TransformModule(this)

    private val _modules: MutableMap<KClass<out Module>, Module> = hashMapOf(
        TransformModule::class to transform
    )

    val modules get() = _modules.values.toList()

    fun <T : Module> getOrCreateModule(clazz: KClass<T>, vararg args: Any): Module {
        return _modules.computeIfAbsent(clazz) {
            applicationContext.createBean(clazz.java, this, *args).apply(Module::enable)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Module> getModule(clazz: KClass<T>): T? {
        return _modules[clazz] as T?
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Module> getModules(clazz: KClass<T>): List<T> {
        return _modules.values.filter {
            clazz.isInstance(it)
        }.map { it as T }.toList()
    }

    fun destroy() {
        _modules.values.forEach(Module::destroy)
        _modules.clear()
    }
}
