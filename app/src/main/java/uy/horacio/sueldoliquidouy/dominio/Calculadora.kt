package uy.horacio.sueldoliquidouy.dominio

import kotlin.math.max
import kotlin.math.min

object Calculadora {

    /**
     * El valor por defecto de [p] no es comodidad: es lo que permite que los tests
     * originales sigan pasando sin tocarlos, y que cualquier llamada que no sepa
     * de parámetros personalizados use los oficiales.
     */
    fun calcular(
        e: Entrada,
        p: ParametrosFiscales = ParametrosFiscales.OFICIALES
    ): Resultado {
        val nominal = max(0.0, e.nominal)

        // 1. Aporte jubilatorio (con tope de cotización)
        val baseJubilatoria =
            if (e.aplicaTopeJubilatorio) min(nominal, p.topeJubilatorio) else nominal
        val jubilatorio = baseJubilatoria * p.tasaJubilatoria

        // 2. FONASA — sobre el nominal completo, sin tope
        val tieneHijos = (e.hijos + e.hijosConDiscapacidad) > 0
        val tasaFonasa = tasaFonasa(nominal, tieneHijos, e.conyugeACargo, p)
        val fonasa = nominal * tasaFonasa

        // 3. FRL
        val frl = nominal * p.tasaFrl

        // 4. IRPF
        val impuesto = impuestoPorFranjas(nominal, p)

        val deduccionHijos =
            (e.hijos * p.deduccionHijo +
                e.hijosConDiscapacidad * p.deduccionHijoDiscapacidad) * e.atribucionHijos

        val totalDeducciones =
            jubilatorio + fonasa + frl + deduccionHijos + e.fondoSolidaridad + e.cajaProfesional

        val tasaDeduccion =
            if (nominal <= p.umbralTasaDeduccion) p.tasaDeduccionAlta else p.tasaDeduccionBaja

        val credito = totalDeducciones * tasaDeduccion
        val irpf = max(0.0, impuesto - credito)

        // 5. Líquido
        val otros = e.fondoSolidaridad + e.cajaProfesional + e.otrosDescuentos
        val totalDescuentos = jubilatorio + fonasa + frl + irpf + otros

        return Resultado(
            nominal = nominal,
            jubilatorio = jubilatorio,
            fonasa = fonasa,
            tasaFonasa = tasaFonasa,
            frl = frl,
            impuestoPorFranjas = impuesto,
            totalDeducciones = totalDeducciones,
            tasaDeduccion = tasaDeduccion,
            creditoDeducciones = credito,
            irpf = irpf,
            otrosDescuentos = otros,
            totalDescuentos = totalDescuentos,
            liquido = nominal - totalDescuentos
        )
    }

    fun tasaFonasa(
        nominal: Double,
        tieneHijos: Boolean,
        conyugeACargo: Boolean,
        p: ParametrosFiscales = ParametrosFiscales.OFICIALES
    ): Double =
        if (nominal <= p.umbralFonasa) {
            if (conyugeACargo) p.fonasaBajoConConyuge else p.fonasaBajoSinConyuge
        } else {
            when {
                conyugeACargo && tieneHijos -> p.fonasaAltoConAmbos
                conyugeACargo -> p.fonasaAltoConConyuge
                tieneHijos -> p.fonasaAltoConHijos
                else -> p.fonasaAltoSolo
            }
        }

    /** Escala progresional: cada tramo tributa a su propia tasa. */
    fun impuestoPorFranjas(
        renta: Double,
        p: ParametrosFiscales = ParametrosFiscales.OFICIALES
    ): Double {
        var restante = renta
        var pisoAnterior = 0.0
        var total = 0.0
        for (franja in p.escalaIrpf) {
            if (restante <= 0.0) break
            val tope = p.topeEnPesos(franja)
            val gravado = min(restante, tope - pisoAnterior)
            total += gravado * franja.tasa
            restante -= gravado
            pisoAnterior = tope
        }
        return total
    }
}
