//package com.lightningkite.kotlin.observable.list
//
//import com.lightningkite.kotlin.Disposable
//import com.lightningkite.kotlin.observable.property.ObservablePropertyReference
//import com.lightningkite.kotlin.runAll
//import java.util.*
//
///**
// * Allows you to observe the changes to a list.
// * Created by josep on 9/7/2015.
// */
//class ObservableListGroupingByContiguous<E, G, L>(
//        val source: ObservableList<E>,
//        val grouper: (E) -> G,
//        val listWrapper: (ObservableList<E>) -> L,
//        val innerList: ObservableListWrapper<Pair<G, L>> = observableListOf()
//) : ObservableList<Pair<G, L>> by innerList, Disposable {
//
//    private inner class InnerList(
//            var lowIndex: Int,
//            var highIndex: Int
//    ) : ObservableList<E> {
//        override val onAdd = HashSet<(E, Int) -> Unit>()
//        override val onChange = HashSet<(E, E, Int) -> Unit>()
//        override val onMove = HashSet<(E, Int, Int) -> Unit>()
//        override val onUpdate = ObservablePropertyReference<ObservableList<E>>({ this }, { throw IllegalAccessException() })
//        override val onReplace = HashSet<(ObservableList<E>) -> Unit>()
//        override val onRemove = HashSet<(E, Int) -> Unit>()
//
//        override fun add(element: E): Boolean {
//            source.add(highIndex + 1, element); return true
//        }
//
//        override fun add(index: Int, element: E) = source.add(lowIndex + index, element)
//        override fun addAll(index: Int, elements: Collection<E>): Boolean = source.addAll(lowIndex + index, elements)
//        override fun addAll(elements: Collection<E>): Boolean = source.addAll(highIndex + 1, elements)
//        override fun clear() {
//            source.removeAll(this.toList())
//        }
//
//        override fun remove(element: E): Boolean = source.remove(element)
//        override fun removeAll(elements: Collection<E>): Boolean = source.removeAll(elements)
//        override fun removeAt(index: Int): E = source.removeAt(lowIndex + index)
//        override fun retainAll(elements: Collection<E>): Boolean = source.retainAll(elements)
//        override fun set(index: Int, element: E): E = source.set(lowIndex + index, element)
//
//        override fun update(element: E): Boolean = source.update(element)
//        override fun move(fromIndex: Int, toIndex: Int) = source.move(lowIndex + fromIndex, lowIndex + toIndex)
//        override fun updateAt(index: Int) = source.updateAt(lowIndex + index)
//
//        override fun replace(list: List<E>) = throw UnsupportedOperationException()
//        override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = throw UnsupportedOperationException()
//        override fun listIterator(): MutableListIterator<E> = throw UnsupportedOperationException()
//        override fun listIterator(index: Int): MutableListIterator<E> = throw UnsupportedOperationException()
//
//        override val size: Int get() = highIndex - lowIndex + 1
//
//        override fun contains(element: E): Boolean = source.indexOf(element) in lowIndex..highIndex
//        override fun containsAll(elements: Collection<E>): Boolean = elements.all { contains(it) }
//        override fun get(index: Int): E = source[index + lowIndex]
//        override fun indexOf(element: E): Int = source.indexOf(element) - lowIndex
//        override fun lastIndexOf(element: E): Int = lastIndexOf(element) - lowIndex
//        override fun isEmpty(): Boolean = size == 0
//        override fun iterator(): MutableIterator<E> = object : MutableIterator<E> {
//            var index = lowIndex
//            override fun hasNext(): Boolean = index <= highIndex
//            override fun remove() {
//                source.removeAt(index)
//            }
//
//            override fun next(): E {
//                val result = source[index]
//                index++
//                return result
//            }
//
//        }
//
//    }
//
//    fun modifyIndicesBy(after: Int, by: Int) {
//        for ((group, inner) in pairs) {
//            if (inner.lowIndex >= after)
//                inner.lowIndex += by
//            if (inner.highIndex >= after)
//                inner.highIndex += by
//        }
//    }
//
//    private val pairs = ArrayList<Pair<G, InnerList>>()
//    private fun getPair(index: Int): Pair<G, InnerList> = pairs.first { index in it.second.lowIndex..it.second.highIndex }
//    private fun getPairIndex(index: Int): Int = pairs.indexOfFirst { index in it.second.lowIndex..it.second.highIndex }
//
//    private fun addPair(group: G, index: Int): Pair<G, InnerList> {
//        val newPair = group to InnerList(index, index)
//        pairs.add(newPair)
//        innerList.add(newPair.first to listWrapper(newPair.second))
//        return newPair
//    }
//
//    private fun removePair(pairIndex: Int) {
//        pairs.removeAt(pairIndex)
//        innerList.removeAt(pairIndex)
//    }
//
//    private fun reset() {
//        clear()
//        var pair: Pair<G, InnerList>? = null
//        for (itemIndex in source.indices) {
//            val item = source[itemIndex]
//            val oldPair = pair
//            val newGroup = grouper(item)
//            if (oldPair != null && oldPair.first == newGroup) {
//                oldPair.second.highIndex++
//            } else {
//                val newPair = addPair(newGroup, itemIndex)
//                pair = newPair
//            }
//        }
//    }
//
//
//    val listener = ObservableListListenerSet<E>(
//            onAddListener = { item, index ->
//                modifyIndicesBy(index, 1)
//                val group = grouper(item)
//                val leftPair = getPair(index)
//                val rightPair = if (index + 1 < source.size) getPair(index + 1) else null
//                if (group == leftPair.first) {
//                    //add to left
//                    leftPair.second.onAdd.runAll(item, index - leftPair.second.lowIndex)
//                } else if (rightPair != null && group == rightPair.first) {
//                    //add to right
//                    leftPair.second.highIndex--
//                    rightPair.second.highIndex--
//                    rightPair.second.onAdd.runAll(item, 0)
//                } else if (rightPair != null && leftPair == rightPair) {
//                    //split
//                    val newPair = addPair(group, index)
//                    val newRightPair = addPair(leftPair.first, index + 1)
//                    newRightPair.second.highIndex = leftPair.second.highIndex
//                    leftPair.second.highIndex = index - 1
//                } else {
//                    //insert new
//                    addPair(group, index)
//                }
//            },
//            onRemoveListener = { item, index ->
//                val pairIndex = getPairIndex(index)
//                val pair = pairs[pairIndex]
//                if (pair.second.size == 1) {
//                    if(pairIndex > 0 && pairIndex < pairs.size - 1 && pairs[pairIndex + 1].first == pairs[pairIndex - 1].first){
//                        //merge
//                        removePair(pairIndex)
//                        val last = pairs[pairIndex].second.highIndex
//                        removePair(pairIndex)
//                        pairs[pairIndex - 1].second.highIndex = last
//                    } else {
//                        removePair(pairIndex)
//                    }
//                }
//                modifyIndicesBy(index, -1)
//                pair.second.onRemove.runAll(item, index - pair.second.lowIndex)
//            },
//            onChangeListener = { oldItem, item, index ->
//                val currentPairIndex = getPairIndex(index)
//                val currentPair = pairs[currentPairIndex]
//                val leftPair = getPair(index - 1)
//                val rightPair = getPair(index + 1)
//                val newGroup = grouper(item)
//                if (currentPair.first == newGroup) {
//                    //stay in current group
//                    currentPair.second.onChange.runAll(oldItem, item, index - currentPair.second.lowIndex)
//                } else {
//                    if(leftPair == rightPair && currentPair == leftPair){
//                        //split
//                        val newPair = addPair(newGroup, index)
//                        val newRightPair = addPair(leftPair.first, index + 1)
//                        newRightPair.second.highIndex = leftPair.second.highIndex
//                        leftPair.second.highIndex = index - 1
//                    } else if(leftPair.first == newGroup){
//                        //move to left
//                    } else if (rightPair.first == newGroup){
//                        //move to right
//                    } else {
//                        //independent
//                        pairs[currentPairIndex] = newGroup to currentPair.second
//                    }
//                }
//            },
//            onMoveListener = { item, oldIndex, index ->
//                val group = getCurrentIndexGroup(oldIndex)
//                val groupList = groups[group]
//                if (groupList != null) {
//                    val indexIndex = groupList.indexList.indexOf(oldIndex)
//                    modifyIndicesBy(oldIndex, -1)
//                    modifyIndicesBy(index, 1)
//                    groupList.indexList[indexIndex] = index
//                } else throw IllegalArgumentException()
//            },
//            onReplaceListener = { list ->
//                reset()
//            }
//    )
////
////    private val groups = HashMap<G, InnerList>()
////
////    private fun getOrMakeGroup(group: G): InnerList {
////        val current = groups[group]
////        if (current != null) return current
////
////        val list = InnerList()
////        val wrapper = listWrapper(list)
////        innerList.add(group to wrapper)
////        groups[group] = list
////        return list
////    }
////
////    private fun removeGroup(group: G) {
////        groups.remove(group)
////        val index = innerList.indexOfFirst { it.first == group }
////        if (index != -1) {
////            innerList.removeAt(index)
////        }
////    }
////
////    private fun reset() {
////        innerList.clear()
////        groups.clear()
////
////        val myGroups = HashMap<G, InnerList>()
////        for (index in source.indices) {
////            val item = source[index]
////            val group = grouper(item)
////            val current = getOrMakeGroup(group)
////            current.indexList.add(index)
////            current.onAdd.runAll(item, current.indexList.size - 1)
////        }
////    }
////
////
////    fun getCurrentIndexGroup(index: Int): G {
////        for ((group, list) in groups) {
////            if (list.indexList.contains(index)) {
////                return group
////            }
////        }
////        throw IllegalStateException()
////    }
////
////    val listener = ObservableListListenerSet<E>(
////            onAddListener = { item, index ->
////                modifyIndicesBy(index, 1)
////                val group = grouper(item)
////                val list = getOrMakeGroup(group)
////                list.indexList.add(index)
////                list.onAdd.runAll(item, list.indexList.size - 1)
////            },
////            onRemoveListener = { item, index ->
////                val group = getCurrentIndexGroup(index)
////                val groupList = groups[group]
////                if (groupList != null) {
////                    val indexIndex = groupList.indexList.indexOf(index)
////                    groupList.indexList.removeAt(indexIndex)
////                    modifyIndicesBy(index, -1)
////                    groupList.onRemove.runAll(item, indexIndex)
////                    if (groupList.isEmpty()) {
////                        removeGroup(group)
////                    }
////                } else throw IllegalArgumentException()
////            },
////            onChangeListener = { oldItem, item, index ->
////                val oldGroup = getCurrentIndexGroup(index)
////                val newGroup = grouper(item)
////                if (oldGroup == newGroup) {
////                    val groupList = groups[oldGroup]
////                    if (groupList != null) {
////                        val indexIndex = groupList.indexList.indexOf(index)
////                        groupList.onChange.runAll(oldItem, item, indexIndex)
////                    } else throw IllegalArgumentException()
////                } else {
////                    val oldGroupList = groups[oldGroup] ?: throw IllegalArgumentException()
////                    val oldIndexIndex = oldGroupList.indexList.indexOf(index)
////                    oldGroupList.indexList.removeAt(oldIndexIndex)
////                    oldGroupList.onRemove.runAll(oldItem, oldIndexIndex)
////                    if (oldGroupList.isEmpty()) {
////                        removeGroup(oldGroup)
////                    }
////
////                    val newGroupList = getOrMakeGroup(newGroup)
////                    newGroupList.indexList.add(index)
////                    newGroupList.onAdd.runAll(item, newGroupList.indexList.size - 1)
////                }
////            },
////            onMoveListener = { item, oldIndex, index ->
////                val group = getCurrentIndexGroup(oldIndex)
////                val groupList = groups[group]
////                if (groupList != null) {
////                    val indexIndex = groupList.indexList.indexOf(oldIndex)
////                    modifyIndicesBy(oldIndex, -1)
////                    modifyIndicesBy(index, 1)
////                    groupList.indexList[indexIndex] = index
////                } else throw IllegalArgumentException()
////            },
////            onReplaceListener = { list ->
////                reset()
////            }
////    )
////
////    init {
////        setup()
////    }
////
////    fun setup() {
////        reset()
////        source.addListenerSet(listener)
////    }
////
////    override fun dispose() {
////        source.removeListenerSet(listener)
////        innerList.clear()
////    }
//}
//
////fun <E, G, L> ObservableList<E>.groupingBy(
////        grouper: (E) -> G,
////        listWrapper: (ObservableList<E>) -> L
////) = ObservableListGroupingBy(this, grouper, listWrapper)
////
////fun <E, G, L> ObservableList<E>.groupingBy(
////        lifecycle: LifecycleConnectable,
////        grouper: (E) -> G,
////        listWrapper: (ObservableList<E>) -> L
////): ObservableListGroupingBy<E, G, L> {
////    val list = ObservableListGroupingBy(this, grouper, listWrapper)
////    lifecycle.connect(object : LifecycleListener {
////        override fun onStart() {
////            list.setup()
////        }
////
////        override fun onStop() {
////            list.dispose()
////        }
////    })
////    return list
////}
////
////inline fun <E, G> ObservableList<E>.groupingBy(noinline grouper: (E) -> G) = groupingBy(grouper, { it })
////
////inline fun <E, G> ObservableList<E>.groupingBy(lifecycle: LifecycleConnectable, noinline grouper: (E) -> G) = groupingBy(lifecycle, grouper, { it })