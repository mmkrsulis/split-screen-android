// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.data
import io.github.mmkrsulis.splitscreenandroid.model.AppPair
import kotlinx.coroutines.flow.*
interface PairRepository { val pairs:StateFlow<List<AppPair>>; suspend fun save(pair:AppPair):AppPair; suspend fun delete(id:String):Boolean; suspend fun find(id:String):AppPair?; suspend fun markLaunched(id:String, at:Long=System.currentTimeMillis()):Boolean }
class InMemoryPairRepository:PairRepository {
 private val raw=MutableStateFlow<List<AppPair>>(emptyList()); override val pairs:StateFlow<List<AppPair>> = raw.asStateFlow()
 override suspend fun save(pair:AppPair):AppPair { val now=System.currentTimeMillis(); val value=pair.copy(updatedAt=now); raw.value=sort(raw.value.filterNot{it.id==value.id}+value); return value }
 override suspend fun delete(id:String):Boolean { val old=raw.value; raw.value=old.filterNot{it.id==id}; return old.size!=raw.value.size }
 override suspend fun find(id:String)=raw.value.find{it.id==id}
 override suspend fun markLaunched(id:String,at:Long):Boolean { val p=find(id)?:return false; save(p.copy(lastUsedAt=at)); return true }
 private fun sort(v:List<AppPair>)=v.sortedWith(compareByDescending<AppPair>{it.favorite}.thenByDescending{it.lastUsedAt?:Long.MIN_VALUE}.thenBy(String.CASE_INSENSITIVE_ORDER){it.name})
}
