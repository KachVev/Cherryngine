package ru.cherryngine.engine.scenes

import io.micronaut.context.ApplicationContext
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import net.minestom.server.registry.Registries
import net.minestom.server.world.DimensionType
import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.engine.core.world.PolarChunkSupplier
import ru.cherryngine.engine.scenes.modules.BlockHolderModule
import java.util.*

@Singleton
class SceneManager(
    private val applicationContext: ApplicationContext,
    private val registries: Registries,
) {
    val scenes: MutableMap<UUID, Scene> = HashMap()
    lateinit var masterScene: Scene

    @PostConstruct
    fun init() {
        masterScene = createScene(data = Scene.Data(20))

        val blockHolder = BlockHolder(
            registries.dimensionType().get(DimensionType.OVERWORLD)!!,
            PolarChunkSupplier(javaClass.getResource("/world.polar")!!.readBytes(), registries)
        )

        masterScene.createGameObject().getOrCreateModule(BlockHolderModule::class, blockHolder)
    }

    @PreDestroy
    fun destroy() {
        scenes.values.forEach(Scene::stop)
        scenes.clear()
    }

    fun removeScene(id: UUID) {
        scenes.remove(id)?.stop()
    }

    fun createScene(data: Scene.Data): Scene {
        return Scene(applicationContext, data).also {
            scenes[it.id] = it
            it.start()
        }
    }

}