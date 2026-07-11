package com.exa.android.reflekt.loopit.presentation.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.abbreviateStream
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
fun AcademicChipsRow(
    branch: String?,
    section: String?,
    year: String?,
    modifier: Modifier = Modifier
) {
    val chips = listOfNotNull(
        branch?.ifBlank { null }?.let { abbreviateStream(it) },
        section?.ifBlank { null },
        year?.ifBlank { null }
    )

    if (chips.isNotEmpty()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier
        ) {
            chips.forEach { chip ->
                CardAcademicChip(chip)
            }
        }
    }
}

@Composable
fun CardAcademicChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(LiquidColors.Primary.copy(alpha = 0.10f))
            .border(1.dp, LiquidColors.Primary.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = LiquidColors.Primary,
            fontWeight = FontWeight.Medium
        )
    }
}
