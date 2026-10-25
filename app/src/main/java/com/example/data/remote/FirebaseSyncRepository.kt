package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Episode
import com.example.data.model.Podcast
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseSyncRepository(private val context: Context) {

    private val auth: FirebaseAuth = Firebase.auth
    private val scope = CoroutineScope(Dispatchers.IO)

    // Initialized with custom named Firestore database
    val firestore: FirebaseFirestore by lazy {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
    }

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow<Long?>(null)
    val lastSyncTimestamp: StateFlow<Long?> = _lastSyncTimestamp.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _firebaseUser.value = firebaseAuth.currentUser
            firebaseAuth.currentUser?.let { user ->
                scope.launch {
                    syncUserProfile(user)
                }
            }
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                _firebaseUser.value = user
                syncUserProfile(user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Failed Google sign-in", e)
            false
        }
    }

    fun signOut() {
        auth.signOut()
        _firebaseUser.value = null
    }

    private suspend fun syncUserProfile(user: FirebaseUser) {
        try {
            val userDoc = firestore.collection("users").document(user.uid)
            val profileData = mapOf(
                "userId" to user.uid,
                "email" to (user.email ?: ""),
                "displayName" to (user.displayName ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc.set(profileData, SetOptions.merge()).await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error saving user profile to Firestore", e)
        }
    }

    suspend fun syncFavoriteToCloud(podcast: Podcast, isFavorited: Boolean) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        try {
            val favDoc = firestore.collection("users").document(uid)
                .collection("favorites").document(podcast.id)

            if (isFavorited) {
                val data = mapOf(
                    "podcastId" to podcast.id,
                    "title" to podcast.title,
                    "author" to podcast.author,
                    "imageUrl" to podcast.imageUrl,
                    "category" to podcast.category,
                    "rating" to podcast.rating,
                    "favoritedAt" to FieldValue.serverTimestamp()
                )
                favDoc.set(data, SetOptions.merge()).await()
            } else {
                favDoc.delete().await()
            }
            _lastSyncTimestamp.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error updating favorite in Firestore", e)
        }
    }

    suspend fun syncSubscriptionToCloud(podcast: Podcast, isSubscribed: Boolean) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        try {
            val subDoc = firestore.collection("users").document(uid)
                .collection("subscriptions").document(podcast.id)

            if (isSubscribed) {
                val data = mapOf(
                    "podcastId" to podcast.id,
                    "title" to podcast.title,
                    "author" to podcast.author,
                    "imageUrl" to podcast.imageUrl,
                    "subscribedAt" to FieldValue.serverTimestamp()
                )
                subDoc.set(data, SetOptions.merge()).await()
            } else {
                subDoc.delete().await()
            }
            _lastSyncTimestamp.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error updating subscription in Firestore", e)
        }
    }

    suspend fun syncHistoryToCloud(episode: Episode) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        try {
            val histDoc = firestore.collection("users").document(uid)
                .collection("history").document(episode.id)

            val data = mapOf(
                "episodeId" to episode.id,
                "podcastId" to episode.podcastId,
                "podcastTitle" to episode.podcastTitle,
                "title" to episode.title,
                "progressSeconds" to episode.playbackPositionSeconds,
                "durationSeconds" to episode.durationSeconds,
                "completed" to episode.isPlayed,
                "listenedAt" to FieldValue.serverTimestamp()
            )
            histDoc.set(data, SetOptions.merge()).await()
            _lastSyncTimestamp.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error updating history in Firestore", e)
        }
    }

    suspend fun syncSavedEpisodeToCloud(episode: Episode, isSaved: Boolean) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        try {
            val epDoc = firestore.collection("users").document(uid)
                .collection("saved_episodes").document(episode.id)

            if (isSaved) {
                val data = mapOf(
                    "episodeId" to episode.id,
                    "podcastId" to episode.podcastId,
                    "podcastTitle" to episode.podcastTitle,
                    "podcastImageUrl" to episode.podcastImageUrl,
                    "title" to episode.title,
                    "description" to episode.description,
                    "audioUrl" to episode.audioUrl,
                    "durationSeconds" to episode.durationSeconds,
                    "releaseDate" to episode.releaseDate,
                    "savedAt" to FieldValue.serverTimestamp()
                )
                epDoc.set(data, SetOptions.merge()).await()
            } else {
                epDoc.delete().await()
            }
            _lastSyncTimestamp.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error saving episode to Firestore", e)
        }
    }

    suspend fun fetchCloudFavorites(): List<String> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("favorites").get().await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error fetching cloud favorites", e)
            emptyList()
        }
    }

    suspend fun fetchCloudSubscriptions(): List<String> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("subscriptions").get().await()
            snapshot.documents.map { it.id }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error fetching cloud subscriptions", e)
            emptyList()
        }
    }

    suspend fun fetchCloudSavedEpisodes(): List<Episode> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("saved_episodes").get().await()
            snapshot.documents.mapNotNull { doc ->
                val epId = doc.getString("episodeId") ?: doc.id
                val podId = doc.getString("podcastId") ?: ""
                val podTitle = doc.getString("podcastTitle") ?: ""
                val podImg = doc.getString("podcastImageUrl") ?: ""
                val title = doc.getString("title") ?: ""
                val desc = doc.getString("description") ?: ""
                val audio = doc.getString("audioUrl") ?: ""
                val duration = doc.getLong("durationSeconds")?.toInt() ?: 1800
                val release = doc.getString("releaseDate") ?: "Kayıtlı"

                if (title.isNotBlank()) {
                    Episode(
                        id = epId,
                        podcastId = podId,
                        podcastTitle = podTitle,
                        podcastImageUrl = podImg,
                        title = title,
                        description = desc,
                        audioUrl = audio,
                        durationSeconds = duration,
                        releaseDate = release,
                        isFavorited = true
                    )
                } else null
            }
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error fetching cloud saved episodes", e)
            emptyList()
        }
    }

    suspend fun deleteAllUserDataFromCloud(): Boolean = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext true
        val uid = user.uid
        try {
            val userDoc = firestore.collection("users").document(uid)

            val subcollections = listOf("favorites", "subscriptions", "history", "saved_episodes")
            for (sub in subcollections) {
                try {
                    val docs = userDoc.collection(sub).get().await()
                    for (doc in docs) {
                        doc.reference.delete().await()
                    }
                } catch (e: Exception) {
                    Log.w("FirebaseSync", "Could not delete subcollection $sub: ${e.message}")
                }
            }

            try {
                userDoc.delete().await()
            } catch (e: Exception) {
                Log.w("FirebaseSync", "Could not delete user root doc: ${e.message}")
            }

            try {
                user.delete().await()
            } catch (e: Exception) {
                Log.w("FirebaseSync", "Firebase user delete requires recent login or failed: ${e.message}")
            }

            auth.signOut()
            _firebaseUser.value = null
            _lastSyncTimestamp.value = null
            true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error deleting cloud account data", e)
            auth.signOut()
            _firebaseUser.value = null
            false
        }
    }
}
