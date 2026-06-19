package io.shareit.transfer.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.shareit.transfer.security.SecretPinStore

private enum class ChangePinStep {
    Current,
    New,
    Confirm,
}

@Composable
fun ChangePinScreen(
    currentPin: String,
    onPinChanged: (String) -> Unit,
    onBack: () -> Unit,
) {
    var step by remember { mutableStateOf(ChangePinStep.Current) }
    var entered by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val title = when (step) {
        ChangePinStep.Current -> "Enter current passcode"
        ChangePinStep.New -> "Enter new passcode"
        ChangePinStep.Confirm -> "Confirm new passcode"
    }

    val subtitle = when {
        error && step == ChangePinStep.Current -> "Wrong passcode. Try again."
        error && step == ChangePinStep.Confirm -> "Passcodes do not match. Try again."
        step == ChangePinStep.Current -> "Verify your current passcode"
        step == ChangePinStep.New -> "Choose a new 6-digit passcode"
        step == ChangePinStep.Confirm -> "Re-enter the new passcode"
        else -> ""
    }

    fun resetEntry() {
        entered = ""
        error = false
    }

    fun submit() {
        when (step) {
            ChangePinStep.Current -> {
                if (entered == currentPin) {
                    step = ChangePinStep.New
                    resetEntry()
                } else {
                    error = true
                    entered = ""
                }
            }

            ChangePinStep.New -> {
                newPin = entered
                step = ChangePinStep.Confirm
                resetEntry()
            }

            ChangePinStep.Confirm -> {
                if (entered == newPin) {
                    onPinChanged(newPin)
                } else {
                    error = true
                    entered = ""
                    step = ChangePinStep.New
                    newPin = ""
                }
            }
        }
    }

    PinEntryScaffold(
        title = title,
        subtitle = subtitle,
        error = error,
        filledCount = entered.length,
        onBack = onBack,
        onDigit = { digit ->
            if (entered.length < SecretPinStore.PIN_LENGTH) {
                error = false
                entered += digit
                if (entered.length == SecretPinStore.PIN_LENGTH) submit()
            }
        },
        onBackspace = {
            if (entered.isNotEmpty()) {
                entered = entered.dropLast(1)
                error = false
            }
        },
    )
}
