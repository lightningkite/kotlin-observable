package com.ivieleague.kotlin.observable

import com.ivieleague.kotlin.collection.addSorted
import com.ivieleague.kotlin.collection.map
import com.ivieleague.kotlin.runAll
import java.util.*

/**
 * Created by jivie on 5/23/16.
 */
class ObservableListSorted<E>(sourceInit: ObservableList<E>, sorter: (E, E) -> Boolean) : ObservableList<E>, com.ivieleague.kotlin.Disposable {

    val indexList = ArrayList<Int>()

    val indexCompare = { a: Int, b: Int -> sorter(source!![a], source!![b]) }
    var listenerSet: KObservableListListenerSet<E>? = null
    var source: ObservableList<E> = sourceInit
        set(value) {
            source.removeListenerSet(listenerSet!!)
            field = value
            newSetup()
        }

    init {
        newSetup()
    }

    private fun newSetup() {
        indexList.clear()
        source.forEachIndexed { index, item ->
            indexList.addSorted(index, indexCompare)
        }
        listenerSet = KObservableListListenerSet(
                onAddListener = { item, index ->
                    for (i in indexList.indices) {
                        if (indexList[i] >= index)
                            indexList[i]++
                    }
                    val sortedIndex = indexList.addSorted(index, indexCompare)
                    onAdd.runAll(item, sortedIndex)
                },
                onRemoveListener = { item, index ->
                    val sortedIndex = indexList.indexOf(index)
                    indexList.removeAt(sortedIndex)
                    for (i in indexList.indices) {
                        if (indexList[i] >= index)
                            indexList[i]--
                    }
                    onRemove.runAll(item, sortedIndex)
                },
                onChangeListener = { item, index ->
                    val removeSortedIndex = indexList.indexOf(index)
                    indexList.removeAt(removeSortedIndex)
                    onRemove.runAll(item, removeSortedIndex)

                    val addSortedIndex = indexList.addSorted(index, indexCompare)
                    onChange.runAll(item, addSortedIndex)
                },
                onReplaceListener = {
                    indexList.clear()
                    it.forEachIndexed { index, item ->
                        indexList.addSorted(index, indexCompare)
                    }
                    onReplace.runAll(this)
                }
        )
        source.addListenerSet(listenerSet!!)
    }

    override fun dispose() {
        source.removeListenerSet(listenerSet!!)
        source = ObservableListWrapper()
        listenerSet = null
    }

    override val size: Int get() = source.size

    override fun contains(element: E): Boolean = source.contains(element)
    override fun containsAll(elements: Collection<E>): Boolean = source.containsAll(elements)
    override fun get(index: Int): E = source.get(indexList[index]) ?: throw IllegalStateException("No source specified.")
    override fun indexOf(element: E): Int = indexList.indexOf(source.indexOf(element))
    override fun lastIndexOf(element: E): Int = indexList.indexOf(source.lastIndexOf(element))
    override fun isEmpty(): Boolean = source.isEmpty()
    override fun add(element: E): Boolean = source.add(element)
    override fun add(index: Int, element: E) = source.add(index, element)
    override fun addAll(index: Int, elements: Collection<E>): Boolean = source.addAll(index, elements)
    override fun addAll(elements: Collection<E>): Boolean = source.addAll(elements)
    override fun clear() = source.clear()
    override fun remove(element: E): Boolean = source.remove(element)
    override fun removeAll(elements: Collection<E>): Boolean = source.removeAll(elements)
    override fun removeAt(index: Int): E = source.removeAt(indexList[index])
    override fun retainAll(elements: Collection<E>): Boolean = source.retainAll(elements)
    override fun set(index: Int, element: E): E = source.set(index, element)
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = indexList.subList(fromIndex, toIndex).map { source[it] }.toMutableList()

    override fun listIterator(): MutableListIterator<E> = throw UnsupportedOperationException()
    override fun listIterator(index: Int): MutableListIterator<E> = throw UnsupportedOperationException()
    override fun iterator(): MutableIterator<E> = indexList.iterator().map({ source[it] }, { indexOf(it) })
    override fun replace(list: List<E>) = source.replace(list)

    val listenerMapper = { input: (E, Int) -> Unit ->
        { element: E, index: Int ->
            input(element, indexList.indexOf(index))
        }
    }

    override val onAdd = HashSet<(E, Int) -> Unit>()
    override val onChange = HashSet<(E, Int) -> Unit>()
    override val onUpdate = ObservablePropertyReference<ObservableList<E>>({ this }, { replace(it) })
    override val onReplace = HashSet<(ObservableList<E>) -> Unit>()
    override val onRemove = HashSet<(E, Int) -> Unit>()

//    override val onAdd: MutableSet<(E, Int) -> Unit> get() = source.onAdd.map(listenerMapper)
//    override val onRemove: MutableSet<(E, Int) -> Unit> get() = source.onRemove.map(listenerMapper)
//    override val onChange: MutableSet<(E, Int) -> Unit> get() = source.onChange.map(listenerMapper)
//
//    override val onUpdate = sourceInit.onUpdate.mapObservable<KObservableListInterface<E>, KObservableListInterface<E>>({ it -> this }, { throw IllegalAccessException() })
//    override val onReplace: MutableSet<(KObservableListInterface<E>) -> Unit> get() = source.onReplace.map({ input -> { input(this) } })

}

inline fun <E> ObservableList<E>.sorted(noinline sorter: (E, E) -> Boolean): ObservableListSorted<E>
        = ObservableListSorted(this, sorter)

fun main(args: Array<String>) {
    //test
    val unsorted = ObservableListWrapper(mutableListOf(4, 2, 7, 1, 3))
    val sorted = unsorted.sorted { a, b -> a < b }
    println(sorted.joinToString())
    sorted.removeAt(2)
    println(sorted.joinToString())
    sorted.add(6)
    println(sorted.joinToString())
    println(unsorted.joinToString())

}