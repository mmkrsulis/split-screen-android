// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.data

import io.github.mmkrsulis.splitscreenandroid.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class InMemoryPairRepositoryTest {
    private fun pair(name: String, favorite: Boolean=false, used: Long?=null) = AppPair(name=name, favorite=favorite, lastUsedAt=used,
        appA=AppTarget("a.$name", "a.$name.Main", "A"), appB=AppTarget("b.$name", "b.$name.Main", "B"))

    @Test fun `favorites precede recent then alphabetical`() = runTest {
        val repo = InMemoryPairRepository()
        repo.save(pair("Zulu", used=20)); repo.save(pair("Alpha", favorite=true)); repo.save(pair("Beta", used=30))
        assertEquals(listOf("Alpha", "Beta", "Zulu"), repo.pairs.value.map { it.name })
    }

    @Test fun `mark launched updates matching pair only`() = runTest {
        val repo = InMemoryPairRepository(); val saved = repo.save(pair("One")); repo.markLaunched(saved.id, 99)
        assertEquals(99L, repo.pairs.value.single().lastUsedAt)
        assertFalse(repo.markLaunched("missing", 1))
    }
}
