package com.exa.android.reflekt.loopit.presentation.main.post.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

// Adaptive colour tokens — matches EventDetailScreen's EventColors system
private object EdColors {
    val CardBg: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF18272E) else Color(0xFFFFFFFF)
    val Surface: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF252B2E) else Color(0xFFE2E8F0)
    val Border: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF3D484F) else Color(0xFF94A3B8)
    val TextMain: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFDEE3E8) else Color(0xFF0F172A)
    val TextMuted: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFBCC8D0) else Color(0xFF475569)
    val WarningAmber: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFEAB308) else Color(0xFFD97706)
    val ErrorRed: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFFB4AB) else Color(0xFFDC2626)
    val CyanAccent = Color(0xFF13B6EC)
    val CyanPrimary = Color(0xFF71D2FF)
}

@Composable
fun EventDetailsCard(
    eventDetails: EventDetails,
    isRegistered: Boolean = false,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
            .clip(RoundedCornerShape(20.dp))
            .background(EdColors.CardBg)
            .border(1.dp, EdColors.Border.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            eventDetails.eventDate?.let { timestamp ->
                val date = timestamp.toDate()
                DateBadge(date = date)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    eventDetails.eventTime?.let { time ->
                        EventChip(icon = Icons.Default.Schedule, text = time, tint = EdColors.WarningAmber)
                    }
                    val loc = if (eventDetails.isVirtual) "Virtual"
                    else eventDetails.venue ?: eventDetails.location
                    loc?.let {
                        EventChip(
                            icon = if (eventDetails.isVirtual) Icons.Default.Videocam else Icons.Default.LocationOn,
                            text = it,
                            tint = EdColors.TextMuted
                        )
                    }
                }

                eventDetails.capacity?.let { capacity ->
                    val filled = eventDetails.registeredCount
                    val progress = (filled.toFloat() / capacity).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(9999.dp)),
                            color = if (eventDetails.isFull()) EdColors.ErrorRed else EdColors.CyanAccent,
                            trackColor = EdColors.Surface
                        )
                        Text(
                            text = if (eventDetails.isFull()) "Event Full"
                            else "${eventDetails.remainingCapacity()} spots left",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (eventDetails.isFull()) EdColors.ErrorRed else EdColors.TextMuted
                        )
                    }
                }

                if (eventDetails.isVirtual) {
                    eventDetails.meetingLink?.let { link ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EdColors.CyanAccent.copy(alpha = 0.1f))
                                .clickable {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                    )
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = EdColors.CyanAccent, modifier = Modifier.size(14.dp))
                            Text("Join Meeting", style = MaterialTheme.typography.labelSmall, color = EdColors.CyanAccent, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = EdColors.CyanAccent, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = EdColors.Border.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = Spacing.medium))

        Box(modifier = Modifier.padding(Spacing.medium)) {
            val buttonText = when {
                isRegistered                       -> "Already Registered"
                eventDetails.isFull()              -> "Event Full"
                !eventDetails.isRegistrationOpen() -> "Registration Closed"
                else                               -> "Register for Event"
            }
            val buttonEnabled = !isRegistered && eventDetails.isRegistrationOpen() && !eventDetails.isFull()

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = buttonEnabled,
                colors = if (buttonEnabled)
                    ButtonDefaults.buttonColors(containerColor = EdColors.CyanAccent)
                else
                    ButtonDefaults.buttonColors(
                        disabledContainerColor = EdColors.Surface,
                        disabledContentColor = EdColors.TextMuted
                    )
            ) {
                if (buttonEnabled) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF003547))
                    Spacer(Modifier.width(6.dp))
                    Text(buttonText, fontWeight = FontWeight.Bold, color = Color(0xFF003547))
                } else {
                    if (isRegistered) Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DateBadge(date: Date) {
    val locale = LocalConfiguration.current.locales[0]
    val day   = SimpleDateFormat("dd",  locale).format(date)
    val month = SimpleDateFormat("MMM", locale).format(date).uppercase(locale)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(EdColors.Surface)
            .border(1.dp, EdColors.Border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.labelSmall,
            color = EdColors.CyanAccent,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = day,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = EdColors.TextMain,
            lineHeight = 28.sp
        )
    }
}

@Composable
private fun EventChip(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .background(EdColors.Surface)
            .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(9999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = tint, fontWeight = FontWeight.SemiBold)
    }
}
