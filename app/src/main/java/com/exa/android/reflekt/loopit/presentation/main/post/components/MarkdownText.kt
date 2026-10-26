package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import dev.jeziellago.compose.markdowntext.MarkdownText as LibraryMarkdownText

/**
 * Drop-in replacement for Text that renders Markdown stored in post descriptions.
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    lineHeight: TextUnit = TextUnit.Unspecified,
    softWrap: Boolean = true
) {
    LibraryMarkdownText(
        markdown = text,
        modifier = modifier,
        style = style.copy(
            color = if (color != Color.Unspecified) color else style.color,
            lineHeight = lineHeight
        ),
        maxLines = maxLines
    )
}
