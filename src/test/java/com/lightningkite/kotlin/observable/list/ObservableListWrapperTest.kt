package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by joseph on 9/26/16.
 */
class ObservableListWrapperTest {

    fun makeTestList() = observableListOf('a', 'b', 'c', 'd', 'e')

    @Test
    fun set() {
        val changeIndex = 2
        val newItem = 'z'

        var callbackOccurred = false
        val list = makeTestList()
        val originalSize = list.size
        val oldItem = list[changeIndex]

        list.onChange += { oldChar, char, index ->
            assertEquals(oldItem, oldChar)
            assertEquals(newItem, char)
            assertEquals(changeIndex, index)

            callbackOccurred = true
        }
        list[changeIndex] = newItem

        assertEquals(originalSize, list.size)
        assert(callbackOccurred, { "callback occurred" })
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
    fun addAt() {
        val addIndex = 2
        val newItem = 'z'

        var callbackOccurred = false
        val list = makeTestList()
        val originalSize = list.size

        list.onAdd += { char, index ->
            assertEquals(newItem, char)
            assertEquals(addIndex, index)

            callbackOccurred = true
        }
        list.add(addIndex, 'z')

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
    fun move() {
        val sourceIndex = 2
        val destIndex = 3

        var callbackOccurred = false
        val list = makeTestList()
        val originalSize = list.size

        list.onMove += { char, oldIndex, index ->
            assertEquals(char, 'c')
            assertEquals(sourceIndex, oldIndex)
            assertEquals(destIndex, index)

            callbackOccurred = true
        }
        list.move(sourceIndex, destIndex)

        assertEquals(originalSize, list.size)
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