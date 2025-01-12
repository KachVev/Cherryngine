package ru.cherryngine.engine.scenes

import io.micronaut.context.ApplicationContext
import ru.cherryngine.engine.scenes.modules.TransformModule
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.reflect.KClass

class GameObject(
    private val applicationContext: ApplicationContext,
    val scene: Scene,
) {

    val id: UUID = UUID.randomUUID()

    val transform: TransformModule = TransformModule(this)

    val parent: GameObject?
        get() {
            val parentKey = scene.parents.keys.find { scene.parents.getValue(it).contains(id) }
            return parentKey?.let { scene.gameObjects[it] }
        }

    val children: List<GameObject>
        get() = scene.parents.mapNotNull { scene.gameObjects[it.key] }.toList()


    private val _modules: MutableMap<KClass<out Module>, Module> = hashMapOf(
        TransformModule::class to transform
    )

    val modules get() = _modules.values.toList()

    @Suppress("UNCHECKED_CAST")
    fun <T : Module> getOrCreateModule(clazz: KClass<T>, vararg args: Any): T {
        return _modules.computeIfAbsent(clazz) {
            applicationContext.createBean(clazz.java, this, *args).apply(Module::enable)
        } as T
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

//    fun addChild(child: UUID) {
//        scene.parents.computeIfAbsent(id) { HashSet() }.add(child)
//    }

    fun addChild(child: UUID) {
        scene.gameObjects[child]?.let {
            val parent = it.parent
            if (parent != null) {
                scene.parents[parent.id]?.remove(child)
            }
            scene.parents.computeIfAbsent(id) { HashSet() }.add(child)
        }
    }

    fun removeAllChildren() {
        scene.parents[id]?.clear()
    }

    fun removeChild(child: UUID) {
        scene.parents[id]?.remove(child)
    }

    fun destroy() {
        _modules.values.forEach(Module::destroy)
        _modules.clear()
        removeAllChildren()
    }
}
