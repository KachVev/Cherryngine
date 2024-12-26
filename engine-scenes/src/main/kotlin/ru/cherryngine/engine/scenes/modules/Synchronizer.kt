package ru.cherryngine.engine.scenes.modules

import io.micronaut.context.annotation.Parameter
import ru.cherryngine.engine.scenes.GameObject
import ru.cherryngine.engine.scenes.Module
import ru.cherryngine.engine.scenes.ModulePrototype

@ModulePrototype
class Synchronizer(
    @Parameter override val gameObject: GameObject
) : Module {

    override fun enable() {

    }

}