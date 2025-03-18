// ui/theme/ComponentStyles.kt
package com.exa.android.reflekt.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Button Styles (now composable)
object ButtonStyles {
    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = AppColors.CoralAccent,
        contentColor = Color.White
    )

    @Composable
    fun secondaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    )
}

// Text Field Styling (composable)
@Composable
fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

val TextFieldShape = RoundedCornerShape(12.dp)  // Can remain non-composable
val CardElevation = 4.dp                      // Constant value
val CardShape = RoundedCornerShape(12.dp)      // Constant value