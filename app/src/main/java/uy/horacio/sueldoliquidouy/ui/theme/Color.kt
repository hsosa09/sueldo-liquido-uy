package uy.horacio.sueldoliquidouy.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de la app: verde azulado, el mismo del ícono.
 *
 * Reemplaza al morado que genera Android Studio en cada proyecto nuevo. El
 * motivo no es estético: las capturas de la ficha de Play tienen que verse como
 * el ícono que las acompaña, y el morado por defecto no se parece a nada de la
 * app.
 *
 * Los nombres dicen el rol, no el color, así que cambiar el verde por otro tono
 * algún día no obliga a renombrar nada.
 */

// --- Modo claro ---
val VerdePrimario = Color(0xFF0F766E)
val SobreVerdePrimario = Color(0xFFFFFFFF)
val VerdeContenedor = Color(0xFF9FF2E5)
val SobreVerdeContenedor = Color(0xFF00201C)

val VerdeSecundario = Color(0xFF4A635E)
val SobreVerdeSecundario = Color(0xFFFFFFFF)
val VerdeSecundarioContenedor = Color(0xFFCCE8E2)
val SobreVerdeSecundarioContenedor = Color(0xFF06201B)

val AzulTerciario = Color(0xFF45617B)
val SobreAzulTerciario = Color(0xFFFFFFFF)
val AzulTerciarioContenedor = Color(0xFFCBE6FF)
val SobreAzulTerciarioContenedor = Color(0xFF001E31)

val FondoClaro = Color(0xFFF5FBF8)
val SobreFondoClaro = Color(0xFF191C1B)
val SuperficieVariante = Color(0xFFDAE5E1)
val SobreSuperficieVariante = Color(0xFF3F4946)
val BordeClaro = Color(0xFF6F7976)

// --- Modo oscuro ---
val VerdePrimarioOscuro = Color(0xFF6ED8C8)
val SobreVerdePrimarioOscuro = Color(0xFF00382F)
val VerdeContenedorOscuro = Color(0xFF005046)
val SobreVerdeContenedorOscuro = Color(0xFF9FF2E5)

val VerdeSecundarioOscuro = Color(0xFFB1CCC6)
val SobreVerdeSecundarioOscuro = Color(0xFF1C3531)
val VerdeSecundarioContenedorOscuro = Color(0xFF334B47)
val SobreVerdeSecundarioContenedorOscuro = Color(0xFFCCE8E2)

val AzulTerciarioOscuro = Color(0xFFAECBE8)
val SobreAzulTerciarioOscuro = Color(0xFF16344B)
val AzulTerciarioContenedorOscuro = Color(0xFF2D4962)
val SobreAzulTerciarioContenedorOscuro = Color(0xFFCBE6FF)

val FondoOscuro = Color(0xFF191C1B)
val SobreFondoOscuro = Color(0xFFE0E3E1)
val SuperficieVarianteOscura = Color(0xFF3F4946)
val SobreSuperficieVarianteOscura = Color(0xFFBEC9C5)
val BordeOscuro = Color(0xFF899390)

// --- Superficies ---
//
// Material 3 tiene una escala de superficies (`surfaceContainer*`) que usan las
// tarjetas, los menús y el fondo de los switches. Si no se declaran, Compose
// las toma de su paleta base, que es morada: alcanza para que un switch apagado
// se vea lila arriba de un fondo verde. Hay que definirlas todas.

val SuperficieTenueClara = Color(0xFFD6DBD8)
val SuperficieBrillanteClara = Color(0xFFF5FBF8)
val ContenedorMinimoClaro = Color(0xFFFFFFFF)
val ContenedorBajoClaro = Color(0xFFEFF5F2)
val ContenedorClaro = Color(0xFFE9EFEC)
val ContenedorAltoClaro = Color(0xFFE4EAE7)
val ContenedorMaximoClaro = Color(0xFFDEE4E1)
val BordeVarianteClaro = Color(0xFFBEC9C5)
val SuperficieInversaClara = Color(0xFF2E3130)
val SobreSuperficieInversaClara = Color(0xFFEFF1EF)

val SuperficieTenueOscura = Color(0xFF191C1B)
val SuperficieBrillanteOscura = Color(0xFF363A38)
val ContenedorMinimoOscuro = Color(0xFF0C0F0E)
val ContenedorBajoOscuro = Color(0xFF191C1B)
val ContenedorOscuro = Color(0xFF1D2120)
val ContenedorAltoOscuro = Color(0xFF282B2A)
val ContenedorMaximoOscuro = Color(0xFF333635)
val BordeVarianteOscuro = Color(0xFF3F4946)
val SuperficieInversaOscura = Color(0xFFE0E3E1)
val SobreSuperficieInversaOscura = Color(0xFF2E3130)
