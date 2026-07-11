package com.exa.android.reflekt.library_ui.dialog

data class ModalDialogState(
    val title: String,
    val subtitle: String? = null,
    val iconResId: Int? = null,
    val primaryButtonText: String,
    val secondaryButtonText: String? = null,
    val showInputField: Boolean = false,
    val inputHint: String? = null,
    val inputText: String = "",
    val isPrimaryButtonEnabled: Boolean = true
)
