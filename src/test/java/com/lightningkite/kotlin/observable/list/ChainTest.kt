package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by joseph on 9/26/16.
 */
class ChainTest {
    fun makeTestDatas(): List<Pair<ObservableList<Char>, ObservableList<Char>>> {
        val copyA = observableListOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')
        val copyB = observableListOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')
        val copyC = observableListOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')
        val copyD = observableListOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')
        val copyE = observableListOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')
        return listOf(
                copyA to copyA.filtering { it != 'b' },
                copyB to copyB.sorting { a, b -> b < a }.filtering { it != 'c' },
                copyC to copyC.filtering { it != 'b' }.sorting { a, b -> b < a },
                copyD to copyD.sorting { a, b -> b < a }.filtering { it != 'c' },
                copyE to copyE.filtering { it != 'b' }.sorting { a, b -> b < a }
        )
    }

    @Test
    fun onChangeTest() {
        makeTestDatas().forEach {
            for (i in it.first.indices) {
                val startElement = it.first[i]
                val callback = { old: Char, new: Char, index: Int ->
                    assertEquals(startElement, old)
                    assertEquals(it.second.indexOf(startElement), index)
                }
                it.second.onChange += callback
                it.first.updateAt(i)
                it.second.onChange -= callback
            }
        }
    }

    @Test
    fun onUpdateAfterChangeTest() {
        makeTestDatas().forEach {
            for (i in it.first.indices) {
                val startElement = it.first[i]
                var uncalledOrBoth = true
                val callback = { old: Char, new: Char, index: Int ->
                    assertEquals(startElement, old)
                    assertEquals(it.second.indexOf(startElement), index)
                    uncalledOrBoth = false
                }
                val onUpdateCallback = { list: List<Char> ->
                    uncalledOrBoth = true
                }
                it.second.onUpdate += onUpdateCallback
                it.second.onChange += callback
                it.first.updateAt(i)
                it.second.onChange -= callback
                it.second.onUpdate -= onUpdateCallback
                assert(uncalledOrBoth)
            }
        }
    }
}