package com.campuswave.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.campuswave.app.data.models.Banner
import kotlinx.coroutines.delay
import com.campuswave.app.data.network.ApiConfig
import com.campuswave.app.utils.UrlUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AutoScrollingBanner(
    banners: List<Banner>,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll logic
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000) // 5 seconds
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    var showPreview by remember { mutableStateOf(false) }
    var previewUrl by remember { mutableStateOf("") }

    if (showPreview) {
        ZoomableImageDialog(
            imageUrl = previewUrl,
            onDismiss = { showPreview = false }
        )
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val banner = banners[page]
            val baseUrl = ApiConfig.BASE_URL.removeSuffix("/api/")
            val imageUrl = UrlUtils.joinUrl(baseUrl, banner.image_url)

            AsyncImage(
                model = imageUrl,
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        previewUrl = imageUrl ?: ""
                        showPreview = true
                    }
            )
        }

        // Indicators
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(banners.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
