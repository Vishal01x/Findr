package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

/**
* Represents a role needed for a project
*/
data class Role(
    val id: String,
    val name: String, // e.g., "Frontend Developer"
    val technologies: List<String> = emptyList(), // e.g., ["Next.js", "React.js"]
    val count: Int = 1, // How many people needed for this role
    val description: String = ""
) {
    val displayName: String get() {
        return if (technologies.isEmpty()) {
            name
        } else {
            "$name (${technologies.joinToString(", ")})"
        }
    }
}