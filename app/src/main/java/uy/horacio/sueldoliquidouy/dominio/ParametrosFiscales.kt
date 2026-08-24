package uy.horacio.sueldoliquidouy.dominio

import kotlinx.serialization.Serializable

@Serializable
data class FranjaIrpf(
    /** Tope de la franja, expresado en BPC. null = última franja, sin tope. */
    val hastaEnBpc: Double?,
    /** Tasa en tanto por uno: 0.24 significa 24 %. */
    val tasa: Double
)

/** La escala publicada por BPS y DGI. Los topes van en BPC, como los define la ley. */
val ESCALA_IRPF_OFICIAL: List<FranjaIrpf> = listOf(
    FranjaIrpf(7.0, 0.00),
    FranjaIrpf(10.0, 0.10),
    FranjaIrpf(15.0, 0.15),
    FranjaIrpf(30.0, 0.24),
    FranjaIrpf(50.0, 0.25),
    FranjaIrpf(75.0, 0.27),
    FranjaIrpf(115.0, 0.31),
    FranjaIrpf(null, 0.36)
)

/**
 * Todos los valores fiscales que intervienen en el cálculo.
 *
 * Los que la ley expresa en BPC se guardan en BPC, no en pesos. Eso hace que
 * cambiar un solo número —la BPC— actualice de una vez las ocho franjas de
 * IRPF, el umbral de FONASA, el umbral de la tasa de deducción y las
 * deducciones por hijo.
 */
@Serializable
data class ParametrosFiscales(
    val ejercicio: Int = 2026,

    // --- Lo que cambia todos los años ---
    val bpc: Double = 6_864.0,
    val topeJubilatorio: Double = 288_836.0,

    // --- Lo que cambia solo con una reforma ---
    val tasaJubilatoria: Double = 0.15,
    val tasaFrl: Double = 0.001,

    val umbralFonasaEnBpc: Double = 2.5,
    val fonasaBajoSinConyuge: Double = 0.03,
    val fonasaBajoConConyuge: Double = 0.05,
    val fonasaAltoSolo: Double = 0.045,
    val fonasaAltoConHijos: Double = 0.06,
    val fonasaAltoConConyuge: Double = 0.065,
    val fonasaAltoConAmbos: Double = 0.08,

    val escalaIrpf: List<FranjaIrpf> = ESCALA_IRPF_OFICIAL,
    val umbralTasaDeduccionEnBpc: Double = 15.0,
    val tasaDeduccionAlta: Double = 0.14,
    val tasaDeduccionBaja: Double = 0.08,
    val deduccionHijoAnualEnBpc: Double = 20.0,
    val deduccionHijoDiscapacidadAnualEnBpc: Double = 40.0
) {
    // Valores derivados: no se guardan, se calculan a partir de la BPC.
    val umbralFonasa: Double get() = umbralFonasaEnBpc * bpc
    val umbralTasaDeduccion: Double get() = umbralTasaDeduccionEnBpc * bpc
    val deduccionHijo: Double get() = deduccionHijoAnualEnBpc * bpc / 12.0
    val deduccionHijoDiscapacidad: Double get() = deduccionHijoDiscapacidadAnualEnBpc * bpc / 12.0

    /** Tope de una franja convertido a pesos. La última franja no tiene tope. */
    fun topeEnPesos(franja: FranjaIrpf): Double =
        franja.hastaEnBpc?.times(bpc) ?: Double.MAX_VALUE

    /** true si el usuario tocó algo. Al ser data class, es una sola comparación. */
    val sonPersonalizados: Boolean get() = this != OFICIALES

    companion object {
        /** Los valores publicados por BPS y DGI. Se actualizan con cada versión de la app. */
        val OFICIALES = ParametrosFiscales()
    }
}

/** Devuelve la lista de problemas. Vacía significa que los parámetros son usables. */
fun ParametrosFiscales.errores(): List<String> {
    val fallas = mutableListOf<String>()

    fun exigirTasa(valor: Double, nombre: String) {
        if (valor.isNaN() || valor < 0.0 || valor > 1.0) {
            fallas += "$nombre tiene que estar entre 0 % y 100 %."
        }
    }

    if (bpc <= 0.0 || bpc.isNaN()) fallas += "La BPC tiene que ser mayor que cero."
    if (topeJubilatorio <= 0.0 || topeJubilatorio.isNaN()) {
        fallas += "El tope jubilatorio tiene que ser mayor que cero."
    }
    if (umbralFonasaEnBpc < 0.0) fallas += "El umbral de FONASA no puede ser negativo."
    if (umbralTasaDeduccionEnBpc < 0.0) {
        fallas += "El umbral de la tasa de deducción no puede ser negativo."
    }
    if (deduccionHijoAnualEnBpc < 0.0) fallas += "La deducción por hijo no puede ser negativa."
    if (deduccionHijoDiscapacidadAnualEnBpc < 0.0) {
        fallas += "La deducción por discapacidad no puede ser negativa."
    }

    exigirTasa(tasaJubilatoria, "El aporte jubilatorio")
    exigirTasa(tasaFrl, "El FRL")
    exigirTasa(tasaDeduccionAlta, "La tasa de deducción alta")
    exigirTasa(tasaDeduccionBaja, "La tasa de deducción baja")
    listOf(
        fonasaBajoSinConyuge to "FONASA (bajo el umbral, sin cónyuge)",
        fonasaBajoConConyuge to "FONASA (bajo el umbral, con cónyuge)",
        fonasaAltoSolo to "FONASA (sobre el umbral, sin cargas)",
        fonasaAltoConHijos to "FONASA (sobre el umbral, con hijos)",
        fonasaAltoConConyuge to "FONASA (sobre el umbral, con cónyuge)",
        fonasaAltoConAmbos to "FONASA (sobre el umbral, con ambos)"
    ).forEach { (valor, nombre) -> exigirTasa(valor, nombre) }

    if (escalaIrpf.isEmpty()) {
        fallas += "La escala de IRPF no puede quedar vacía."
    } else {
        if (escalaIrpf.last().hastaEnBpc != null) {
            fallas += "La última franja de IRPF no puede tener tope."
        }
        if (escalaIrpf.dropLast(1).any { it.hastaEnBpc == null }) {
            fallas += "Solo la última franja puede quedar sin tope."
        }
        val topes = escalaIrpf.dropLast(1).mapNotNull { it.hastaEnBpc }
        if (topes.any { it <= 0.0 }) {
            fallas += "Los topes de las franjas tienen que ser mayores que cero."
        }
        if (topes != topes.sorted() || topes.distinct().size != topes.size) {
            fallas += "Los topes de las franjas tienen que ir de menor a mayor, sin repetirse."
        }
        escalaIrpf.forEachIndexed { i, f -> exigirTasa(f.tasa, "La tasa de la franja ${i + 1}") }
    }

    // La que más veces salva: la combinación de tasas que da un líquido negativo.
    val aportesMaximos = tasaJubilatoria + fonasaAltoConAmbos + tasaFrl
    if (aportesMaximos >= 1.0) {
        fallas += "Los aportes suman más del 100 % del nominal: el líquido daría negativo."
    }

    return fallas
}
