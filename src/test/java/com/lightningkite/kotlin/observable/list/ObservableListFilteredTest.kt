package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * Created by joseph on 9/26/16.
 */
class ObservableListFilteredTest {
    fun makeTestList() = observableListOf(0, 1, 2, 3, 4, 5, 6, 7)
    fun makeTestData(): Pair<ObservableList<Int>, ObservableList<Int>> {
        val list = makeTestList()
        val filtering = list.filtering { it % 2 == 0 }
        return list to filtering
    }

    @Test
    fun filteringWorks() {
        val (list, filtering) = makeTestData()
        assertEquals(4, filtering.size)
        assert(filtering.all { it % 2 == 0 }) { "filtering isn't working. ${filtering.joinToString()}" }
    }

    @Test
    fun setSourceMissCatch() {
        val (list, filtering) = makeTestData()

        val changeIndex = 3
        val expectIndex = 2
        val newItem = 32

        var callbackOccurred = false

        filtering.onAdd += { char, index ->
            assertEquals(newItem, char)
            assertEquals(expectIndex, index)

            callbackOccurred = true
        }
        filtering.onRemove += { item, index -> fail() }
        filtering.onChange += { old, item, index -> fail() }
        list[changeIndex] = newItem

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun setSourceCatchCatch() {
        val (list, filtering) = makeTestData()

        val changeIndex = 4
        val expectIndex = 2
        val newItem = 32
        val oldItem = list[changeIndex]

        var callbackOccurred = false
        filtering.onChange += { old, char, index ->
            assertEquals(oldItem, old)
            assertEquals(newItem, char)
            assertEquals(expectIndex, index)

            callbackOccurred = true
        }

        filtering.onAdd += { item, index -> fail() }
        filtering.onRemove += { item, index -> fail() }
        list[changeIndex] = newItem

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun setSourceCatchMiss() {
        val (list, filtering) = makeTestData()

        val changeIndex = 4
        val newItem = 33
        val expectIndex = 2
        val oldItem = list[changeIndex]

        var callbackOccurred = false
        filtering.onRemove += { char, index ->
            assertEquals(oldItem, char)
            assertEquals(expectIndex, index)

            callbackOccurred = true
        }

        filtering.onAdd += { item, index -> fail() }
        filtering.onChange += { old, item, index -> fail() }
        list[changeIndex] = newItem

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun setSourceMissMiss() {
        val (list, filtering) = makeTestData()

        val changeIndex = 3
        val newItem = 33

        filtering.onAdd += { item, index -> fail() }
        filtering.onRemove += { item, index -> fail() }
        filtering.onChange += { old, item, index -> fail() }
        list[changeIndex] = newItem
    }

    @Test
    fun addSourceCatch() {
        val (list, filtering) = makeTestData()

        val newItem = 22
        val expectIndex = 4

        var callbackOccurred = false
        filtering.onAdd += { char, index ->
            assertEquals(newItem, char)
            assertEquals(expectIndex, index)

            callbackOccurred = true
        }
        list.add(newItem)

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun removeSourceMiss() {
        val (list, filtering) = makeTestData()

        val removeIndex = 3

        filtering.onRemove += { char, index ->
            fail()
        }
        list.removeAt(removeIndex)

        filtering.last()
    }

//    @Test
//    fun addAtSource(){
//        val addIndex = 2
//        val newItem = 'z'
//
//        var callbackOccurred = false
//        val list = makeTestList()
//        val originalSize = list.size
//
//        list.onAdd += { char, index ->
//            assertEquals(newItem, char)
//            assertEquals(addIndex, index)
//
//            callbackOccurred = true
//        }
//        list.add(addIndex, 'z')
//
//        assertEquals(originalSize+1, list.size)
//        assert(callbackOccurred, {"callback occurred"})
//    }
//
//    @Test
//    fun removeAtSource(){
//        val removeIndex = 2
//
//        var callbackOccurred = false
//        val list = makeTestList()
//        val originalSize = list.size
//        val oldElement = list[removeIndex]
//
//        list.onRemove += { char, index ->
//            assertEquals(char, oldElement)
//            assertEquals(removeIndex, index)
//
//            callbackOccurred = true
//        }
//        list.removeAt(removeIndex)
//
//        assertEquals(originalSize-1, list.size)
//        assert(callbackOccurred, {"callback occurred"})
//    }
//
//    @Test
//    fun moveSource(){
//        val sourceIndex = 2
//        val destIndex = 3
//
//        var callbackOccurred = false
//        val list = makeTestList()
//        val originalSize = list.size
//
//        list.onMove += { char, oldIndex, index ->
//            assertEquals(char, 'c')
//            assertEquals(sourceIndex, oldIndex)
//            assertEquals(destIndex, index)
//
//            callbackOccurred = true
//        }
//        list.move(sourceIndex,destIndex)
//
//        assertEquals(originalSize, list.size)
//        assert(callbackOccurred, {"callback occurred"})
//    }

    @Test
    fun set() {
        val (list, filtering) = makeTestData()

        val changeIndex = 2
        val newItem = 22
        val oldItem = filtering[changeIndex]
        val expectedSize = filtering.size

        var callbackOccurred = false
        filtering.onChange += { oldChar, char, index ->
            assertEquals(oldItem, oldChar)
            assertEquals(newItem, char)
            assertEquals(changeIndex, index)

            callbackOccurred = true
        }
        filtering[changeIndex] = newItem

        assertEquals(expectedSize, filtering.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun add() {
        val (list, filtering) = makeTestData()

        val newItem = 22
        val expectedSize = filtering.size

        var callbackOccurred = false
        filtering.onAdd += { char, index ->
            assertEquals(newItem, char)
            assertEquals(expectedSize, index)

            callbackOccurred = true
        }
        filtering.add(newItem)

        assertEquals(expectedSize + 1, filtering.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun addAt() {
        val (list, filtering) = makeTestData()

        val addIndex = 2
        val newItem = 22

        var callbackOccurred = false
        val originalSize = filtering.size

        filtering.onAdd += { char, index ->
            assertEquals(newItem, char)
            assertEquals(addIndex, index)

            callbackOccurred = true
        }
        filtering.add(addIndex, newItem)

        assertEquals(originalSize + 1, filtering.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun removeAt() {
        val (list, filtering) = makeTestData()

        val removeIndex = 2
        val oldElement = filtering[removeIndex]
        val originalSize = filtering.size

        var callbackOccurred = false
        filtering.onRemove += { char, index ->
            assertEquals(char, oldElement)
            assertEquals(removeIndex, index)

            callbackOccurred = true
        }
        filtering.removeAt(removeIndex)

        assertEquals(originalSize - 1, filtering.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun removeAtEnd() {
        val (list, filtering) = makeTestData()

        val removeIndex = filtering.lastIndex
        val oldElement = filtering[removeIndex]
        val originalSize = filtering.size

        var callbackOccurred = false
        filtering.onRemove += { char, index ->
            assertEquals(char, oldElement)
            assertEquals(removeIndex, index)

            callbackOccurred = true
        }
        filtering.removeAt(removeIndex)

        assertEquals(originalSize - 1, filtering.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun move() {
        val (list, filtering) = makeTestData()

        val sourceIndex = 2
        val destIndex = 3

        var callbackOccurred = false
        val originalSize = filtering.size
        val oldElement = filtering[sourceIndex]

        filtering.onMove += { char, oldIndex, index ->
            assertEquals(char, oldElement)
            assertEquals(sourceIndex, oldIndex)
            assertEquals(destIndex, index)

            callbackOccurred = true
        }
        filtering.move(sourceIndex, destIndex)

        assertEquals(originalSize, filtering.size)
        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun iterator() {
        val (list, filtering) = makeTestData()
        var index = 0
        for (item in filtering) {
            assertEquals(filtering[index], item)
            index++
        }
        assertEquals(filtering.size, index)
    }
}