package com.hhst.youtubelite.ui.nativehome

import com.hhst.youtubelite.extractor.VideoId
import com.hhst.youtubelite.extractor.Extractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/** Native catalog item rendered by Compose; no webpage markup is involved. */
data class NativeVideoCard(
    val id: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val viewCount: Long,
    val url: String,
)

/**
 * Catalog provider for native routes. The existing Extractor initializes
 * NewPipe before this repository is used, so it shares the app's downloader,
 * cookies, and extraction configuration.
 */
class NativeCatalogRepository(
    // Keeps NewPipe initialization and the app's configured downloader alive.
    @Suppress("UNUSED_PARAMETER") private val extractor: Extractor,
) {
    suspend fun search(query: String): List<NativeVideoCard> = withContext(Dispatchers.IO) {
        val text = query.trim()
        if (text.isEmpty()) return@withContext emptyList()
        val extractor = ServiceList.YouTube.getSearchExtractor(text)
        extractor.fetchPage()
        extractor.initialPage.items.asSequence()
            .filterIsInstance<StreamInfoItem>()
            .mapNotNull { item ->
                val id = VideoId.parse(item.url) ?: return@mapNotNull null
                NativeVideoCard(
                    id = id,
                    title = item.name.orEmpty(),
                    author = item.uploaderName.orEmpty(),
                    thumbnailUrl = item.thumbnails.firstOrNull()?.url,
                    durationSeconds = item.duration,
                    viewCount = item.viewCount,
                    url = VideoId.watchUrl(id),
                )
            }
            .toList()
    }
}
