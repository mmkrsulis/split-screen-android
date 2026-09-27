// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.shortcuts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortcutRequestTest {
    private val id = "123e4567-e89b-12d3-a456-426614174000"

    @Test fun `accepts exact action and canonical pair uri`() {
        assertEquals(id, validateShortcutRequest(ShortcutSupport.ACTION, "splitscreen", "pair", listOf(id), id))
    }

    @Test fun `rejects wrong action malformed uuid and mismatched extra`() {
        assertNull(validateShortcutRequest("other", "splitscreen", "pair", listOf(id), id))
        assertNull(validateShortcutRequest(ShortcutSupport.ACTION, "splitscreen", "pair", listOf("not-a-uuid"), "not-a-uuid"))
        assertNull(validateShortcutRequest(ShortcutSupport.ACTION, "splitscreen", "pair", listOf(id), "123e4567-e89b-12d3-a456-426614174001"))
    }
}
