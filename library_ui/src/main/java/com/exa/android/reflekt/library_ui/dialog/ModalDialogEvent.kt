package com.exa.android.reflekt.library_ui.dialog

sealed interface ModalDialogEvent {
    object OnPrimaryButtonClicked : ModalDialogEvent
    object OnSecondaryButtonClicked : ModalDialogEvent
    object OnDismissRequest : ModalDialogEvent
    data class OnInputTextChanged(val text: String) : ModalDialogEvent
}
