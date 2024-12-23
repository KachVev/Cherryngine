package ru.cherryngine.impl.demo

import io.micronaut.context.annotation.Prototype

@Prototype
class GameObject(vararg modules: Module) {

    val modules = HashSet<Module>()

    init {
        this.modules.addAll(modules);
    }
}
