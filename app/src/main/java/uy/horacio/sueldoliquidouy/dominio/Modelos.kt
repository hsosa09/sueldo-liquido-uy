package uy.horacio.sueldoliquidouy.dominio

data class Entrada (

    val nominal: Double = 0.0,

    // Cónyuge o concubino sin cobertura propia
    val conyugeACargo: Boolean = false,
    val hijos: Int = 0,
    val hijosConDiscapacidad: Int = 0,

    // 1.0 = 100% deduccion, 0.5 = compartida entre padres
    val atribucionHijos: Double = 1.0,
    val fondoSolidaridad: Double = 0.0,
    val cajaProfesional: Double = 0.0,
    val otrosDescuentos: Double = 0.0,
    val aplicaTopeJubilatorio: Boolean = true
)

data class Resultado (
    val nominal: Double,
    val jubilatorio: Double,
    val fonasa: Double,
    val tasaFonasa: Double,
    val frl: Double,
    val impuestoPorFranjas: Double,
    val totalDeducciones: Double,
    val tasaDeduccion: Double,
    val creditoDeducciones: Double,
    val irpf: Double,
    val otrosDescuentos: Double,
    val totalDescuentos: Double,
    val liquido: Double
)