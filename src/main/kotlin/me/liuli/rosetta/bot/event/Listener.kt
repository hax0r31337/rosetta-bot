package me.liuli.rosetta.bot.event

abstract class Listener<T : Event>(val eventType: Class<T>) {

    abstract fun on(event: T)
}

class FuncListener<T : Event>(eventType: Class<T>, val func: (T) -> Unit) : Listener<T>(eventType) {

    override fun on(event: T) {
        func(event)
    }
}