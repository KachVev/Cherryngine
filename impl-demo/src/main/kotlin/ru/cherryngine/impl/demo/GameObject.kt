package ru.cherryngine.impl.demo

import ru.cherryngine.impl.demo.event.EventBus
import ru.cherryngine.impl.demo.modules.Transform
import java.util.UUID
import kotlin.collections.HashSet
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class GameObject(val scene: Scene, vararg modules: Module) {
    val id: UUID = UUID.randomUUID()

    val transform: Transform = Transform(this)

    val bus: EventBus
        get() = scene.bus

    private val modules = HashSet<Module>()

    init {
        addModule(transform)
        modules.forEach(::addModule)
    }

//    fun <T : Module> addModule(clazz: KClass<T>, vararg args: Any) : Module? {
//        var module = getModule(clazz)
//        if (module == null) {
//            module = clazz.primaryConstructor?.call(args)
//        }
//        return module
//    }
//
//    fun <T : Module> addModule(clazz: Class<T>, vararg args: Any) : Module? {
//        var module = getModule(clazz)
//        if (module == null) {
//            val types = ArrayList<Class<*>>()
//            args.forEach { types.add(it.javaClass) }
//            val constructor = clazz.getDeclaredConstructor(*types.toTypedArray())
//            module = constructor.newInstance(args)
//        }
//        return module
//    }

    fun addModule(module: Module) {
        this.modules.add(module)
        module.gameObject = this
        module.enable()
    }

    fun <T : Module> getModule(clazz: Class<T>) : Module? {
        return modules.filterIsInstance(clazz).firstOrNull()
    }

    fun <T : Module> getModule(clazz: KClass<T>) : Module? {
        return getModule(clazz.java)
    }

    fun destroy() {
        modules.forEach(Module::destroy)
    }
}
