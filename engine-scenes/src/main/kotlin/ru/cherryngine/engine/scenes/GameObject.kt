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

    val parent: GameObject?
        get() {
            val graph = scene.parentGraph
            val parentId = graph.incomingEdgesOf(id).firstOrNull()?.let(graph::getEdgeSource)
            return parentId?.let { scene.gameObjects[it] }
        }

    val children: List<GameObject>
        get() {
            val graph = scene.parentGraph
            return graph.outgoingEdgesOf(id).asSequence()
                .map { graph.getEdgeTarget(it) }
                .mapNotNull { scene.gameObjects[it] }
                .toList()
        }


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
        val graph = scene.parentGraph
        val oldParentEdge = graph.incomingEdgesOf(child).firstOrNull()
        oldParentEdge?.let(graph::removeEdge)
        graph.addEdge(this.id, child)
    }

    fun removeAllChildren() {
        val graph = scene.parentGraph
        graph.outgoingEdgesOf(id).forEach(graph::removeEdge)
    }

    fun removeChild(child: UUID) {
        scene.parentGraph.removeEdge(id, child)
    }

    fun destroy() {
        _modules.values.forEach(Module::destroy)
        _modules.clear()
        removeAllChildren()
    }
}
