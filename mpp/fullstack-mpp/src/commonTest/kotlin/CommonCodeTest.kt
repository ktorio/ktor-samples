package io.ktor.samples.fullstack.common

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonCodeTest {
    @Test
    fun testGetCommonWorldString() {
        assertEquals("common-world", getCommonWorldString())
    }
}
