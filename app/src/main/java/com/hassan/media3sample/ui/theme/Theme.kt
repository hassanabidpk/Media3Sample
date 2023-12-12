package com.hassan.media3sample.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Media3SampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val DarkColorPalette = darkColorScheme(
    primary = Purple200,
    secondary = Purple700,
    tertiary = Teal200,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
)

private val LightColorPalette = lightColorScheme(
    primary = Purple500,
    secondary = Purple700,
    tertiary = Teal200,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,


    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

@Composable
fun LLMSampleTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

private val JetpackLightColorPalette = JetsnackColors(
    brand = Shadow5,
    brandSecondary = Ocean3,
    uiBackground = Neutral0,
    uiBorder = Neutral4,
    uiFloated = FunctionalGrey,
    textSecondary = Neutral7,
    textHelp = Neutral6,
    textInteractive = Neutral0,
    textLink = Ocean11,
    iconSecondary = Neutral7,
    iconInteractive = Neutral0,
    iconInteractiveInactive = Neutral1,
    error = FunctionalRed,
    gradient6_1 = listOf(Shadow4, Ocean3, Shadow2, Ocean3, Shadow4),
    gradient6_2 = listOf(Rose4, Lavender3, Rose2, Lavender3, Rose4),
    gradient3_1 = listOf(Shadow2, Ocean3, Shadow4),
    gradient3_2 = listOf(Rose2, Lavender3, Rose4),
    gradient2_1 = listOf(Shadow4, Shadow11),
    gradient2_2 = listOf(Ocean3, Shadow3),
    gradient2_3 = listOf(Lavender3, Rose2),
    tornado1 = listOf(Shadow4, Ocean3),
    isDark = false
)

private val JetPackDarkColorPalette = JetsnackColors(
    brand = Shadow1,
    brandSecondary = Ocean2,
    uiBackground = Neutral8,
    uiBorder = Neutral3,
    uiFloated = FunctionalDarkGrey,
    textPrimary = Shadow1,
    textSecondary = Neutral0,
    textHelp = Neutral1,
    textInteractive = Neutral7,
    textLink = Ocean2,
    iconPrimary = Shadow1,
    iconSecondary = Neutral0,
    iconInteractive = Neutral7,
    iconInteractiveInactive = Neutral6,
    error = FunctionalRedDark,
    gradient6_1 = listOf(Shadow5, Ocean7, Shadow9, Ocean7, Shadow5),
    gradient6_2 = listOf(Rose11, Lavender7, Rose8, Lavender7, Rose11),
    gradient3_1 = listOf(Shadow9, Ocean7, Shadow5),
    gradient3_2 = listOf(Rose8, Lavender7, Rose11),
    gradient2_1 = listOf(Ocean3, Shadow3),
    gradient2_2 = listOf(Ocean4, Shadow2),
    gradient2_3 = listOf(Lavender3, Rose3),
    tornado1 = listOf(Shadow4, Ocean3),
    isDark = true
)

@Composable
fun JetsnackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) JetpackLightColorPalette else JetpackLightColorPalette

    ProvideJetsnackColors(colors) {
       androidx.compose.material.MaterialTheme(
           colors = debugColors(darkTheme),
            typography = JetSnackTypography,
            shapes = JetSnackShapes,
            content = content
        )
    }
}

object JetsnackTheme {
    val colors: JetsnackColors
        @Composable
        get() = LocalJetsnackColors.current
}

/**
 * Jetsnack custom Color Palette
 */
