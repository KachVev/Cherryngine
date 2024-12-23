package ru.cherryngine.impl.demo.event

class EventBus {

    companion object {
        val GLOBAL = EventBus()
    }

    private val subscriptions = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()

    fun <T> subscribe(eventClass: Class<T>, subscriber: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        subscriptions.getOrPut(eventClass) { mutableListOf() }.add(subscriber as (Any) -> Unit)
    }

    fun <T> unsubscribe(eventClass: Class<T>, subscriber: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        subscriptions[eventClass]?.remove(subscriber as (Any) -> Unit)
    }

    fun post(event: Any) {
        val eventClass = event.javaClass
        subscriptions[eventClass]?.forEach { it.invoke(event) }
    }
}