package com.exa.android.reflekt.loopit.presentation.main.placement.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.VerificationStatus

@Composable
fun VerificationBadge(
    status: VerificationStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, text) = when (status) {
        VerificationStatus.SAFE -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.15f),
            Color(0xFF2E7D32),
            "Verified"
        )
        VerificationStatus.NOT_VERIFIED -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.15f),
            Color(0xFFE65100),
            "Pending"
        )
        VerificationStatus.SUSPICIOUS -> Triple(
            Color(0xFFF44336).copy(alpha = 0.15f),
            Color(0xFFC62828),
            "Flagged"
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
