package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Podcast
import com.example.ui.theme.GoogleBlue

@Composable
fun PodcastCard(
    podcast: Podcast,
    onPodcastClicked: () -> Unit,
    onSubscribeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .width(160.dp)
            .clickable { onPodcastClicked() }
            .testTag("podcast_card_${podcast.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(podcast.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = podcast.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Category tag on top right
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = podcast.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = podcast.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
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

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${podcast.rating}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Subscribe chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (podcast.isSubscribed) MaterialTheme.colorScheme.surfaceVariant else GoogleBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSubscribeClicked() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (podcast.isSubscribed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Abone Olundu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        Text(
                            text = if (podcast.isSubscribed) "Abone" else "Abone Ol",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (podcast.isSubscribed) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionAvatarItem(
    podcast: Podcast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .clickable { onClick() }
            .testTag("sub_avatar_${podcast.id}")
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(podcast.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = podcast.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            )

            // Red dot indicating new episode
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(com.example.ui.theme.GoogleRed)
                    .padding(2.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = podcast.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
