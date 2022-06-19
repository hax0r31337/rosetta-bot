package me.liuli.rosetta.bot.event

import java.lang.reflect.Method

open class Event {

    companion object {

        private val emitMethod: Method

        init {
            val clazz = Listener::class.java
            val method = clazz.declaredMethods.find { it.name == "on" }!!
            method.isAccessible = true
            emitMethod = method
        }

        fun emit(listener: Listener<*>, event: Event) {
            try {
                emitMethod.invoke(listener, event)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}

open class EventCancellable : Event() {
    var isCancelled = false

    fun cancel() {
        isCancelled = true
    }
}