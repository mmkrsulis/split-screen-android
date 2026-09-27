// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid
import android.app.Application
import io.github.mmkrsulis.splitscreenandroid.data.*
import kotlinx.coroutines.*
class SplitScreenApplication:Application(){ val appScope=CoroutineScope(SupervisorJob()+Dispatchers.IO); val repository:PairRepository by lazy{DataStorePairRepository(this,appScope)} }
