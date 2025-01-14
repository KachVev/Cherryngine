package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import net.minestom.server.coordinate.ChunkRange
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype
import ru.cherryngine.engine.scenes.event.Event
import ru.cherryngine.engine.scenes.modules.client.ClientModule
import ru.cherryngine.engine.scenes.view.Viewable
import ru.cherryngine.engine.scenes.view.Viewer

@ModulePrototype
class BlockHolderModule(
    @Parameter override val gameObject: GameObject,
    @Parameter val blockHolder: BlockHolder
) : Module, Viewable {

    override fun showFor(viewer: Viewer) {
        when (viewer) {
            is ClientModule -> {
                viewer.gameObject.transform.global.translation.minestomPos().let { pos ->
                    ChunkRange.chunksInRange(
                        pos.chunkX(),
                        pos.chunkZ(),
                        viewer.viewDistance
                    ) { x: Int, z: Int ->
                        blockHolder.generatePacket(x, z)?.let { viewer.connection.sendPackets(it) }
                    }
                }
            }
            else -> viewer.show(this)
        }
    }

    override fun hideFor(viewer: Viewer) {
        viewer.hide(this)
    }
}