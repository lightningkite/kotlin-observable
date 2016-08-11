package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.Disposable
import com.lightningkite.kotlin.collection.mapped
import com.lightningkite.kotlin.observable.property.ObservableProperty
import com.lightningkite.kotlin.runAll
import java.util.*

/**
 * Created by jivie on 5/23/16.
 */
class ObservableListSorted<E>(sourceInit: ObservableList<E>, val getInsertionIndex: (List<E>, E) -> Int) : ObservableList<E>, Disposable {

    val indexList = ArrayList<Int>()
    val indexListMapped = indexList.mapped<Int, E>(
            { it: Int -> source[it] },
            { it: E -> indexList.indexOf(source.indexOf(it)) }
    )

    var source: ObservableList<E> = sourceInit
        set(value) {
            source.removeListenerSet(listenerSet!!)
            field = value
            newSetup()
        }

    fun indexGetInsertionIndex(item: E) = getInsertionIndex(indexListMapped, item)
    var listenerSet: ObservableListListenerSet<E>? = null

    init {
        newSetup()
    }

    private fun newSetup() {
        indexList.clear()
        source.forEachIndexed { index, item ->
            val sortedIndex = indexGetInsertionIndex(item)
            indexList.add(sortedIndex, index)
        }
        listenerSet = ObservableListListenerSet(
                onAddListener = { item, index ->
                    for (i in indexList.indices) {
                        if (indexList[i] >= index)
                            indexList[i]++
                    }
                    val sortedIndex = indexGetInsertionIndex(item)
                    indexList.add(sortedIndex, index)
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
                    val sortedIndex = indexGetInsertionIndex(item)

                    if (removeSortedIndex != sortedIndex) {
                        onRemove.runAll(item, removeSortedIndex)
                        indexList.add(sortedIndex, index)
                        onAdd.runAll(item, sortedIndex)
                    } else {
                        indexList.add(sortedIndex, index)
                        onChange.runAll(item, removeSortedIndex)
                    }
                },
                onReplaceListener = {
                    indexList.clear()
                    it.forEachIndexed { index, item ->
                        val sortedIndex = indexGetInsertionIndex(item)
                        indexList.add(sortedIndex, index)
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

    override val size: Int get() = indexList.size

    override fun contains(element: E): Boolean = source.contains(element)
    override fun containsAll(elements: Collection<E>): Boolean = source.containsAll(elements)
    override fun get(index: Int): E = source[indexList[index]] ?: throw IllegalStateException("No source specified.")
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
    override fun iterator(): MutableIterator<E> = indexList.iterator().mapped({ source[it] }, { indexOf(it) })
    override fun replace(list: List<E>) = source.replace(list)

    val listenerMapper = { input: (E, Int) -> Unit ->
        { element: E, index: Int ->
            input(element, indexList.indexOf(index))
        }
    }

    override val onAdd = HashSet<(E, Int) -> Unit>()
    override val onChange = HashSet<(E, Int) -> Unit>()
    override val onUpdate: ObservableProperty<ObservableList<E>> get() = source.onUpdate
    override val onReplace = HashSet<(ObservableList<E>) -> Unit>()
    override val onRemove = HashSet<(E, Int) -> Unit>()

//    override val onAdd: MutableSet<(E, Int) -> Unit> get() = source.onAdd.map(listenerMapper)
//    override val onRemove: MutableSet<(E, Int) -> Unit> get() = source.onRemove.map(listenerMapper)
//    override val onChange: MutableSet<(E, Int) -> Unit> get() = source.onChange.map(listenerMapper)
//
//    override val onUpdate = sourceInit.onUpdate.mapObservable<KObservableListInterface<E>, KObservableListInterface<E>>({ it -> this }, { throw IllegalAccessException() })
//    override val onReplace: MutableSet<(KObservableListInterface<E>) -> Unit> get() = source.onReplace.map({ input -> { input(this) } })

}

@Deprecated("This has been renamed to 'sorting' because it sorts on the fly.", ReplaceWith("sorting(sorter)", "com.lightningkite.kotlin.observable.list.sorting"))
inline fun <E> ObservableList<E>.sorted(noinline sorter: (E, E) -> Boolean): ObservableListSorted<E> = sorting(sorter)

inline fun <E> ObservableList<E>.sorting(noinline sorter: (E, E) -> Boolean): ObservableListSorted<E>
        = ObservableListSorted(this, { list, item ->
    if (list.isEmpty())
        0
    else {
        val res = list.indexOfFirst { sorter(item, it) }
        if (res == -1) list.size else res
    }
})

@Deprecated("This has been renamed to 'sortingWithInsertionIndex' because it sorts on the fly.", ReplaceWith("sortingWithInsertionIndex(getInsertionIndex)", "com.lightningkite.kotlin.observable.list.sortingWithInsertionIndex"))
inline fun <E> ObservableList<E>.sortedWithInsertionIndex(noinline getInsertionIndex: (List<E>, E) -> Int): ObservableListSorted<E>
        = ObservableListSorted(this, getInsertionIndex)

inline fun <E> ObservableList<E>.sortingWithInsertionIndex(noinline getInsertionIndex: (List<E>, E) -> Int): ObservableListSorted<E>
        = ObservableListSorted(this, getInsertionIndex)

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