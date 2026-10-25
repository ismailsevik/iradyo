package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Category
import com.example.data.model.Podcast
import com.example.ui.components.PodcastCard
import com.example.ui.theme.GoogleBlue

@Composable
fun ExploreScreen(
    categories: List<Category>,
    selectedCategory: Category?,
    onSelectCategory: (Category?) -> Unit,
    podcasts: List<Podcast>,
    onPodcastClicked: (Podcast) -> Unit,
    onToggleSubscribe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Section Header: "Podcast Keşfet"
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "Podcast Keşfet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Öne çıkan, popüler ve kategorilere göre yeni podcast'ler",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // "Tümü" chip
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("Tüm Kategoriler") },
                        leadingIcon = if (selectedCategory == null) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_all")
                    )
                }

                items(categories, key = { it.id }) { cat ->
                    val isSelected = selectedCategory?.id == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) onSelectCategory(null) else onSelectCategory(cat)
                        },
                        label = { Text(cat.name) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("category_chip_${cat.id}")
                    )
                }
            }
        }

        // Featured Podcasts (Öne Çıkanlar) - show if no category filter
        if (selectedCategory == null) {
            item {
                Text(
                    text = "Öne Çıkan Podcast'ler",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(podcasts.take(4), key = { "featured_${it.id}" }) { podcast ->
                        PodcastCard(
                            podcast = podcast,
                            onPodcastClicked = { onPodcastClicked(podcast) },
                            onSubscribeClicked = { onToggleSubscribe(podcast.id) }
                        )
                    }
                }
            }

            // Top Charts / En Popülerler
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = GoogleBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "En Popüler Podcast'ler (Top Listeler)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Ranked List
            itemsIndexed(podcasts, key = { index, item -> "popular_${item.id}_$index" }) { index, podcast ->
                TopChartItem(
                    rank = index + 1,
                    podcast = podcast,
                    onClick = { onPodcastClicked(podcast) },
                    onSubscribe = { onToggleSubscribe(podcast.id) }
                )
            }
        } else {
            // Category Selected - Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedCategory.name} Podcast'leri",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${podcasts.size} yayın",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(podcasts, key = { it.id }) { podcast ->
                TopChartItem(
                    rank = null,
                    podcast = podcast,
                    onClick = { onPodcastClicked(podcast) },
                    onSubscribe = { onToggleSubscribe(podcast.id) }
                )
            }
        }
    }
}

@Composable
fun TopChartItem(
    rank: Int?,
    podcast: Podcast,
    onClick: () -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("top_chart_item_${podcast.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank number badge if present
            if (rank != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 4.dp)
                ) {
                    Text(
                        text = "$rank",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) GoogleBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cover artwork
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(podcast.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = podcast.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title, author, category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = podcast.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = podcast.author,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = podcast.category,
                        fontSize = 11.sp,
                        color = GoogleBlue,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${podcast.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Subscribe button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (podcast.isSubscribed) MaterialTheme.colorScheme.surfaceVariant else GoogleBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSubscribe() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (podcast.isSubscribed) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Text(
                        text = if (podcast.isSubscribed) "Kayıtlı" else "Abone Ol",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (podcast.isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                    )
                }
            }
        }
    }
}
