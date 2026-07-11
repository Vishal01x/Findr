package com.exa.android.reflekt.loopit.data.feature.main.placement

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks whether the currently logged-in user is a placement admin.
 *
 * Firestore structure:
 *   placement_admins/allowed_uids  {  uids: ["uid1", "uid2", ...] }
 *
 * To grant admin access, a super-admin just adds a UID to the list in
 * the Firebase Console — no code change required.
 */
@Singleton
class PlacementAdminRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COLLECTION = "placement_admins"
        private const val DOC_ID     = "allowed_uids"
        private const val FIELD_UIDS = "uids"
    }

    /**
     * Returns true if the current user's UID is in the admin list.
     * Returns false on any error or if the user is not logged in.
     */
    suspend fun isCurrentUserAdmin(): Boolean {
        // Hardcoded for testing. Uncomment the Firestore logic below for production.
        val email = auth.currentUser?.email
        return email == "vishaldangi838@gmail.com"
        
        /*
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val snapshot = firestore
                .collection(COLLECTION)
                .document(DOC_ID)
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            val uids = snapshot.get(FIELD_UIDS) as? List<String> ?: emptyList()
            uid in uids
        } catch (e: Exception) {
            false
        }
        */
    }
}
