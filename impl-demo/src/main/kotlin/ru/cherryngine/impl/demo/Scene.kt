package ru.cherryngine.impl.demo

import java.util.UUID

class Scene(val data: Data) {
    val id: UUID = UUID.randomUUID()
    val gameObjects = HashSet<GameObject>()
    var tickStartMils = 0L
    var tickEndMils = 0L
    var tickElapsedMils = 0L

    private var isAlive = false

    fun start() {
        isAlive = true
        Thread.startVirtualThread {
            while (isAlive) {

                tickStartMils = System.currentTimeMillis()

                tick()

                tickEndMils = System.currentTimeMillis()
                tickElapsedMils = tickEndMils - tickStartMils

                println("$id | elapsed time = $tickElapsedMils")

                Thread.sleep(1000L / data.tps)
            }
        }
    }

    fun stop() {
        isAlive = false
    }

    fun tick() {



    }

    data class Data(
        val tps: Int
    )
}