package ru.cherryngine.engine.scenes

import io.micronaut.context.ApplicationContext
import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DirectedAcyclicGraph
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.event.impl.SceneTickEvent
import java.util.*
import kotlin.reflect.KClass

class Scene(
    private val applicationContext: ApplicationContext,
    private val sceneManager: SceneManager,
    val data: Data,
) {
    val id: UUID = UUID.randomUUID()

    val gameObjects: MutableMap<UUID, GameObject> = HashMap()
    private val parentGraph: Graph<UUID, DefaultEdge> = DirectedAcyclicGraph(DefaultEdge::class.java)

    var tick = 0L

    var tickStartMils = 0L
    var tickEndMils = 0L
    var tickElapsedMils = 0L

    private var isAlive = false

    fun start() {
        isAlive = true
        Thread {
            while (isAlive) {

                tickStartMils = System.currentTimeMillis()

                tick()

                tickEndMils = System.currentTimeMillis()
                tickElapsedMils = tickEndMils - tickStartMils

                //println("$id | elapsed time = $tickElapsedMils")

                Thread.sleep(1000L / data.tps)
            }
        }.start()
    }

    fun stop() {
        isAlive = false
    }

    fun tick() {
        fireEvent(SceneTickEvent(this))
        tick++
    }

    fun createGameObject(): GameObject {
        return GameObject(applicationContext, this).apply {
            gameObjects[id] = this
            parentGraph.addVertex(id)
        }
    }

    fun destroyGameObject(id: UUID) {
        gameObjects.remove(id)?.destroy()
        parentGraph.removeVertex(id)
    }

    fun <T : Module> getModules(clazz: KClass<T>): List<T> {
        val result: MutableList<T> = LinkedList()
        gameObjects.values.filter {
            result.addAll(it.getModules(clazz))
        }.toList()
        return result
    }

    fun fireEvent(event: Event) {
        val modules = gameObjects.flatMap { it.value.modules }.groupBy { it::class }
        sceneManager.sortedModuleTypes.forEach { moduleType ->
            modules[moduleType]?.forEach { module ->
                module.onEvent(event)
            }
        }
    }

    fun getParentId(childId: UUID): UUID? {
        return parentGraph.incomingEdgesOf(childId).firstOrNull()?.let(parentGraph::getEdgeSource)
    }

    fun getChildrenIds(parentId: UUID): List<UUID> {
        return parentGraph.outgoingEdgesOf(parentId).map(parentGraph::getEdgeTarget)
    }

    fun addChild(parentId: UUID, childId: UUID) {
        removeParent(childId)
        parentGraph.addEdge(parentId, childId)
    }

    fun removeParent(childId: UUID) {
        // Ввообще парент может быть только один, но хуле и не итернуться целиком
        parentGraph.incomingEdgesOf(childId).forEach(parentGraph::removeEdge)
    }

    fun removeChild(parentId: UUID, childId: UUID): Boolean {
        return parentGraph.removeEdge(parentId, childId) != null
    }

    fun removeAllChildren(parentId: UUID) {
        parentGraph.outgoingEdgesOf(parentId).forEach(parentGraph::removeEdge)
    }

    data class Data(
        val tps: Int,
    )
}