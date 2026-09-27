// SPDX-License-Identifier: AGPL-3.0-only
package io.github.mmkrsulis.splitscreenandroid.ui

import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mmkrsulis.splitscreenandroid.BuildConfig
import io.github.mmkrsulis.splitscreenandroid.domain.homeSections
import io.github.mmkrsulis.splitscreenandroid.domain.validatePair
import io.github.mmkrsulis.splitscreenandroid.launcher.AppDiscovery
import io.github.mmkrsulis.splitscreenandroid.launcher.LaunchResult
import io.github.mmkrsulis.splitscreenandroid.launcher.NativeSplitScreenLauncher
import io.github.mmkrsulis.splitscreenandroid.launcher.shouldRecordAttempt
import io.github.mmkrsulis.splitscreenandroid.model.AppPair
import io.github.mmkrsulis.splitscreenandroid.model.AppTarget
import io.github.mmkrsulis.splitscreenandroid.shortcuts.ShortcutSupport
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { SplitScreenApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreenApp(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var screen by rememberSaveable { mutableStateOf("home") }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = state.pairs.find { it.id == editingId }
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (screen == "home") "Split Screen" else if (screen == "settings") "Settings" else "Create pair") },
                navigationIcon = { if (screen != "home") IconButton({ screen = "home" }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { if (screen == "home") IconButton({ screen = "settings" }) { Icon(Icons.Default.Settings, "Settings") } },
            )
        },
        floatingActionButton = {
            if (screen == "home") ExtendedFloatingActionButton(
                text = { Text("Create pair") },
                icon = { Icon(Icons.Default.Add, null) },
                onClick = { editingId = null; screen = "edit" },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (screen) {
                "home" -> Home(state, { editingId = it.id; screen = "edit" }, vm)
                "edit" -> PairEditor(editing, { vm.save(it); screen = "home" }, { screen = "home" })
                else -> SettingsScreen()
            }
        }
    }
    LaunchedEffect(state.message) {
        state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); vm.clearMessage() }
    }
}

