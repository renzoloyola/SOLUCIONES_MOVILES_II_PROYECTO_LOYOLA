package com.app.changescout.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.changescout.ui.navigation.NavegacionChangeScout
import com.app.changescout.ui.screens.auth.PantallaSesion
import com.app.changescout.ui.viewmodel.EventoSesion
import com.app.changescout.ui.viewmodel.ViewModelSesion

@Composable
fun AplicacionChangeScout(
    viewModelSesion: ViewModelSesion = hiltViewModel()
) {
    val state by viewModelSesion.uiState.collectAsStateWithLifecycle()

    if (state.estaAutenticado) {
        NavegacionChangeScout(
            onCerrarSesion = {
                viewModelSesion.onEvent(EventoSesion.CerrarSesion)
            }
        )
    } else {
        PantallaSesion(
            state = state,
            onEvent = viewModelSesion::onEvent
        )
    }
}
