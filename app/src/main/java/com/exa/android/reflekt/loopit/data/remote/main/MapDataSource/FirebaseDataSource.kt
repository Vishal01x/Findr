package com.exa.android.reflekt.loopit.data.remote.main.MapDataSource
import com.exa.android.reflekt.loopit.data.remote.main.Repository.UserRepository.ProfileDataWrapper
import com.exa.android.reflekt.loopit.util.model.Profile.ProfileData
import com.exa.android.reflekt.loopit.util.model.profileUser
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.Timestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.google.firebase.firestore.DocumentSnapshot
import timber.log.Timber


class FirebaseDataSource @Inject constructor() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("userLocations")
    private val geoFire = GeoFire(database)
    private val firestore = FirebaseFirestore.getInstance()

    fun saveUserLocation(userId: String, location: GeoLocation, onComplete: (String?, DatabaseError?) -> Unit) {
        Timber.tag("GeoFire").d("Saving location for user $userId: $location")
        geoFire.setLocation(userId, location, onComplete)
    }

    fun fetchUser(userId: String, onSuccess: (profileUser?) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    try {
                        val profileWrapper = documentSnapshot?.toObject(ProfileDataWrapper::class.java)
                        val profileData = profileWrapper?.profileData ?: ProfileData() // Return empty ProfileData if missing
                        if (profileData != null) {
                            val data = profileData.profileHeader

                            val profile = profileUser(
                                uid = data.uid,
                                email = data.email,
                                name = data.name,
                                imageUrl = data.profileImageUrl,
                                role = data.role,
                                isStudent = data.isStudent,
                                createdAt = data.createdAt,
                                collegeName = data.collegeName,
                                year = data.year,
                                lat = data.lat,  // Safe conversion
                                lng = data.lng  // Safe conversion
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

    fun listenToUserLocation(userId: String, onLocationUpdate: (GeoLocation) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    // GeoFire stores locations in a specific format with g (geohash), l (latitude/longitude)
                    val locationData = snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                    locationData?.let { data ->
                        val latLng = data["l"] as? List<*>
                        latLng?.let {
                            val latitude = (it[0] as? Double) ?: 0.0
                            val longitude = (it[1] as? Double) ?: 0.0
                            onLocationUpdate(GeoLocation(latitude, longitude))
                        }
                    }
                } catch (e: Exception) {
                    Timber.tag("GeoFire").e(e, "Error parsing location data")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.tag("GeoFire").e("Location listener cancelled: ${error.message}")
            }
        }
        database.child(userId).addValueEventListener(listener)
        return listener
    }

    fun removeLocationListener(userId: String, listener: ValueEventListener) {
        database.child(userId).removeEventListener(listener)
    }

}