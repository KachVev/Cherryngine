package ru.cherryngine.engine.scenes

import io.micronaut.context.ApplicationContext
import ru.cherryngine.engine.scenes.event.EventBus
import ru.cherryngine.engine.scenes.modules.TransformModule
import java.util.*
import kotlin.reflect.KClass

class GameObject(
    private val applicationContext: ApplicationContext,
    val scene: Scene,
) {
    val id: UUID = UUID.randomUUID()

    val transform: TransformModule = TransformModule(this)

    val bus: EventBus
        get() = scene.bus

    private val modules: MutableMap<KClass<out Module>, Module> = hashMapOf(
        TransformModule::class to transform
    )

    fun <T : Module> getOrCreateModule(clazz: KClass<T>, vararg args: Any): Module {
        return modules.computeIfAbsent(clazz) {
            applicationContext.createBean(clazz.java, this, *args).apply { enable() }
        }
    }

    fun <T : Module> getModule(clazz: KClass<T>): Module? {
        return modules[clazz]
    }

    fun destroy() {
        modules.values.forEach(Module::destroy)
        modules.clear()
    }
}
