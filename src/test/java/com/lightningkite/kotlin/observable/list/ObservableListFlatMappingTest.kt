package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by joseph on 9/26/16.
 */
class ObservableListFlatMappingTest {

    fun makeTestList() = observableListOf(
            observableListOf('a', 'b', 'c'),
            observableListOf('e', 'f', 'g'),
            observableListOf('h', 'i', 'j')
    )

    @Test
    fun addToSub() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        flat.onAdd += { char, index ->
            assertEquals(char, 'z')
            assertEquals(4, index)

            callbackOccurred = true
        }
        list[1].add(1, 'z')

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun addToWhole() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        val sublist = observableListOf('x', 'y', 'z')
        var currentIndex = 0

        flat.onAdd += { char, index ->
            assertEquals(sublist[currentIndex], char)
            assertEquals(currentIndex + 3, index)
            currentIndex++

            callbackOccurred = true
        }
        list.add(1, sublist)

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun removeFromSub() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        flat.onRemove += { char, index ->
            assertEquals(char, 'f')
            assertEquals(4, index)

            callbackOccurred = true
        }
        list[1].removeAt(1)

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun removeFromWhole() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        val sublist = list[1]
        var currentIndex = sublist.lastIndex

        flat.onRemove += { char, index ->
            assertEquals(sublist[currentIndex], char)
            assertEquals(currentIndex + 3, index)
            currentIndex--

            callbackOccurred = true
        }
        list.removeAt(1)

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun changeToSub() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        flat.onChange += { old, char, index ->
            assertEquals(old, 'f')
            assertEquals(char, 'z')
            assertEquals(4, index)

            callbackOccurred = true
        }
        list[1][1] = 'z'

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun changeToWhole() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        val newList = observableListOf('x', 'y')
        val oldList = list[1]
        var newIndex = 0
        var oldIndex = oldList.lastIndex

        flat.onRemove += { char, index ->
            assertEquals(oldList[oldIndex], char)
            assertEquals(oldIndex + 3, index)
            oldIndex--

            callbackOccurred = true
        }
        flat.onAdd += { char, index ->
            assertEquals(newList[newIndex], char)
            assertEquals(newIndex + 3, index)
            newIndex++

            callbackOccurred = true
        }
        list[1] = newList

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun moveInSub() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        flat.onMove += { char, oldIndex, index ->
            assertEquals(char, 'f')
            assertEquals(4, oldIndex)
            assertEquals(5, index)
            callbackOccurred = true
        }
        list[1].move(1, 2)

        assert(callbackOccurred, { "callback occurred" })
    }

    @Test
    fun moveInWhole() {
        var callbackOccurred = false
        val list = makeTestList()
        val flat = list.flatMapping { it }

        val sublist = list[1]
        var removeIndex = sublist.lastIndex
        var addIndex = 0

        flat.onRemove += { char, index ->
            assertEquals(sublist[removeIndex], char)
            assertEquals(removeIndex + 3, index)
            removeIndex--

            callbackOccurred = true
        }
        flat.onAdd += { char, index ->
            assertEquals(sublist[addIndex], char)
            assertEquals(addIndex + 6, index)
            addIndex++

            callbackOccurred = true
        }
        list.move(1, 2)

        assert(callbackOccurred, { "callback occurred" })
    }

}