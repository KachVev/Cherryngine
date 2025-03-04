package ru.cherryngine.engine.core.world

fun interface ChunkSupplier {
    fun create(blockHolder: BlockHolder, chunkX: Int, chunkZ: Int): Chunk?
}
