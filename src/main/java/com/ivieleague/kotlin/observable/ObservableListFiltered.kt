package com.ivieleague.kotlin.observable

import com.ivieleague.kotlin.Disposable
import com.ivieleague.kotlin.runAll
import java.util.*

/**
 * Allows you to observe the changes to a list.
 * Created by josep on 9/7/2015.
 */
class ObservableListFiltered<E>(
        val full: ObservableList<E>
) : ObservableList<E>, Disposable {

    override fun replace(list: List<E>) {
        throw UnsupportedOperationException()
    }

    val filterObs = StandardObservableProperty<(E) -> Boolean>({ true })
    var filter by filterObs

    //binding
    val bindings = ArrayList<Pair<MutableCollection<*>, *>>()

    fun <T> bind(collection: MutableCollection<T>, element: T) {
        bindings.add(collection to element)
        collection.add(element)
    }

    override fun dispose() {
        for ((collection, element) in bindings) {
            collection.remove(element)
        }
    }

    //filtering
    var passing = (full.indices).toSortedSet()

    init {
        filterObs.add {
            if (full.none(filter)) {
                passing.clear()
                onReplace.runAll(this)
            } else {
                var otherIndex = 0
                for (i in full.indices) {
                    val passes = filter(full[i])
                    if (passes) {
                        if (passing.add(i)) {
                            onAdd.runAll(full[i], otherIndex)
                        }
                        otherIndex++
                    } else {
                        if (passing.remove(i)) {
                            onRemove.runAll(full[i], otherIndex)
                        }
                    }
                }
            }
            onUpdate.runAll(this)
        }
        bind(full.onAdd) { item, index ->
            val passes = filter(item)
            if (passes) {
                val iterator = passing.iterator()
                val toAdd = ArrayList<Int>()
                while (iterator.hasNext()) {
                    val i = iterator.next()
                    if (i > index) {
                        iterator.remove()
                        toAdd += i + 1
                    }
                }
                passing.addAll(toAdd)
                passing.add(index)
                onAdd.runAll(item, passing.indexOf(index))
                onUpdate.runAll(this)
            }
        }
        bind(full.onChange) { item, index ->
            val passes = filter(item)
            val indexOf = passing.indexOf(index)
            val passed = indexOf != -1
            if (passes != passed) {
                if (passes) {
                    passing.add(index)
                    onAdd.runAll(item, index)
                    onUpdate.runAll(this)
                } else {
                    passing.remove(index)
                    onRemove.runAll(item, index)
                    onUpdate.runAll(this)
                }
            } else {
                onChange.runAll(item, indexOf)
            }
        }
        bind(full.onRemove) { item, index ->
            val passes = filter(item)
            if (passes) {
                val oldIndexOf = passing.indexOf(index)
                passing.remove(index)
                val iterator = passing.iterator()
                val toAdd = ArrayList<Int>()
                while (iterator.hasNext()) {
                    val i = iterator.next()
                    if (i > index) {
                        iterator.remove()
                        toAdd += i - 1
                    }
                }
                passing.addAll(toAdd)
                onRemove.runAll(item, oldIndexOf)
                onUpdate.runAll(this)
            }
        }
        bind(full.onReplace) {
            passing.clear()
            for (i in full.indices) {
                val passes = filter(full[i])
                if (passes) passing.add(i)
            }
            onReplace.runAll(this)
            onUpdate.runAll(this)
        }
    }

    override val onAdd = HashSet<(E, Int) -> Unit>()
    override val onChange = HashSet<(E, Int) -> Unit>()
    override val onUpdate = ObservablePropertyReference<ObservableList<E>>({ this@ObservableListFiltered }, { throw IllegalAccessException() })
    override val onReplace = HashSet<(ObservableList<E>) -> Unit>()
    override val onRemove = HashSet<(E, Int) -> Unit>()

    override fun set(index: Int, element: E): E {
        full[passing.elementAt(index)] = element
        return element
    }

    override fun add(element: E): Boolean = full.add(element)
    override fun add(index: Int, element: E): Unit = full.add(passing.elementAt(index), element)
    override fun addAll(elements: Collection<E>): Boolean = full.addAll(elements)
    override fun addAll(index: Int, elements: Collection<E>): Boolean = full.addAll(passing.elementAt(index), elements)
    @Suppress("UNCHECKED_CAST")
    override fun remove(element: E): Boolean = full.remove(element)

    override fun removeAt(index: Int): E = full.removeAt(passing.elementAt(index))
    @Suppress("UNCHECKED_CAST")
    override fun removeAll(elements: Collection<E>): Boolean = throw IllegalAccessException()

    override fun retainAll(elements: Collection<E>): Boolean = throw IllegalAccessException()
    override fun clear(): Unit = full.clear()

    override fun isEmpty(): Boolean = passing.isEmpty()
    override fun contains(element: E): Boolean = passing.contains(full.indexOf(element))
    override fun containsAll(elements: Collection<E>): Boolean = passing.containsAll(elements.map { full.indexOf(it) })
    override fun listIterator(): MutableListIterator<E> = throw UnsupportedOperationException()
    override fun listIterator(index: Int): MutableListIterator<E> = throw UnsupportedOperationException()
    override fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
        var iterator = passing.iterator()
        var current = -1
        override fun hasNext(): Boolean = iterator.hasNext()
        override fun next(): E {
            current = iterator.next()
            return full[current]
        }

        override fun remove() {
            iterator.remove()
            full.removeAt(current)
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = throw UnsupportedOperationException()
    override fun get(index: Int): E = full[passing.elementAt(index)]
    override fun indexOf(element: E): Int = passing.indexOf(full.indexOf(element))
    override fun lastIndexOf(element: E): Int = passing.lastIndexOf(full.lastIndexOf(element))
    override val size: Int get() = passing.size
}