package com.exa.android.reflekt.loopit.presentation.main.profile.components.setting

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.exa.android.reflekt.BuildConfig
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    onContactSupport: () -> Unit,
    onFaqItemClick: (String) -> Unit
) {
    val faqItems = listOf(
        FAQ(
            "How do I enable or disable location access?",
            "To manage location access, go to Settings > App Permissions > Location. Enable GPS for more accurate location tracking and skill-based matching nearby."
        ),
        FAQ(
            "How do I turn on push notifications?",
            "You can enable push notifications in Settings > Notifications. Make sure your device’s system settings also allow notifications for this app."
        ),
        FAQ(
            "How do I share or request a project?",
            "Head to the Projects section to create a project with specific skill tags. You can invite others or accept/deny incoming requests to collaborate."
        ),
        FAQ(
            "Can I join a project someone else posted?",
            "Yes! You can browse available projects based on your skills and interests. Tap on a project to view details and choose to enroll if it's open for collaboration."
        ),
        FAQ(
            "How do I find users with specific skills near me?",
            "Use the Map screen to search for users by skill within a geofence radius around you. Tap on profiles to learn more or send them a message directly."
        ),
        FAQ("What devices are supported?", "Our app supports Android 8.0+ and iOS 12+ devices."),
        FAQ(
            "How do I delete my account?",
            "Account deletion can be done in Settings > Account > Delete Account. Note this action is irreversible."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Center", color = LiquidColors.TextPrimary, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LiquidColors.GlassSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = LiquidColors.TextPrimary
                        )
                    }
                }
            )
        },
        containerColor = LiquidColors.BgBase,
        contentColor = LiquidColors.TextPrimary
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "How can we help you?",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // FAQ Section
            item {
                Text(
                    text = "Frequently Asked Questions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = LiquidColors.Primary
                )
            }

            items(faqItems.size) { index ->
                val faq = faqItems[index]
                ExpandableFAQItem(faq = faq)
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Still need help?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = LiquidColors.Primary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LiquidColors.GlassSurface)
                        .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
                        .clickableWithNoIndication(onClick = onContactSupport)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = LiquidColors.Primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Contact Support",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LiquidColors.TextPrimary
                        )
                        Text(
                            text = "We typically respond within 24 hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidColors.TextSecondary
                        )
                    }
                }
            }

            // App Version
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "App Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
        }
    }
}


@Composable
fun ExpandableFAQItem(faq: FAQ) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LiquidColors.GlassSurface)
            .clickableWithNoIndication { expanded = !expanded }
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.bodyLarge,
                color = LiquidColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = LiquidColors.TextPrimary
            )
        }

        AnimatedVisibility(visible = expanded) {
            Text(
                text = faq.answer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
                color = LiquidColors.TextSecondary
            )
        }
    }
}


data class FAQ(val question: String, val answer: String)


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SimpleDateFormat")
@Composable
fun TermsPrivacyScreen(
    onBack: () -> Unit,
    onAcceptTerms: (Boolean) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var acceptedTerms by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Privacy", color = LiquidColors.TextPrimary, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LiquidColors.GlassSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = LiquidColors.TextPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (onAcceptTerms != {}) {
                Surface(
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onAcceptTerms(acceptedTerms) },
                            enabled = acceptedTerms
                        ) {
                            Text("Accept & Continue")
                        }
                    }
                }
            }
        },
        containerColor = LiquidColors.BgBase,
        contentColor = LiquidColors.TextPrimary
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "1. You must be at least 13 years old to use this app\n" +
                        "2. You are responsible for maintaining the confidentiality of your account\n" +
                        "3. You agree not to use the service for illegal activities\n" +
                        "4. We reserve the right to terminate accounts for violations of these terms",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "We collect the following information to provide and improve our service:\n\n" +
                        "- Account information (name, email, etc.)\n" +
                        "- Device information for analytics\n" +
                        "- Usage data to improve our services\n\n" +
                        "We do not sell your personal information to third parties. We may share data with:\n\n" +
                        "- Service providers who assist in operating our services\n" +
                        "- Law enforcement when required by law\n\n" +
                        "You have the right to:\n" +
                        "- Access your personal data\n" +
                        "- Request correction or deletion\n" +
                        "- Opt-out of marketing communications",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (onAcceptTerms != {}) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it }
                    )
                    Text(
                        text = "I agree to the Terms of Service and Privacy Policy",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Text(
                text = "Last Updated: ${SimpleDateFormat("MMMM d, yyyy").format(Date())}",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidColors.TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsPrivacyWebView(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Privacy", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    loadUrl("https://findr-privacy-policy-guide.lovable.app/privacy")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsWebView(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Privacy", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    loadUrl("https://findr-privacy-policy-guide.lovable.app/terms")
                }
            }
        )
    }
}
