// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.domain

import io.github.mmkrsulis.splitscreenandroid.model.AppPair

enum class PairValidation { BLANK_NAME, SAME_APP, INVALID_POSITION }

fun validatePair(pair: AppPair): PairValidation? = when {
    pair.name.isBlank() -> PairValidation.BLANK_NAME
    pair.appA.packageName == pair.appB.packageName -> PairValidation.SAME_APP
    pair.position !in 0.1f..0.9f -> PairValidation.INVALID_POSITION
    else -> null
}

data class HomeSections(
    val favorites: List<AppPair>,
    val recent: List<AppPair>,
    val all: List<AppPair>,
)

fun homeSections(pairs: List<AppPair>): HomeSections {
    val favorites = pairs.filter { it.favorite }
    val nonFavorites = pairs.filterNot { it.favorite }
    val recent = nonFavorites.filter { it.lastUsedAt != null }
        .sortedByDescending { it.lastUsedAt }
        .take(5)
    val recentIds = recent.mapTo(mutableSetOf()) { it.id }
    return HomeSections(favorites, recent, nonFavorites.filterNot { it.id in recentIds })
}

enum class LaunchStrategy { NATIVE_BEST_EFFORT, NATIVE_STRONGEST, UNSUPPORTED }

fun selectStrategy(api: Int, nativeSupported: Boolean): LaunchStrategy = when {
    api < 28 || !nativeSupported -> LaunchStrategy.UNSUPPORTED
    api >= 32 -> LaunchStrategy.NATIVE_STRONGEST
    else -> LaunchStrategy.NATIVE_BEST_EFFORT
}
