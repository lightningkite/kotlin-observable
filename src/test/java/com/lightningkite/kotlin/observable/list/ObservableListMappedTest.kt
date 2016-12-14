package com.lightningkite.kotlin.observable.list

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by joseph on 9/26/16.
 */
class ObservableListMappedTest {

    class TestData(
            val label: String,
            val source: ObservableList<Char>,
            val transformed: ObservableList<Char>
    )

    inline fun makeTestData(label: String, transforms: ObservableList<Char>.() -> ObservableList<Char>): TestData {
        val copy = observableListOf('b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p')
        return TestData(label, copy, copy.transforms())
    }

    @Test
    fun testMapping() {
        val testData = makeTestData("mapping") { mapping { it + 1 }.mapping { it - 1 } }
        for (i in testData.source.indices) {
            assertEquals(testData.source[i], testData.transformed[i])
        }
    }

    @Test
    fun testOnChange() {
        val testData = makeTestData("mapping") { mapping { it + 1 }.mapping { it - 1 } }
        for (i in testData.source.indices) {
            val startElement = testData.source[i]
            val newElement = (startElement.toInt() - 1).toChar()
            println("Modifying element $startElement to $newElement")
            val callback = { old: Char, new: Char, index: Int ->
                println("onChange: $old to $new")
                assertEquals(startElement, old)
                assertEquals(newElement, new)
                val validIndices = testData.transformed.mapIndexedNotNull { index, it -> if (it == newElement) index else null }
                assert(validIndices.contains(index), { "Index $index is not a valid index.  Valid indices are $validIndices" })
            }
            testData.transformed.onChange += callback
            testData.source[i] = newElement
            assert(testData.transformed.onChange.remove(callback))
        }
    }
}