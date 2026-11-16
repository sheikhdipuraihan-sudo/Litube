package com.hhst.youtubelite.ui.nativehome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.hhst.youtubelite.extractor.VideoId
import com.hhst.youtubelite.ui.YoutubeThumb

@Composable
fun NativeHomeScreen(
    state: NativeCatalogState,
    onSearch: (String) -> Unit,
    onPlayVideo: (String) -> Unit,
    onOpenWebSearch: (String) -> Unit,
    onOpenRoute: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf(state.query) }
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = state.items.firstOrNull { it.id == selectedId }

    fun submit() {
        val value = query.trim()
        if (value.isBlank()) return
        val id = VideoId.parse(value)
        if (id != null) onPlayVideo(VideoId.watchUrl(id)) else onSearch(value)
    }

    if (selected != null) {
        NativeVideoDetails(
            item = selected,
            onBack = { selectedId = null },
            onPlay = { onPlayVideo(selected.url) },
            modifier = modifier,
        )
        return
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("LiTube", style = MaterialTheme.typography.titleLarge)
                }
                Button(onClick = onOpenSettings) { Text("You") }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search or paste a video link") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { submit() }),
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = ::submit, enabled = query.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                Text("Search")
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("All", "Music", "Live", "Gaming", "News", "Mixes").forEachIndexed { index, label ->
                    FilterChip(selected = index == 0, onClick = { }, label = { Text(label) })
                }
            }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
                when {
                    state.loading -> Column(
                    modifier = Modifier.fillMaxSize().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    ) { CircularProgressIndicator() }
                    state.error != null -> NativeCatalogError(state.error, onRetry = { onSearch(state.query) })
                    state.items.isNotEmpty() -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                    item { Text("Results for ${state.query}", style = MaterialTheme.typography.titleMedium) }
                    items(state.items, key = { it.id }) { item ->
                        NativeVideoCard(item, onClick = { selectedId = item.id })
                    }
                    }
                    else -> NativeHomeEmpty(onOpenWebSearch = onOpenWebSearch)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TextButton(onClick = { onOpenRoute("https://m.youtube.com") }) { Text("Home") }
                TextButton(onClick = { onOpenRoute("https://m.youtube.com/shorts") }) { Text("Shorts") }
                TextButton(onClick = { onOpenRoute("https://m.youtube.com/feed/subscriptions") }) { Text("Subs") }
                TextButton(onClick = { onOpenRoute("https://m.youtube.com/feed/library") }) { Text("Library") }
                TextButton(onClick = onOpenSettings) { Text("You") }
            }
        }
    }
}

@Composable
private fun NativeVideoDetails(
    item: NativeVideoCard,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Button(onClick = onBack) { Text("Back to results") }
        YoutubeThumb(
            url = item.thumbnailUrl,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium),
            contentDescription = item.title,
        )
        Text(item.title, style = MaterialTheme.typography.headlineSmall)
        Text(item.author, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (item.viewCount > 0) Text("${item.viewCount} views",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onPlay, modifier = Modifier.fillMaxWidth()) { Text("Play") }
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                "This video opens in LiTube's native Media3 player. Playback controls, subtitles, quality, casting, PiP, and background audio are native Android UI.",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun NativeVideoCard(item: NativeVideoCard, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            YoutubeThumb(
                url = item.thumbnailUrl,
                modifier = Modifier.width(142.dp).aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium),
                contentDescription = item.title,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 3)
                Spacer(Modifier.height(5.dp))
                Text(item.author, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                if (item.viewCount > 0) Text(
                    "${item.viewCount} views",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NativeCatalogError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun NativeHomeEmpty(onOpenWebSearch: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Native app UI", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Search results and video cards now render as real Android Compose UI. Selecting a card opens the existing native Media3 player.")
            }
        }
        Button(onClick = { onOpenWebSearch("") }, modifier = Modifier.fillMaxWidth()) {
            Text("Open remaining catalog routes")
        }
    }
}
