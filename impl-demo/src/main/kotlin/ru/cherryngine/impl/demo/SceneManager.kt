package ru.cherryngine.impl.demo

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
class SceneManager {
    val scenes: MutableMap<UUID, Scene> = HashMap()

    @PostConstruct
    fun init() {
        createScene(data = Scene.Data(20))
    }

    @PreDestroy
    fun destroy() {
        scenes.values.forEach(Scene::stop)
        scenes.clear()
    }

    fun removeScene(id: UUID) {
        scenes.remove(id)?.stop()
    }

    fun createScene(data: Scene.Data) : Scene {
        return Scene(data).also {
            scenes[it.id] = it
            it.start()
        }
    }

}