package com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model

sealed class Package {
    data class Range(
        val min: Double,
        val max: Double
    ) : Package()

    data class Discrete(
        val packages: List<Double>
    ) : Package()

    fun getDisplayText(): String {
        return when (this) {
            is Range -> "₹${min}L - ₹${max}L"
            is Discrete -> packages.joinToString(", ") { "₹${it}L" }
        }
    }

    fun getMaxPackage(): Double {
        return when (this) {
            is Range -> max
            is Discrete -> packages.maxOrNull() ?: 0.0
        }
    }
}
