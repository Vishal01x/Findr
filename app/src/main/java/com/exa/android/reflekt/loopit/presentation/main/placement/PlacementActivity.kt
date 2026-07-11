package com.exa.android.reflekt.loopit.presentation.main.placement

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.meeting.room.compose.ui.AppTheme

@AndroidEntryPoint
class PlacementActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_COMPANY_ID  = "COMPANY_ID"
        const val EXTRA_SCREEN_TYPE = "SCREEN_TYPE"
        const val SCREEN_DETAIL     = "DETAIL"    // CompanyDetailScreen (tabs)
        const val SCREEN_INSIGHTS   = "INSIGHTS"  // DetailedCompanyInsightsScreen (scrollable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val companyId  = intent.getStringExtra(EXTRA_COMPANY_ID)  ?: "google"
        val screenType = intent.getStringExtra(EXTRA_SCREEN_TYPE) ?: SCREEN_INSIGHTS

        setContent {
            AppTheme(darkTheme = true) {
                when (screenType) {
                    SCREEN_INSIGHTS -> {
                        DetailedCompanyInsightsScreen(
                            companyId = companyId,
                            onBack = { finish() }
                        )
                    }
                    else -> {
                        CompanyDetailScreen(
                            companyId = companyId,
                            onNavigateBack = { finish() }
                        )
                    }
                }
            }
        }
    }
}