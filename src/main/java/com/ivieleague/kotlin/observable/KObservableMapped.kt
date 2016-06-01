package com.ivieleague.kotlin.observable

import java.util.*

/**
 * Created by jivie on 2/22/16.
 */
class KObservableMapped<S, T>(val actualObservable: ObservableProperty<S>, val mapper: (S) -> T, val reverseMapper: (T) -> S) : ObservableProperty<T> {

    val actionToWrapper = HashMap<(T) -> Unit, Wrapper>()

    inner class Wrapper(val func: (T) -> Unit) : (S) -> Unit {
        override fun invoke(p1: S) {
            func(mapper(p1))
        }
    }

    val notPartOfWrapper by lazy { IllegalArgumentException("Function not a wrapper in this KObservableMapped.") }

    override val size: Int get() = actualObservable.size
    override fun contains(element: (T) -> Unit): Boolean =
            actualObservable.contains(actionToWrapper[element] ?: throw notPartOfWrapper)

    override fun containsAll(elements: Collection<(T) -> Unit>): Boolean = actualObservable.containsAll(
            elements.map { actionToWrapper[it] ?: throw notPartOfWrapper }
    )

    override fun isEmpty(): Boolean = actualObservable.isEmpty()
    override fun clear() {
        actualObservable.clear()
    }

    override fun iterator(): MutableIterator<(T) -> Unit> = actionToWrapper.keys.iterator()
    override fun remove(element: (T) -> Unit): Boolean {
        val wrapper = actionToWrapper[element] ?: throw notPartOfWrapper
        actionToWrapper.remove(wrapper.func)
        return actualObservable.remove(wrapper)
    }

    override fun removeAll(elements: Collection<(T) -> Unit>): Boolean = actualObservable.removeAll(
            elements.map {
                val wrapper = actionToWrapper[it] ?: throw notPartOfWrapper
                actionToWrapper.remove(wrapper.func)
                wrapper
            }
    )

    override fun retainAll(elements: Collection<(T) -> Unit>): Boolean = throw UnsupportedOperationException()

    override fun add(element: (T) -> Unit): Boolean {
        element(mapper(actualObservable.value))
        val wrapper = Wrapper(element)
        actionToWrapper[element] = wrapper
        return actualObservable.add(wrapper)
    }

    override fun addAll(elements: Collection<(T) -> Unit>): Boolean {
        val value = mapper(actualObservable.value)
        elements.forEach { it(value) }

        return actualObservable.addAll(elements.map {
            val wrapper = Wrapper(it)
            actionToWrapper[it] = wrapper
            wrapper
        })
    }

    override var value: T
        get() = mapper(actualObservable.value)
        set(value) {
            if (actualObservable is MutableObservableProperty) {
                actualObservable.value = reverseMapper(value)
            } else {
                throw IllegalAccessException()
            }
        }
}

inline fun <S, T> MutableObservableProperty<S>.mapObservable(noinline mapper: (S) -> T, noinline reverseMapper: (T) -> S): KObservableMapped<S, T> {
    return KObservableMapped(this, mapper, reverseMapper)
}

inline fun <S, T> ObservableProperty<S>.mapReadOnly(noinline mapper: (S) -> T): KObservableMapped<S, T> {
    return KObservableMapped(this, mapper, { throw IllegalAccessException() })
}

inline fun <T> ObservableProperty<T?>.notNull(default: T): KObservableMapped<T?, T> {
    return KObservableMapped(this, { it ?: default }, { it })
}