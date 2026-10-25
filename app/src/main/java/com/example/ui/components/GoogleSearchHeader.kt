package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.theme.GoogleBlue
import com.example.ui.theme.GoogleGreen
import com.example.ui.theme.GoogleRed
import com.example.ui.theme.GoogleYellow

@Composable
fun GoogleSearchHeader(
    currentUser: UserProfile?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onSearchBarClicked: () -> Unit,
    onProfileClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        ColumnWrapper {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google Podcasts 4-color antenna / waveform icon
                GoogleWaveformLogo(modifier = Modifier.padding(end = 10.dp))

                Text(
                    text = "iRadyo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Profile Avatar button (Google style circle)
                val avatarColor = try {
                    Color(android.graphics.Color.parseColor(currentUser?.avatarColorHex ?: "#1A73E8"))
                } catch (_: Exception) {
                    GoogleBlue
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(avatarColor)
                        .clickable { onProfileClicked() }
                        .testTag("profile_avatar_button")
                ) {
                    val initial = currentUser?.fullName?.take(1)?.uppercase()
                        ?: currentUser?.username?.take(1)?.uppercase()
                        ?: "İ"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Google Search pill bar
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(50.dp)
                    .clickable { onSearchBarClicked() }
                    .testTag("search_bar_surface")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    "Podcast veya bölüm ara...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 15.sp
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_text_input")
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (searchQuery.isNotBlank()) searchQuery else "Podcast'lerde ara...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ColumnWrapper(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column {
        content()
    }
}

@Composable
fun GoogleWaveformLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Green
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(11.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GoogleGreen)
        )
        Spacer(modifier = Modifier.width(3.dp))
        // Red
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GoogleRed)
        )
        Spacer(modifier = Modifier.width(3.dp))
        // Blue
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GoogleBlue)
        )
        Spacer(modifier = Modifier.width(3.dp))
        // Yellow
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(17.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GoogleYellow)
        )
        Spacer(modifier = Modifier.width(3.dp))
        // Green
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(GoogleGreen)
        )
    }
}
