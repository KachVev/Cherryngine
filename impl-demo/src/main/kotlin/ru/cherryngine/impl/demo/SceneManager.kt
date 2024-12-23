package ru.cherryngine.impl.demo

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import net.minestom.server.registry.Registries
import net.minestom.server.world.DimensionType
import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.engine.core.world.PolarChunkSupplier
import ru.cherryngine.impl.demo.modules.BlockHolderModule
import java.util.UUID

@Singleton
class SceneManager (
    private val registries: Registries,
) {
    val scenes: MutableMap<UUID, Scene> = HashMap()
    var masterScene: Scene? = null

    @PostConstruct
    fun init() {
        masterScene = createScene(data = Scene.Data(20))

        val blockHolderModule = BlockHolderModule(BlockHolder(
            registries.dimensionType().get(DimensionType.OVERWORLD)!!,
            PolarChunkSupplier(javaClass.getResource("/world.polar")!!.readBytes(), registries)
        ))

        masterScene!!.createGameObject().addModule(blockHolderModule)
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