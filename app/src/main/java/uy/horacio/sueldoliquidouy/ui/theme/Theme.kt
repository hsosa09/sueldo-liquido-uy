package uy.horacio.sueldoliquidouy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EsquemaOscuro = darkColorScheme(
    primary = VerdePrimarioOscuro,
    onPrimary = SobreVerdePrimarioOscuro,
    primaryContainer = VerdeContenedorOscuro,
    onPrimaryContainer = SobreVerdeContenedorOscuro,
    secondary = VerdeSecundarioOscuro,
    onSecondary = SobreVerdeSecundarioOscuro,
    secondaryContainer = VerdeSecundarioContenedorOscuro,
    onSecondaryContainer = SobreVerdeSecundarioContenedorOscuro,
    tertiary = AzulTerciarioOscuro,
    onTertiary = SobreAzulTerciarioOscuro,
    tertiaryContainer = AzulTerciarioContenedorOscuro,
    onTertiaryContainer = SobreAzulTerciarioContenedorOscuro,
    background = FondoOscuro,
    onBackground = SobreFondoOscuro,
    surface = FondoOscuro,
    onSurface = SobreFondoOscuro,
    surfaceVariant = SuperficieVarianteOscura,
    onSurfaceVariant = SobreSuperficieVarianteOscura,
    outline = BordeOscuro,
    outlineVariant = BordeVarianteOscuro,
    surfaceDim = SuperficieTenueOscura,
    surfaceBright = SuperficieBrillanteOscura,
    surfaceContainerLowest = ContenedorMinimoOscuro,
    surfaceContainerLow = ContenedorBajoOscuro,
    surfaceContainer = ContenedorOscuro,
    surfaceContainerHigh = ContenedorAltoOscuro,
    surfaceContainerHighest = ContenedorMaximoOscuro,
    inverseSurface = SuperficieInversaOscura,
    inverseOnSurface = SobreSuperficieInversaOscura,
    inversePrimary = VerdePrimario
)

private val EsquemaClaro = lightColorScheme(
    primary = VerdePrimario,
    onPrimary = SobreVerdePrimario,
    primaryContainer = VerdeContenedor,
    onPrimaryContainer = SobreVerdeContenedor,
    secondary = VerdeSecundario,
    onSecondary = SobreVerdeSecundario,
    secondaryContainer = VerdeSecundarioContenedor,
    onSecondaryContainer = SobreVerdeSecundarioContenedor,
    tertiary = AzulTerciario,
    onTertiary = SobreAzulTerciario,
    tertiaryContainer = AzulTerciarioContenedor,
    onTertiaryContainer = SobreAzulTerciarioContenedor,
    background = FondoClaro,
    onBackground = SobreFondoClaro,
    surface = FondoClaro,
    onSurface = SobreFondoClaro,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = SobreSuperficieVariante,
    outline = BordeClaro,
    outlineVariant = BordeVarianteClaro,
    surfaceDim = SuperficieTenueClara,
    surfaceBright = SuperficieBrillanteClara,
    surfaceContainerLowest = ContenedorMinimoClaro,
    surfaceContainerLow = ContenedorBajoClaro,
    surfaceContainer = ContenedorClaro,
    surfaceContainerHigh = ContenedorAltoClaro,
    surfaceContainerHighest = ContenedorMaximoClaro,
    inverseSurface = SuperficieInversaClara,
    inverseOnSurface = SobreSuperficieInversaClara,
    inversePrimary = VerdePrimarioOscuro
)

/**
 * @param colorDinamico deja que Android 12+ derive la paleta del fondo de
 *   pantalla (Material You). Viene apagado: con esto encendido, la app se ve
 *   distinta en cada teléfono, y las capturas de la ficha de Play no coinciden
 *   con el ícono ni entre sí. Encenderlo es cambiar este `false` por `true`.
 */
@Composable
fun SueldoLiquidoUYTheme(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    colorDinamico: Boolean = false,
    content: @Composable () -> Unit
) {
    val esquema = when {
        colorDinamico && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val contexto = LocalContext.current
            if (temaOscuro) dynamicDarkColorScheme(contexto) else dynamicLightColorScheme(contexto)
        }

        temaOscuro -> EsquemaOscuro
        else -> EsquemaClaro
    }

    MaterialTheme(
        colorScheme = esquema,
        typography = Typography,
        content = content
    )
}