@Composable
private fun Home(state: HomeState, onEdit: (AppPair) -> Unit, vm: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (state.pairs.isEmpty()) {
        Column(Modifier.padding(32.dp)) {
            Text("No app pairs yet", style = MaterialTheme.typography.headlineSmall)
            Text("Create a pair to launch two apps side by side.")
        }
    } else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        fun section(title: String, pairs: List<AppPair>) {
            if (pairs.isNotEmpty()) {
                item { Text(title, style = MaterialTheme.typography.titleLarge) }
                items(pairs, key = { it.id }) { pair ->
                    PairCard(pair, { onEdit(pair) }, { vm.favorite(pair) }, { vm.delete(pair.id) }, {
                        val accepted = ShortcutSupport(context).request(pair)
                        Toast.makeText(context, if (accepted) "Shortcut request sent" else "Pinned shortcuts unavailable", Toast.LENGTH_SHORT).show()
                    }, {
                        scope.launch {
                            val result = NativeSplitScreenLauncher(context).launch(pair)
                            if (shouldRecordAttempt(result)) vm.recordLaunchAttempt(pair.id)
                            Toast.makeText(context, resultText(result), Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
        val sections = homeSections(state.pairs)
        section("Favorites", sections.favorites)
        section("Recent", sections.recent)
        section("All", sections.all)
    }
}

private fun resultText(result: LaunchResult) = when (result) {
    LaunchResult.Success -> "Launch completed"
    is LaunchResult.PartialSuccess -> result.message
    is LaunchResult.Unsupported -> result.reason
    is LaunchResult.PermissionRequired -> result.reason
    is LaunchResult.AppUnavailable -> "${result.label} is unavailable"
    is LaunchResult.Failed -> result.message
}

@Composable
private fun PairCard(pair: AppPair, onEdit: () -> Unit, onFavorite: () -> Unit, onDelete: () -> Unit, onShortcut: () -> Unit, onLaunch: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(pair.name, style = MaterialTheme.typography.titleMedium)
                Text("${pair.appA.label} + ${pair.appB.label}")
                Text("${pair.appA.packageName} · ${pair.appB.packageName}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onLaunch, Modifier.semantics { contentDescription = "Launch ${pair.name}" }) { Icon(Icons.Default.PlayArrow, null) }
            Box {
                IconButton({ menu = true }) { Icon(Icons.Default.MoreVert, "More actions") }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text("Edit") }, { menu = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem({ Text(if (pair.favorite) "Unfavorite" else "Favorite") }, { menu = false; onFavorite() }, leadingIcon = { Icon(Icons.Default.Star, null) })
                    DropdownMenuItem({ Text("Pin shortcut") }, { menu = false; onShortcut() })
                    DropdownMenuItem({ Text("Delete") }, { menu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null) })
                }
            }
        }
    }
}

private val appTargetSaver = listSaver<AppTarget?, String>(
    save = { target -> target?.let { listOf(it.packageName, it.componentName, it.label) } ?: emptyList() },
    restore = { values -> values.takeIf { it.size == 3 }?.let { AppTarget(it[0], it[1], it[2]) } },
)

@Composable
private fun PairEditor(existing: AppPair?, onSave: (AppPair) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppTarget>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name ?: "") }
    var a by rememberSaveable(existing?.id, stateSaver = appTargetSaver) { mutableStateOf(existing?.appA) }
    var b by rememberSaveable(existing?.id, stateSaver = appTargetSaver) { mutableStateOf(existing?.appB) }
    var query by rememberSaveable(existing?.id) { mutableStateOf("") }
    var choosingA by rememberSaveable(existing?.id) { mutableStateOf(a == null) }
    LaunchedEffect(context) {
        apps = AppDiscovery(context.applicationContext).discover()
        loading = false
    }
    Column(Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(name, { name = it }, label = { Text("Pair name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(query, { query = it }, label = { Text("Search apps") }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(choosingA, { choosingA = true }, { Text("A: ${a?.label ?: "Choose"}") })
            FilterChip(!choosingA, { choosingA = false }, { Text("B: ${b?.label ?: "Choose"}") })
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(Modifier.weight(1f)) {
            items(apps.filter { query.isBlank() || it.label.contains(query, true) || it.packageName.contains(query, true) }, key = { it.packageName }) { app ->
                ListItem(
                    headlineContent = { Text(app.label) },
                    supportingContent = { Text(app.packageName) },
                    modifier = Modifier.semantics { contentDescription = "Select ${app.label}" },
                    trailingContent = { Button({ if (choosingA) { a = app; choosingA = false } else b = app }) { Text("Select") } },
                )
            }
        }
        val candidate = if (a != null && b != null) existing?.copy(name = name, appA = a!!, appB = b!!) ?: AppPair(name = name, appA = a!!, appB = b!!) else null
        Text("Preview: ${a?.label ?: "App A"} | ${b?.label ?: "App B"}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onCancel) { Text("Cancel") }
            Button({ candidate?.let(onSave) }, enabled = candidate != null && validatePair(candidate) == null) { Text("Save") }
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Launch mode", style = MaterialTheme.typography.titleLarge)
            Text("Uses Android public activity-launch APIs. Split-screen placement is best effort and controlled by Android, the device maker, and target apps.")
        }
        item {
            Text("Diagnostics", style = MaterialTheme.typography.titleLarge)
            val report = "Split Screen ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\nAndroid ${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}\nDevice ${Build.MANUFACTURER} ${Build.MODEL}"
            Button({ (context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).setPrimaryClip(ClipData.newPlainText("diagnostics", report)) }) { Text("Copy privacy-safe report") }
            Text(report)
        }
        item {
            Text("About", style = MaterialTheme.typography.titleLarge)
            Text("Version ${BuildConfig.VERSION_NAME} · AGPL-3.0-only")
            Text("No analytics, network permission, accessibility service, screen-content access, or broad package visibility.")
        }
    }
}
