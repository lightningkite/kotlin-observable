package com.ivieleague.kotlin.observable

import com.ivieleague.kotlin.collection.map

/**
 * Created by jivie on 5/6/16.
 */
class ObservableListMapped<S, E>(val source: ObservableList<S>, val mapper: (S) -> E, val reverseMapper: (E) -> S) : ObservableList<E> {
    override val size: Int get() = source.size

    override fun contains(element: E): Boolean = source.contains(reverseMapper(element))
    override fun containsAll(elements: Collection<E>): Boolean = source.containsAll(elements.map(reverseMapper))
    override fun get(index: Int): E = mapper(source.get(index))
    override fun indexOf(element: E): Int = source.indexOf(reverseMapper(element))
    override fun isEmpty(): Boolean = source.isEmpty()
    override fun lastIndexOf(element: E): Int = source.lastIndexOf(reverseMapper(element))
    override fun add(element: E): Boolean = source.add(reverseMapper(element))
    override fun add(index: Int, element: E) = source.add(index, reverseMapper(element))
    override fun addAll(index: Int, elements: Collection<E>): Boolean = source.addAll(index, elements.map(reverseMapper))
    override fun addAll(elements: Collection<E>): Boolean = source.addAll(elements.map(reverseMapper))
    override fun clear() = source.clear()
    override fun remove(element: E): Boolean = source.remove(reverseMapper(element))
    override fun removeAll(elements: Collection<E>): Boolean = source.removeAll(elements.map(reverseMapper))
    override fun removeAt(index: Int): E = mapper(source.removeAt(index))
    override fun retainAll(elements: Collection<E>): Boolean = source.retainAll(elements.map(reverseMapper))
    override fun set(index: Int, element: E): E = mapper(source.set(index, reverseMapper(element)))
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = source.subList(fromIndex, toIndex).map(mapper).toMutableList()

    override fun listIterator(): MutableListIterator<E> = source.listIterator().map(mapper, reverseMapper)
    override fun listIterator(index: Int): MutableListIterator<E> = source.listIterator(index).map(mapper, reverseMapper)
    override fun iterator(): MutableIterator<E> = source.iterator().map(mapper, reverseMapper)
    override fun replace(list: List<E>) = source.replace(list.map(reverseMapper))

    val listenerMapper = { input: (E, Int) -> Unit ->
        { element:S, index:Int ->
            input(mapper(element), index)
        }
    }
    override val onAdd: MutableSet<(E, Int) -> Unit> get() = source.onAdd.map(listenerMapper)
    override val onRemove: MutableSet<(E, Int) -> Unit> get() = source.onRemove.map(listenerMapper)
    override val onChange: MutableSet<(E, Int) -> Unit> get() = source.onChange.map(listenerMapper)

    override val onUpdate = source.onUpdate.mapObservable<ObservableList<S>, ObservableList<E>>({ it -> this@ObservableListMapped }, { throw IllegalAccessException() })
    override val onReplace: MutableSet<(ObservableList<E>) -> Unit> get() = source.onReplace.map({ input -> { input(this) }})
}

fun <S, E> ObservableList<S>.mapObservableList(mapper: (S) -> E, reverseMapper: (E) -> S): ObservableListMapped<S, E> = ObservableListMapped(this, mapper, reverseMapper)
fun <S, E> ObservableList<S>.mapObservableList(mapper: (S) -> E): ObservableListMapped<S, E> = ObservableListMapped(this, mapper, {throw IllegalArgumentException()})