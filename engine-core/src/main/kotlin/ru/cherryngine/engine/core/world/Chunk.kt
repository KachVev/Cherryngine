package ru.cherryngine.engine.core.world

import net.minestom.server.instance.Section

interface Chunk {
    companion object {
        const val CHUNK_SIZE_X: Int = 16
        const val CHUNK_SIZE_Z: Int = 16
        const val CHUNK_SECTION_SIZE: Int = 16
    }

    val sections: Array<Section>

    class Impl(
        override val sections: Array<Section>
    ) : Chunk
}