@Stable
class JetsnackColors(
    gradient6_1: List<Color>,
    gradient6_2: List<Color>,
    gradient3_1: List<Color>,
    gradient3_2: List<Color>,
    gradient2_1: List<Color>,
    gradient2_2: List<Color>,
    gradient2_3: List<Color>,
    brand: Color,
    brandSecondary: Color,
    uiBackground: Color,
    uiBorder: Color,
    uiFloated: Color,
    interactivePrimary: List<Color> = gradient2_1,
    interactiveSecondary: List<Color> = gradient2_2,
    interactiveMask: List<Color> = gradient6_1,
    textPrimary: Color = brand,
    textSecondary: Color,
    textHelp: Color,
    textInteractive: Color,
    textLink: Color,
    tornado1: List<Color>,
    iconPrimary: Color = brand,
    iconSecondary: Color,
    iconInteractive: Color,
    iconInteractiveInactive: Color,
    error: Color,
    notificationBadge: Color = error,
    isDark: Boolean
) {
    var gradient6_1 by mutableStateOf(gradient6_1)
        private set
    var gradient6_2 by mutableStateOf(gradient6_2)
        private set
    var gradient3_1 by mutableStateOf(gradient3_1)
        private set
    var gradient3_2 by mutableStateOf(gradient3_2)
        private set
    var gradient2_1 by mutableStateOf(gradient2_1)
        private set
    var gradient2_2 by mutableStateOf(gradient2_2)
        private set
    var gradient2_3 by mutableStateOf(gradient2_3)
        private set
    var brand by mutableStateOf(brand)
        private set
    var brandSecondary by mutableStateOf(brandSecondary)
        private set
    var uiBackground by mutableStateOf(uiBackground)
        private set
    var uiBorder by mutableStateOf(uiBorder)
        private set
    var uiFloated by mutableStateOf(uiFloated)
        private set
    var interactivePrimary by mutableStateOf(interactivePrimary)
        private set
    var interactiveSecondary by mutableStateOf(interactiveSecondary)
        private set
    var interactiveMask by mutableStateOf(interactiveMask)
        private set
    var textPrimary by mutableStateOf(textPrimary)
        private set
    var textSecondary by mutableStateOf(textSecondary)
        private set
    var textHelp by mutableStateOf(textHelp)
        private set
    var textInteractive by mutableStateOf(textInteractive)
        private set
    var tornado1 by mutableStateOf(tornado1)
        private set
    var textLink by mutableStateOf(textLink)
        private set
    var iconPrimary by mutableStateOf(iconPrimary)
        private set
    var iconSecondary by mutableStateOf(iconSecondary)
        private set
    var iconInteractive by mutableStateOf(iconInteractive)
        private set
    var iconInteractiveInactive by mutableStateOf(iconInteractiveInactive)
        private set
    var error by mutableStateOf(error)
        private set
    var notificationBadge by mutableStateOf(notificationBadge)
        private set
    var isDark by mutableStateOf(isDark)
        private set

    fun update(other: JetsnackColors) {
        gradient6_1 = other.gradient6_1
        gradient6_2 = other.gradient6_2
        gradient3_1 = other.gradient3_1
        gradient3_2 = other.gradient3_2
        gradient2_1 = other.gradient2_1
        gradient2_2 = other.gradient2_2
        gradient2_3 = other.gradient2_3
        brand = other.brand
        brandSecondary = other.brandSecondary
        uiBackground = other.uiBackground
        uiBorder = other.uiBorder
        uiFloated = other.uiFloated
        interactivePrimary = other.interactivePrimary
        interactiveSecondary = other.interactiveSecondary
        interactiveMask = other.interactiveMask
        textPrimary = other.textPrimary
        textSecondary = other.textSecondary
        textHelp = other.textHelp
        textInteractive = other.textInteractive
        textLink = other.textLink
        tornado1 = other.tornado1
        iconPrimary = other.iconPrimary
        iconSecondary = other.iconSecondary
        iconInteractive = other.iconInteractive
        iconInteractiveInactive = other.iconInteractiveInactive
        error = other.error
        notificationBadge = other.notificationBadge
        isDark = other.isDark
    }

    fun copy(): JetsnackColors = JetsnackColors(
        gradient6_1 = gradient6_1,
        gradient6_2 = gradient6_2,
        gradient3_1 = gradient3_1,
        gradient3_2 = gradient3_2,
        gradient2_1 = gradient2_1,
        gradient2_2 = gradient2_2,
        gradient2_3 = gradient2_3,
        brand = brand,
        brandSecondary = brandSecondary,
        uiBackground = uiBackground,
        uiBorder = uiBorder,
        uiFloated = uiFloated,
        interactivePrimary = interactivePrimary,
        interactiveSecondary = interactiveSecondary,
        interactiveMask = interactiveMask,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        textHelp = textHelp,
        textInteractive = textInteractive,
        textLink = textLink,
        tornado1 = tornado1,
        iconPrimary = iconPrimary,
        iconSecondary = iconSecondary,
        iconInteractive = iconInteractive,
        iconInteractiveInactive = iconInteractiveInactive,
        error = error,
        notificationBadge = notificationBadge,
        isDark = isDark,
    )
}

@Composable
fun ProvideJetsnackColors(
    colors: JetsnackColors,
    content: @Composable () -> Unit
) {
    val colorPalette = remember {
        // Explicitly creating a new object here so we don't mutate the initial [colors]
        // provided, and overwrite the values set in it.
        colors.copy()
    }
    colorPalette.update(colors)
    CompositionLocalProvider(LocalJetsnackColors provides colorPalette, content = content)
}

private val LocalJetsnackColors = staticCompositionLocalOf<JetsnackColors> {
    error("No JetsnackColorPalette provided")
}

/**
 * A Material [Colors] implementation which sets all colors to [debugColor] to discourage usage of
 * [MaterialTheme.colors] in preference to [JetsnackTheme.colors].
 */
fun debugColors(
    darkTheme: Boolean,
    debugColor: Color = Color.Magenta
) = Colors(
    primary = debugColor,
    primaryVariant = debugColor,
    secondary = debugColor,
    secondaryVariant = debugColor,
    background = debugColor,
    surface = debugColor,
    error = debugColor,
    onPrimary = debugColor,
    onSecondary = debugColor,
    onBackground = debugColor,
    onSurface = debugColor,
    onError = debugColor,
    isLight = !darkTheme
)