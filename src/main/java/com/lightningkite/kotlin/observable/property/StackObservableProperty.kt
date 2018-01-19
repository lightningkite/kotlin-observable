package com.lightningkite.kotlin.observable.property

import com.lightningkite.kotlin.lambda.invokeAll

/**
 * An observable property where you can push and pop states.
 * Notifies listeners of the current state.
 * Created by joseph on 1/19/18.
 */
class StackObservableProperty<T> : MutableObservableProperty<T> {
    private val internalStack = ArrayList<T>()
    private val listeners = ArrayList<(T) -> Unit>()
    val stack: List<T> get() = internalStack

    override var value: T
        get() = internalStack.last()
        set(value) {
            if (internalStack.isEmpty()) internalStack.add(value)
            else internalStack[internalStack.lastIndex] = value
            listeners.invokeAll(value)
        }

    override fun add(element: (T) -> Unit): Boolean = listeners.add(element)
    override fun remove(element: (T) -> Unit): Boolean = listeners.remove(element)

    /**
     * Pushes a new state onto the stack and notifies the listeners.
     */
    fun push(element: T) {
        internalStack.add(element)
        listeners.invokeAll(element)
    }

    /**
     * Swaps the top state in the stack for another and notifies the listeners.
     */
    fun swap(element: T) {
        internalStack[internalStack.lastIndex] = element
        listeners.invokeAll(element)
    }

    /**
     * Pops a state off the stack and notifies the listeners.
     */
    fun pop() {
        internalStack.removeAt(internalStack.lastIndex)
        listeners.invokeAll(internalStack.last())
    }

    /**
     * Pops all of the states off the stack except for the bottom one and notifies the listeners.
     */
    fun root() {
        val element = internalStack.first()
        internalStack.clear()
        internalStack.add(element)
        listeners.invokeAll(element)
    }

    /**
     * Pops states off the stack until a predicate is satisfied and notifies the listeners.
     */
    fun back(predicate: (T) -> Boolean) {
        val index = internalStack.indexOfLast(predicate)
        while (index + 1 < internalStack.size) {
            internalStack.removeAt(index + 1)
        }
        listeners.invokeAll(internalStack.last())
    }

    /**
     * Clears the stack and starts over with a new element and notifies the listeners.
     */
    fun reset(element: T) {
        internalStack.clear()
        internalStack.add(element)
        listeners.invokeAll(element)
    }

    fun withSize() = this.transform { it to this.stack.size }
}