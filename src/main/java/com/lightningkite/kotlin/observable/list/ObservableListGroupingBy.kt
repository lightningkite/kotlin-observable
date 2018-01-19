package com.lightningkite.kotlin.observable.list

import com.lightningkite.kotlin.lambda.invokeAll
import com.lightningkite.kotlin.lifecycle.LifecycleConnectable
import com.lightningkite.kotlin.lifecycle.LifecycleListener
import java.io.Closeable
import java.util.*

/**
 * Gives you a grouped-by view of an observable list.
 * Created by josep on 9/7/2015.
 */
class ObservableListGroupingBy<E, G, L>(
        val source: ObservableList<E>,
        val grouper: (E) -> G,
        val listWrapper: (ObservableList<E>) -> L,
        val innerList: ObservableListWrapper<Pair<G, L>> = observableListOf()
) : ObservableList<Pair<G, L>> by innerList, Closeable {

    private inner class InnerList() : ObservableListIndicies<E>(source)

    private val groups = HashMap<G, InnerList>()

    private fun getOrMakeGroup(group: G): InnerList = getOrMakeGroup(group, {})

    private inline fun getOrMakeGroup(group: G, crossinline modify: (InnerList) -> Unit): InnerList {
        val current = groups[group]
        if (current != null) return current.apply(modify)

        val list = InnerList().apply(modify)
        val wrapper = listWrapper(list)
        innerList.add(group to wrapper)
        groups[group] = list
        return list
    }

    private fun removeGroup(group: G) {
        groups.remove(group)
        val index = innerList.indexOfFirst { it.first == group }
        if (index != -1) {
            innerList.removeAt(index)
        }
    }

    private fun reset() {
        innerList.clear()
        groups.clear()

        for (index in source.indices) {
            val item = source[index]
            val group = grouper(item)
            getOrMakeGroup(group) {
                it.indexList.add(index)
                it.onAdd.invokeAll(item, it.indexList.size - 1)
                it.onUpdate.update()
            }
        }
    }

    fun modifyIndicesBy(after: Int, by: Int) {
        for ((group, inner) in groups) {
            for (indexIndex in inner.indexList.indices) {
                val index = inner.indexList[indexIndex]
                if (index >= after)
                    inner.indexList[indexIndex] = index + by
            }
        }
    }

    fun getCurrentIndexGroup(index: Int): G {
        for ((group, list) in groups) {
            if (list.indexList.contains(index)) {
                return group
            }
        }
        throw IllegalStateException()
    }

    val listener = ObservableListListenerSet<E>(
            onAddListener = { item, index ->
                modifyIndicesBy(index, 1)
                val group = grouper(item)
                getOrMakeGroup(group) {
                    it.indexList.add(index)
                    it.onAdd.invokeAll(item, it.indexList.size - 1)
                    it.onUpdate.update()
                }
            },
            onRemoveListener = { item, index ->
                val group = getCurrentIndexGroup(index)
                val groupList = groups[group]
                if (groupList != null) {
                    val indexIndex = groupList.indexList.indexOf(index)
                    groupList.indexList.removeAt(indexIndex)
                    modifyIndicesBy(index, -1)
                    groupList.onRemove.invokeAll(item, indexIndex)
                    groupList.onUpdate.update()
                    if (groupList.isEmpty()) {
                        removeGroup(group)
                    }
                } else throw IllegalArgumentException()
            },
            onChangeListener = { oldItem, item, index ->
                val oldGroup = getCurrentIndexGroup(index)
                val newGroup = grouper(item)
                if (oldGroup == newGroup) {
                    val groupList = groups[oldGroup]
                    if (groupList != null) {
                        val indexIndex = groupList.indexList.indexOf(index)
                        groupList.onChange.invokeAll(oldItem, item, indexIndex)
                        groupList.onUpdate.update()
                    } else throw IllegalArgumentException()
                } else {
                    val oldGroupList = groups[oldGroup] ?: throw IllegalArgumentException()
                    val oldIndexIndex = oldGroupList.indexList.indexOf(index)
                    oldGroupList.indexList.removeAt(oldIndexIndex)
                    oldGroupList.onRemove.invokeAll(oldItem, oldIndexIndex)
                    oldGroupList.onUpdate.update()
                    if (oldGroupList.isEmpty()) {
                        removeGroup(oldGroup)
                    }

                    getOrMakeGroup(newGroup) {
                        it.indexList.add(index)
                        it.onAdd.invokeAll(item, it.indexList.size - 1)
                        it.onUpdate.update()
                    }
                }
            },
            onMoveListener = { item, oldIndex, index ->
                val group = getCurrentIndexGroup(oldIndex)
                val groupList = groups[group]
                if (groupList != null) {
                    val indexIndex = groupList.indexList.indexOf(oldIndex)
                    modifyIndicesBy(oldIndex, -1)
                    modifyIndicesBy(index, 1)
                    groupList.indexList[indexIndex] = index
                } else throw IllegalArgumentException()
            },
            onReplaceListener = { list ->
                reset()
            }
    )
//
//    private fun updateLists() {
//        val newCategories = source.map(grouper).toSet()
//        val oldCategories = innerList.map { it.first }.toSet()
//        for (category in newCategories) {
//            if (category !in oldCategories) {
//                val filtering = InnerList()
//                val wrapped = listWrapper(filtering)
//                innerLists[wrapped] = filtering
//                innerList.add(category to wrapped)
//            }
//        }
//        for (category in oldCategories) {
//            if (category !in newCategories) {
//                innerList.removeAll() { it.first == category }
//            }
//        }
//    }

    init {
        setup()
    }

    fun setup() {
        reset()
        source.addListenerSet(listener)
    }

    override fun close() {
        source.removeListenerSet(listener)
        innerList.clear()
    }
}

fun <E, G, L> ObservableList<E>.groupingBy(
        grouper: (E) -> G,
        listWrapper: (ObservableList<E>) -> L
) = ObservableListGroupingBy(this, grouper, listWrapper)

fun <E, G, L> ObservableList<E>.groupingBy(
        lifecycle: LifecycleConnectable,
        grouper: (E) -> G,
        listWrapper: (ObservableList<E>) -> L
): ObservableListGroupingBy<E, G, L> {
    val list = ObservableListGroupingBy(this, grouper, listWrapper)
    lifecycle.connect(object : LifecycleListener {
        override fun onStart() {
            list.setup()
        }

        override fun onStop() {
            list.close()
        }
    })
    return list
}

fun <E, G> ObservableList<E>.groupingBy(grouper: (E) -> G) = groupingBy(grouper, { it })

fun <E, G> ObservableList<E>.groupingBy(lifecycle: LifecycleConnectable, grouper: (E) -> G) = groupingBy(lifecycle, grouper, { it })