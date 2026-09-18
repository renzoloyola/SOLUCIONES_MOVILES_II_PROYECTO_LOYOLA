package com.app.changescout.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BullionGold,
    onPrimary = MidnightLedger,
    primaryContainer = SurfaceHighlight,
    onPrimaryContainer = LedgerCream,
    secondary = FrontierCopper,
    onSecondary = MidnightLedger,
    secondaryContainer = CharcoalBlue,
    onSecondaryContainer = LedgerCream,
    tertiary = SignalGold,
    onTertiary = MidnightLedger,
    tertiaryContainer = WarningContainer,
    onTertiaryContainer = LedgerCream,
    error = SignalRed,
    onError = LedgerCream,
    errorContainer = ErrorSoft,
    onErrorContainer = LedgerCream,
    background = MidnightLedger,
    onBackground = LedgerCream,
    surface = SurfacePrimary,
    onSurface = LedgerCream,
    surfaceVariant = SurfaceSecondary,
    onSurfaceVariant = DesertFog,
    outline = OutlineSubtle,
    outlineVariant = DustLine
)

private val LightColorScheme = lightColorScheme(
    primary = OldBrass,
    onPrimary = LedgerCream,
    primaryContainer = ColorTokens.LightPrimaryContainer,
    onPrimaryContainer = MidnightLedger,
    secondary = FrontierCopper,
    onSecondary = MidnightLedger,
    secondaryContainer = ColorTokens.LightSecondaryContainer,
    onSecondaryContainer = MidnightLedger,
    tertiary = SignalGold,
    onTertiary = MidnightLedger,
    tertiaryContainer = WarningContainer,
    onTertiaryContainer = MidnightLedger,
    error = SignalRed,
    onError = LedgerCream,
    errorContainer = ErrorSoft,
    onErrorContainer = MidnightLedger,
    background = ColorTokens.LightBackground,
    onBackground = MidnightLedger,
    surface = ColorTokens.LightSurface,
    onSurface = MidnightLedger,
    surfaceVariant = ColorTokens.LightSurfaceVariant,
    onSurfaceVariant = SmokedSteel,
    outline = DustLine,
    outlineVariant = OutlineSubtle
)

@Composable
fun ChangeScoutTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}

private object ColorTokens {
    val LightBackground = LedgerCream
    val LightSurface = ColorTokensInternal.LightSurface
    val LightSurfaceVariant = ColorTokensInternal.LightSurfaceVariant
    val LightPrimaryContainer = ColorTokensInternal.LightPrimaryContainer
    val LightSecondaryContainer = ColorTokensInternal.LightSecondaryContainer
}

private object ColorTokensInternal {
    val LightSurface = LedgerCream.copy(alpha = 0.96f)
    val LightSurfaceVariant = DesertFog.copy(alpha = 0.35f)
    val LightPrimaryContainer = BullionGold.copy(alpha = 0.18f)
    val LightSecondaryContainer = FrontierCopper.copy(alpha = 0.18f)
}
