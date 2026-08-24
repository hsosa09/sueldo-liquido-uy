package uy.horacio.sueldoliquidouy.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadoraTest {

    private val delta = 0.01

    @Test
    fun `sueldo bajo, por debajo del umbral FONASA`() {
        val r = Calculadora.calcular(Entrada(nominal = 15_000.0))
        assertEquals(2_250.0, r.jubilatorio, delta)
        assertEquals(0.03, r.tasaFonasa, delta)   // 15.000 <= 17.160
        assertEquals(450.0, r.fonasa, delta)
        assertEquals(15.0, r.frl, delta)
        assertEquals(0.0, r.irpf, delta)          // por debajo del mínimo no imponible
        assertEquals(12_285.0, r.liquido, delta)
    }

    @Test
    fun `sueldo medio sin cargas, el credito anula el IRPF`() {
        val r = Calculadora.calcular(Entrada(nominal = 50_000.0))
        assertEquals(7_500.0, r.jubilatorio, delta)
        assertEquals(0.045, r.tasaFonasa, delta)
        assertEquals(2_250.0, r.fonasa, delta)
        assertEquals(50.0, r.frl, delta)
        // impuesto por franjas = 195,20 ; crédito = 9.800 x 14 % = 1.372 -> IRPF = 0
        assertEquals(0.0, r.irpf, delta)
        assertEquals(40_200.0, r.liquido, delta)
    }

    @Test
    fun `sueldo alto sin cargas, tasa de deduccion del 8 por ciento`() {
        val r = Calculadora.calcular(Entrada(nominal = 120_000.0))
        assertEquals(18_000.0, r.jubilatorio, delta)
        assertEquals(5_400.0, r.fonasa, delta)
        assertEquals(120.0, r.frl, delta)
        assertEquals(11_296.80, r.impuestoPorFranjas, delta)
        assertEquals(0.08, r.tasaDeduccion, delta)
        assertEquals(1_881.60, r.creditoDeducciones, delta)
        assertEquals(9_415.20, r.irpf, delta)
        assertEquals(87_064.80, r.liquido, delta)
    }

    @Test
    fun `tope de aportacion jubilatoria`() {
        val r = Calculadora.calcular(Entrada(nominal = 350_000.0))
        assertEquals(288_836.0 * 0.15, r.jubilatorio, delta)
        // FONASA y FRL sí van sobre el nominal completo
        assertEquals(350_000.0 * 0.045, r.fonasa, delta)
        assertEquals(350.0, r.frl, delta)
    }

    @Test
    fun `tasas FONASA por situacion familiar`() {
        val base = Entrada(nominal = 60_000.0)
        assertEquals(0.045, Calculadora.calcular(base).tasaFonasa, delta)
        assertEquals(0.06, Calculadora.calcular(base.copy(hijos = 1)).tasaFonasa, delta)
        assertEquals(0.065, Calculadora.calcular(base.copy(conyugeACargo = true)).tasaFonasa, delta)
        assertEquals(0.08, Calculadora.calcular(base.copy(conyugeACargo = true, hijos = 2)).tasaFonasa, delta)
    }

    @Test
    fun `atribucion de hijos al 50 por ciento`() {
        val entera = Calculadora.calcular(Entrada(nominal = 90_000.0, hijos = 2))
        val mitad = Calculadora.calcular(Entrada(nominal = 90_000.0, hijos = 2, atribucionHijos = 0.5))
        assertEquals(
            entera.totalDeducciones - mitad.totalDeducciones,
            ParametrosFiscales.OFICIALES.deduccionHijo * 2 * 0.5,
            delta
        )
    }

    @Test
    fun `nominal cero no rompe nada`() {
        val r = Calculadora.calcular(Entrada(nominal = 0.0))
        assertEquals(0.0, r.liquido, delta)
        assertEquals(0.0, r.irpf, delta)
    }

    // ---------- Parámetros editables ----------

    @Test
    fun `los parametros oficiales son validos`() {
        assertTrue(ParametrosFiscales.OFICIALES.errores().isEmpty())
    }

    @Test
    fun `subir la BPC arrastra todas las franjas`() {
        val subida = ParametrosFiscales.OFICIALES.copy(bpc = 7_500.0)
        // El mínimo no imponible pasa de 7 x 6.864 = 48.048 a 7 x 7.500 = 52.500
        assertEquals(0.0, Calculadora.impuestoPorFranjas(52_000.0, subida), delta)
        assertTrue(Calculadora.impuestoPorFranjas(52_000.0) > 0.0)
    }

    @Test
    fun `parametros a medida cambian el resultado`() {
        val entrada = Entrada(nominal = 120_000.0)
        val oficial = Calculadora.calcular(entrada)
        val sinJubilatorio = Calculadora.calcular(
            entrada,
            ParametrosFiscales.OFICIALES.copy(tasaJubilatoria = 0.0)
        )
        assertEquals(0.0, sinJubilatorio.jubilatorio, delta)
        assertTrue(sinJubilatorio.liquido > oficial.liquido)
    }

    @Test
    fun `la validacion detecta franjas desordenadas`() {
        val rotos = ParametrosFiscales.OFICIALES.copy(
            escalaIrpf = listOf(
                FranjaIrpf(30.0, 0.10),
                FranjaIrpf(10.0, 0.15),
                FranjaIrpf(null, 0.36)
            )
        )
        assertTrue(rotos.errores().isNotEmpty())
    }

    @Test
    fun `la validacion detecta tasas imposibles`() {
        val rotos = ParametrosFiscales.OFICIALES.copy(tasaJubilatoria = 1.5)
        assertTrue(rotos.errores().isNotEmpty())
    }

    @Test
    fun `la validacion detecta liquido negativo`() {
        val rotos = ParametrosFiscales.OFICIALES.copy(
            tasaJubilatoria = 0.60, fonasaAltoConAmbos = 0.45
        )
        assertTrue(rotos.errores().any { it.contains("100 %") })
    }

    @Test
    fun `sonPersonalizados detecta cualquier cambio`() {
        assertTrue(!ParametrosFiscales.OFICIALES.sonPersonalizados)
        assertTrue(ParametrosFiscales.OFICIALES.copy(bpc = 7_000.0).sonPersonalizados)
    }
}
