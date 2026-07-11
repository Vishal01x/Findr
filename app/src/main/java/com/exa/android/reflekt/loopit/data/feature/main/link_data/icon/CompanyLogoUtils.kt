package com.exa.android.reflekt.loopit.data.feature.main.link_data.icon

object CompanyLogoUtils {

    private val STRIP_SUFFIXES = listOf(
        "private limited", "pvt. ltd.", "pvt.ltd.", "pvt ltd", "pvt. ltd",
        "limited", "ltd.", "ltd", "inc.", "inc", "corp.", "corp",
        "corporation", "llc", "llp", "l.l.c", "l.l.p",
        "technologies", "technology", "tech", "solutions", "services",
        "software", "systems", "group", "holdings", "ventures",
        "enterprises", "global", "india", "international"
    ).sortedByDescending { it.length }

    fun companyNameToDomain(name: String): String {
        var result = name.lowercase().trim()
        for (suffix in STRIP_SUFFIXES) {
            if (result.endsWith(", $suffix")) result = result.removeSuffix(", $suffix").trim()
            if (result.endsWith(" $suffix")) result = result.removeSuffix(" $suffix").trim()
        }
        result = result.replace(Regex("[^a-z0-9\\-]"), "").trim('-')
        return if (result.isBlank()) "example.com" else "$result.com"
    }

    // logo.clearbit.com's public anonymous endpoint was decommissioned (DNS no longer
    // resolves), so fall back to Google's favicon service - no API key needed, still up.
    fun faviconLogoUrl(domain: String): String =
        "https://www.google.com/s2/favicons?domain=$domain&sz=128"
}
