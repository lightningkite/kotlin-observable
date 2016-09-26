package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by joseph on 9/26/16.
 */
class ObservableListSortedTest {

    fun makeSourceList() = observableListOf('c', 'd', 'a', 'b', 'e')
    fun makeTestList() = makeSourceList().sorting { a, b -> a < b }

    fun <E : Comparable<E>> assertSorted(list: List<E>) {
        val presorted = list.sorted()
        for (index in list.indices) {
            assertEquals(presorted[index], list[index])
        }
        assertEquals(presorted.size, list.size)
    }

    @Test
    fun isSorted() {
        val list = makeTestList()
        assertSorted(list)
    }

    @Test
    fun set() {
        val changeIndex = 2
        val newItem = 'z'

        var callbacksOccurred = 0
        val list = makeTestList()
        val originalSize = list.size
        val oldItem = list[changeIndex]

        list.onRemove += { item, index ->
            assertEquals(changeIndex, index)
            assertEquals(oldItem, item)
            callbacksOccurred++
        }
        list.onAdd += { item, index ->
            assertEquals(list.lastIndex, index)
            assertEquals(newItem, item)
            callbacksOccurred++
        }
        list[changeIndex] = newItem

        assertEquals(originalSize, list.size)
        assert(callbacksOccurred == 2, { "callback occurred" })
    }

    @Test
    fun add() {
        val newItem = 'z'

        var callbackOccurred = false
        val list = makeTestList()
        val originalSize = list.size

        list.onAdd += { char, index ->
            assertEquals(newItem, char)
            assertEquals(originalSize, index)

            callbackOccurred = true
        }
        list.add(newItem)

        assertEquals(originalSize + 1, list.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun removeAt() {
        val removeIndex = 2

        var callbackOccurred = false
        val list = makeTestList()
        val originalSize = list.size
        val oldElement = list[removeIndex]

        list.onRemove += { char, index ->
            assertEquals(char, oldElement)
            assertEquals(removeIndex, index)

            callbackOccurred = true
        }
        list.removeAt(removeIndex)

        assertEquals(originalSize - 1, list.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun iterator() {
        val list = makeTestList()
        var index = 0
        for (item in list) {
            assertEquals(list[index], item)
            index++
        }
        assertEquals(list.size, index)
    }
}