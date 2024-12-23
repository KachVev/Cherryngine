package ru.cherryngine.impl.demo

import ru.cherryngine.impl.demo.event.EventBus
import ru.cherryngine.impl.demo.event.impl.SceneTickEvent
import java.util.HashMap
import java.util.UUID

class Scene(val data: Data) {
    val id: UUID = UUID.randomUUID()

    val gameObjects = HashMap<UUID, GameObject>()

    var tick = 0L

    var tickStartMils = 0L
    var tickEndMils = 0L
    var tickElapsedMils = 0L

    val bus = EventBus()

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
        bus.post(SceneTickEvent(this))
        tick++
    }

    fun createGameObject(vararg modules: Module) : GameObject {
        return GameObject(this, *modules).also {
            gameObjects[it.id] = it
        }
    }

    fun destroyGameObject(id: UUID) {
        gameObjects.remove(id)?.destroy()
    }

    data class Data(
        val tps: Int
    )
}