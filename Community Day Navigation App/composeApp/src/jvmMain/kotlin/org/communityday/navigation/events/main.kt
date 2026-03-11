package org.communityday.navigation.events

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Community Day Navigation App",
    ) {
        App()
    }
}