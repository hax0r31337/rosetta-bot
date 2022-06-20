package me.liuli.rosetta.bot.event

import java.lang.reflect.Method
import java.lang.reflect.Modifier

abstract class Listener<T : Event>(val eventType: Class<T>) {

    abstract fun on(event: T)
}

class FuncListener<T : Event>(eventType: Class<T>, private val func: (T) -> Unit) : Listener<T>(eventType) {

    override fun on(event: T) {
        func(event)
    }
}

class MethodListener<T : Event>(eventType: Class<T>,
                                private val instance: Any?, private val method: Method) : Listener<T>(eventType) {

    init {
        method.isAccessible = true
    }

    override fun on(event: T) {
        method.invoke(instance, event)
    }
}

abstract class ListenerSet {

    val listeners: Array<MethodListener<*>>
        get() {
            val result = mutableListOf<MethodListener<*>>()

            this.javaClass.declaredMethods.forEach { method ->
                if (method.isAnnotationPresent(Listen::class.java) && method.parameterCount == 1) {
                    val param = method.parameters[0].type
                    if (Event::class.java.isAssignableFrom(param)) {
                        result.add(MethodListener(param as Class<out Event>,
                            if(Modifier.isStatic(method.modifiers)) null else this, method))
                    }
                }
            }

            return result.toTypedArray()
        }

    annotation class Listen
}