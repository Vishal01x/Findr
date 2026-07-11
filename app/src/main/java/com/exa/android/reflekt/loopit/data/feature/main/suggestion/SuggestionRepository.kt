package com.exa.android.reflekt.loopit.data.feature.main.suggestion

import com.exa.android.reflekt.loopit.core.env.AppCollections
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class SuggestionRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCollection get() = db.collection(AppCollections.USERS)
    private val trendingCollection get() = db.collection(AppCollections.TRENDING)

    suspend fun getSuggestedUsers(): List<SuggestedUser> = withContext(Dispatchers.IO) {
        val currentUserId = auth.currentUser?.uid ?: return@withContext emptyList()

        // 1. Fetch ordered user IDs from trending_profiles/current
        val trendingDoc = try {
            trendingCollection.document("current").get().await()
        } catch (_: Exception) { return@withContext emptyList() }

        @Suppress("UNCHECKED_CAST")
        val userIds = (trendingDoc.get("userIds") as? List<String>)
            ?.filter { it != currentUserId }
            ?.take(5)
            ?: return@withContext emptyList()

        if (userIds.isEmpty()) return@withContext emptyList()

        // 2. Fetch each profile in parallel
        coroutineScope {
            userIds.map { uid ->
                async {
                    try {
                        val doc = usersCollection.document(uid).get().await()
                        val header = doc.get("profileHeader") as? Map<*, *>
                        val name = header?.get("name") as? String ?: return@async null
                        if (name.isBlank()) return@async null

                        @Suppress("UNCHECKED_CAST")
                        val skills = (header["skills"] as? List<String>).orEmpty()
                        val skillStr = doc.getString("skill") ?: ""
                        val allSkills = (skills + skillStr.split(",", ";")
                            .map { it.trim() }.filter { it.length > 1 })
                            .distinct().take(3)

                        SuggestedUser(
                            userId = uid,
                            name = name,
                            profileImageUrl = (header["profileImageUrl"] as? String)
                                ?.takeIf { it.isNotBlank() },
                            headline = (header["headline"] as? String)?.takeIf { it.isNotBlank() }
                                ?: (header["role"] as? String) ?: "",
                            skills = allSkills,
                            matchedSkills = emptyList(),
                            points = 0
                        )
                    } catch (_: Exception) { null }
                }
            }.awaitAll().filterNotNull()
        }
    }
}
