package ru.cherryngine.impl.demo.modules

import net.minestom.server.coordinate.ChunkRange
import ru.cherryngine.engine.core.minestomPos
import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.impl.demo.GameObject
import ru.cherryngine.impl.demo.Module
import ru.cherryngine.impl.demo.event.impl.ClientLoadedEvent

class BlockHolderModule(val blockHolder: BlockHolder, gameObject: GameObject? = null) : Module(gameObject) {

    val load: (ClientLoadedEvent) -> Unit = {
        show(it.clientModule)
    }

    override fun enable() {
        gameObject!!.scene.bus.subscribe(ClientLoadedEvent::class.java, load)
    }

    override fun destroy() {
        gameObject!!.scene.bus.unsubscribe(ClientLoadedEvent::class.java, load)
    }

    fun show(client: ClientModule) {
        client.gameObject!!.transform.position.minestomPos().let { pos ->
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