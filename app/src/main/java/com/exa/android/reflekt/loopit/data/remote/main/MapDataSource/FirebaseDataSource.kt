package com.exa.android.reflekt.loopit.data.remote.main.MapDataSource
import com.exa.android.reflekt.loopit.util.model.profileUser
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.google.firebase.firestore.DocumentSnapshot
import java.security.Timestamp
import timber.log.Timber


class FirebaseDataSource @Inject constructor() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("userLocations")
    private val geoFire = GeoFire(database)
    private val firestore = FirebaseFirestore.getInstance()

    fun saveUserLocation(userId: String, location: GeoLocation, onComplete: (String?, DatabaseError?) -> Unit) {
        geoFire.setLocation(userId, location, onComplete)
    }

    fun fetchUser(userId: String, onSuccess: (profileUser?) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("profile").document(userId).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    try {
                        val data = documentSnapshot.data
                        if (data != null) {
                            val profile = profileUser(
                                uid = data["uid"] as? String ?: "",
                                email = data["email"] as? String ?: "",
                                firstName = data["firstName"] as? String ?: "",
                                lastName = data["lastName"] as? String ?: "",
                                role = data["role"] as? String ?: "",
                                isStudent = data["isStudent"] as? Boolean ?: false,
                                createdAt = data["createdAt"] as? Timestamp,
                                collegeName = data["collegeName"] as? String,
                                year = (data["year"] as? Long)?.toInt(),
                                lat = (data["lat"] as? Number)?.toDouble() ?: 0.0,  // Safe conversion
                                lng = (data["lng"] as? Number)?.toDouble() ?: 0.0   // Safe conversion
                            )
                            Timber.tag("GeoFire").d("User profile fetched from Firestore: $profile")
                            onSuccess(profile)
                        } else {
                            onSuccess(null)
                        }
                    } catch (e: Exception) {
                        onFailure(e)
                    }
                } else {
                    onSuccess(null) // Document does not exist
                }
            }
            .addOnFailureListener { exception: Exception ->
                onFailure(exception)
            }
    }

    fun queryLocations(center: GeoLocation, radius: Double, listener: GeoQueryEventListener):GeoQuery {
        val query=geoFire.queryAtLocation(center, radius)
        query.addGeoQueryEventListener(listener)
        return query
    }
}