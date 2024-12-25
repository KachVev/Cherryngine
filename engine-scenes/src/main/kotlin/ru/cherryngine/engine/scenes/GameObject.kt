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

    private val modules_: MutableMap<KClass<out Module>, Module> = hashMapOf(
        TransformModule::class to transform
    )

    val modules get() = modules_.values.toList()

    fun <T : Module> getOrCreateModule(clazz: KClass<T>, vararg args: Any): Module {
        return modules_.computeIfAbsent(clazz) {
            applicationContext.createBean(clazz.java, this, *args).apply(Module::enable)
        }
    }

    fun <T : Module> getModule(clazz: KClass<T>): Module? {
        return modules_[clazz]
    }

    fun destroy() {
        modules_.values.forEach(Module::destroy)
        modules_.clear()
    }
}
