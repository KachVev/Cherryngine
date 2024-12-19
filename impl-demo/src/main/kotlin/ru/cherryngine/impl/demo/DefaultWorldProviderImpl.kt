package ru.cherryngine.impl.demo

import jakarta.inject.Singleton
import net.minestom.server.coordinate.Pos
import net.minestom.server.registry.Registries
import net.minestom.server.world.DimensionType
import ru.cherryngine.engine.core.DefaultWorldProvider
import ru.cherryngine.engine.core.world.BlockHolder
import ru.cherryngine.engine.core.world.PolarChunkSupplier

@Singleton
class DefaultWorldProviderImpl(
    registries: Registries,
) : DefaultWorldProvider {
    override val spawnPos = Pos(169.5, 73.5, 137.5)
    override val blockHolder: BlockHolder = BlockHolder(
        registries.dimensionType().get(DimensionType.OVERWORLD)!!,
        PolarChunkSupplier(javaClass.getResource("/world.polar")!!.readBytes(), registries)
    )
}