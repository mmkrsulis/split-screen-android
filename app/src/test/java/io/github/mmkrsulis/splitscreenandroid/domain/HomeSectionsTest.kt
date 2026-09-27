// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.domain

import io.github.mmkrsulis.splitscreenandroid.model.AppPair
import io.github.mmkrsulis.splitscreenandroid.model.AppTarget
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSectionsTest {
    private fun pair(name: String, used: Long?, favorite: Boolean = false) = AppPair(
        name = name, favorite = favorite, lastUsedAt = used,
        appA = AppTarget("a.$name", "a.$name/Main", "A"),
        appB = AppTarget("b.$name", "b.$name/Main", "B"),
    )

    @Test fun `recent overflow remains visible in all`() {
        val input = (1..7).map { pair("p$it", it.toLong()) } + pair("never", null) + pair("fav", 9, true)
        val sections = homeSections(input)
        assertEquals(1, sections.favorites.size)
        assertEquals(5, sections.recent.size)
        assertEquals(setOf("p1", "p2", "never"), sections.all.map { it.name }.toSet())
        assertEquals(input.map { it.id }.toSet(), (sections.favorites + sections.recent + sections.all).map { it.id }.toSet())
    }

    @Test fun `same package is invalid even when components differ`() {
        val a = AppTarget("same.pkg", "same.pkg/One", "One")
        val b = AppTarget("same.pkg", "same.pkg/Two", "Two")
        assertEquals(PairValidation.SAME_APP, validatePair(AppPair(name = "bad", appA = a, appB = b)))
    }
}
