package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Episode
import com.example.data.model.Podcast
import com.example.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("iRadyo", appName)
  }

  @Test
  fun `episode duration and remaining formatted correctly`() {
    val episode = Episode(
        id = "ep_test",
        podcastId = "pod_test",
        podcastTitle = "Test Podcast",
        podcastImageUrl = "",
        title = "Test Episode",
        description = "Description",
        audioUrl = "http://example.com/audio.mp3",
        durationSeconds = 2460, // 41 min
        releaseDate = "Bugün",
        playbackPositionSeconds = 600 // 10 min played
    )

    assertEquals("41 dk", episode.durationFormatted)
    assertTrue(episode.remainingFormatted.contains("dk kaldı"))
  }

  @Test
  fun `user profile initializes properly`() {
    val user = UserProfile(
        id = "user_1",
        username = "ismail",
        email = "ismail@example.com",
        fullName = "İsmail Şevik",
        totalListeningMinutes = 120
    )

    assertEquals("ismail", user.username)
    assertEquals("İsmail Şevik", user.fullName)
    assertEquals(120, user.totalListeningMinutes)
  }
}
