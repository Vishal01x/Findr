package com.exa.android.reflekt.library_ui.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FindrButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    icon: Painter? = null
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier.height(56.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            ButtonContent(text = text, icon = icon)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(56.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            ButtonContent(text = text, icon = icon)
        }
    }
}

@Composable
private fun ButtonContent(text: String, icon: Painter?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFindrButton() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FindrButton(
                text = "Primary Button",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            
            FindrButton(
                text = "Secondary Button",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false
            )

            FindrButton(
                text = "Disabled Primary",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }
    }
}
