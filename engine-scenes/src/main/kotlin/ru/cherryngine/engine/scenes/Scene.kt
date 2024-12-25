package ru.cherryngine.engine.scenes

import io.micronaut.context.ApplicationContext
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.event.impl.SceneTickEvent
import java.util.*

class Scene(
    private val applicationContext: ApplicationContext,
    private val sceneManager: SceneManager,
    val data: Data
) {
    val id: UUID = UUID.randomUUID()

    val gameObjects = HashMap<UUID, GameObject>()

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

    fun createGameObject() : GameObject {
        return GameObject(applicationContext, this).also {
            gameObjects[it.id] = it
        }
    }

    fun destroyGameObject(id: UUID) {
        gameObjects.remove(id)?.destroy()
    }

    fun fireEvent(event: Event) {
        val modules = gameObjects.flatMap { it.value.modules }.groupBy { it::class }
        sceneManager.sortedModuleTypes.forEach { moduleType ->
            modules[moduleType]?.forEach { module ->
                module.onEvent(event)
            }
        }
    }

    data class Data(
        val tps: Int
    )
}