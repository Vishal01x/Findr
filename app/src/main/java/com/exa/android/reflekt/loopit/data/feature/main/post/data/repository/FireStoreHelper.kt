package com.exa.android.reflekt.loopit.data.feature.main.post.data.repository

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUtils {

    private val db = FirebaseFirestore.getInstance()

    fun generateDocumentId(parentCollection : String, childCollection : String): String {
        return db.collection(parentCollection)
            .document()
            .collection(childCollection)
            .document()
            .id
    }
}
