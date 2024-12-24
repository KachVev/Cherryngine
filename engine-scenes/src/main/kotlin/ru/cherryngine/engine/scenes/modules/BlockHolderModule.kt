package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Prototype
import net.minestom.server.coordinate.ChunkRange
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.event.impl.ClientLoadedEvent

@Prototype
class BlockHolderModule(
    @Parameter override val gameObject: GameObject,
    @Parameter val blockHolder: BlockHolder,
) : Module {

    val load: (ClientLoadedEvent) -> Unit = {
        show(it.clientModule)
    }

    override fun enable() {
        gameObject.scene.bus.subscribe(ClientLoadedEvent::class.java, load)
    }

    override fun destroy() {
        gameObject.scene.bus.unsubscribe(ClientLoadedEvent::class.java, load)
    }

    fun show(client: ClientModule) {
        client.gameObject.transform.translation.minestomPos().let { pos ->
            ChunkRange.chunksInRange(
                pos.chunkX(),
                pos.chunkZ(),
                client.viewDistance
            ) { x: Int, z: Int ->
                blockHolder.generatePacket(x, z)?.let { client.connection.sendPackets(it) }
            }
        }
    }

    fun hide(client: ClientModule) {
        TODO("ТУТ КРЧ СКРЫВАЕМ БЛОКС")
    }
}