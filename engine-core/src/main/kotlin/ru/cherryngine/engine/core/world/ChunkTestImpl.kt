package ru.cherryngine.engine.core.world

import net.minestom.server.instance.Section
import net.minestom.server.instance.block.Block

class ChunkTestImpl(
    sectionCount: Int,
) : Chunk {
    override val sections: Array<Section> = Array(sectionCount) { Section() }

    init {
        // Generate blocks
        for (i in 0 until sectionCount) {
            val section = sections[i]
            val blockPalette = section.blockPalette()
            if (i < 7) {
                blockPalette.fill(Block.STONE.stateId().toInt())
            }
        }
    }
}
