package uy.horacio.sueldoliquidouy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uy.horacio.sueldoliquidouy.datos.Ajustes
import uy.horacio.sueldoliquidouy.datos.RepositorioAjustes
import uy.horacio.sueldoliquidouy.dominio.FranjaIrpf
import uy.horacio.sueldoliquidouy.dominio.ParametrosFiscales
import uy.horacio.sueldoliquidouy.dominio.errores
import java.util.Locale

/** Describe un parámetro editable: cómo se muestra, cómo se lee y cómo se escribe. */
private data class Campo(
    val clave: String,
    val etiqueta: String,
    val ayuda: String,
    val esPorcentaje: Boolean,
    val leer: (ParametrosFiscales) -> Double,
    val aplicar: (ParametrosFiscales, Double) -> ParametrosFiscales
)

private val CAMPOS_BASICOS = listOf(
    Campo(
        "bpc", "BPC del ejercicio",
        "Base de Prestaciones y Contribuciones. Se fija por decreto cada 1.º de enero. " +
            "Al cambiarla se recalculan todas las franjas.",
        false, { it.bpc }, { p, v -> p.copy(bpc = v) }
    ),
    Campo(
        "tope", "Tope jubilatorio mensual",
        "Por encima de este monto no se realizan aportes jubilatorios obligatorios.",
        false, { it.topeJubilatorio }, { p, v -> p.copy(topeJubilatorio = v) }
    )
)

private val CAMPOS_AVANZADOS = listOf(
    Campo("jub", "Aporte jubilatorio", "Tasa personal sobre el nominal.", true,
        { it.tasaJubilatoria }, { p, v -> p.copy(tasaJubilatoria = v) }),
    Campo("frl", "FRL", "Fondo de Reconversión Laboral.", true,
        { it.tasaFrl }, { p, v -> p.copy(tasaFrl = v) }),
    Campo("umbFonasa", "Umbral de FONASA (en BPC)", "Separa las dos tablas de tasas.", false,
        { it.umbralFonasaEnBpc }, { p, v -> p.copy(umbralFonasaEnBpc = v) }),
    Campo("fbSin", "FONASA · bajo el umbral, sin cónyuge", "", true,
        { it.fonasaBajoSinConyuge }, { p, v -> p.copy(fonasaBajoSinConyuge = v) }),
    Campo("fbCon", "FONASA · bajo el umbral, con cónyuge", "", true,
        { it.fonasaBajoConConyuge }, { p, v -> p.copy(fonasaBajoConConyuge = v) }),
    Campo("faSolo", "FONASA · sobre el umbral, sin cargas", "", true,
        { it.fonasaAltoSolo }, { p, v -> p.copy(fonasaAltoSolo = v) }),
    Campo("faHijos", "FONASA · sobre el umbral, con hijos", "", true,
        { it.fonasaAltoConHijos }, { p, v -> p.copy(fonasaAltoConHijos = v) }),
    Campo("faCony", "FONASA · sobre el umbral, con cónyuge", "", true,
        { it.fonasaAltoConConyuge }, { p, v -> p.copy(fonasaAltoConConyuge = v) }),
    Campo("faAmbos", "FONASA · sobre el umbral, con ambos", "", true,
        { it.fonasaAltoConAmbos }, { p, v -> p.copy(fonasaAltoConAmbos = v) }),
    Campo("umbDed", "Umbral de la tasa de deducción (en BPC)", "Por encima, la tasa baja.", false,
        { it.umbralTasaDeduccionEnBpc }, { p, v -> p.copy(umbralTasaDeduccionEnBpc = v) }),
    Campo("dedAlta", "Tasa de deducción alta", "", true,
        { it.tasaDeduccionAlta }, { p, v -> p.copy(tasaDeduccionAlta = v) }),
    Campo("dedBaja", "Tasa de deducción baja", "", true,
        { it.tasaDeduccionBaja }, { p, v -> p.copy(tasaDeduccionBaja = v) }),
    Campo("hijo", "Deducción por hijo (BPC anuales)", "", false,
        { it.deduccionHijoAnualEnBpc }, { p, v -> p.copy(deduccionHijoAnualEnBpc = v) }),
    Campo("hijoDisc", "Deducción por hijo con discapacidad (BPC anuales)", "", false,
        { it.deduccionHijoDiscapacidadAnualEnBpc },
        { p, v -> p.copy(deduccionHijoDiscapacidadAnualEnBpc = v) })
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaParametros(
    repositorio: RepositorioAjustes,
    ajustes: Ajustes,
    onVolver: () -> Unit
) {
    val alcance = rememberCoroutineScope()
    val actuales = ajustes.parametros

    var avanzadosVisibles by remember { mutableStateOf(false) }
    var problemas by remember { mutableStateOf(emptyList<String>()) }
    var guardado by remember { mutableStateOf(false) }

    // Texto en edición, uno por campo. Se siembra de nuevo si cambian los parámetros
    // guardados (por ejemplo, al restaurar los oficiales).
    val textos = remember(actuales) {
        mutableStateMapOf<String, String>().apply {
            (CAMPOS_BASICOS + CAMPOS_AVANZADOS).forEach { campo ->
                val valor = campo.leer(actuales)
                put(campo.clave, aTexto(if (campo.esPorcentaje) valor * 100 else valor))
            }
        }
    }
    val topes = remember(actuales) {
        mutableStateListOf<String>().apply {
            addAll(actuales.escalaIrpf.map { it.hastaEnBpc?.let(::aTexto) ?: "" })
        }
    }
    val tasas = remember(actuales) {
        mutableStateListOf<String>().apply {
            addAll(actuales.escalaIrpf.map { aTexto(it.tasa * 100) })
        }
    }

    fun construir(): ParametrosFiscales? {
        var p = actuales
        for (campo in CAMPOS_BASICOS + CAMPOS_AVANZADOS) {
            val valor = textos[campo.clave].aDecimal() ?: return null
            p = campo.aplicar(p, if (campo.esPorcentaje) valor / 100.0 else valor)
        }
        val escala = mutableListOf<FranjaIrpf>()
        for (i in tasas.indices) {
            val tasa = tasas[i].aDecimal() ?: return null
            val tope = if (i == tasas.lastIndex) null else (topes[i].aDecimal() ?: return null)
            escala += FranjaIrpf(hastaEnBpc = tope, tasa = tasa / 100.0)
        }
        return p.copy(escalaIrpf = escala)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Valores y tasas") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            Text(
                "Estos son los valores con los que la app calcula. Vienen cargados con los " +
                    "publicados por BPS y DGI para el ejercicio ${actuales.ejercicio}. " +
                    "Modificalos solo si sabés lo que estás haciendo: por ejemplo, si ya salió " +
                    "la BPC del año nuevo y la app todavía no se actualizó, o si querés simular " +
                    "un escenario distinto.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(20.dp))

            Text("Lo que cambia todos los años", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            CAMPOS_BASICOS.forEach { campo ->
                CampoParametro(campo, textos[campo.clave].orEmpty()) { textos[campo.clave] = it }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(onClick = { avanzadosVisibles = !avanzadosVisibles }) {
                Text(
                    if (avanzadosVisibles) "Ocultar valores avanzados"
                    else "Mostrar valores avanzados"
                )
            }

            if (avanzadosVisibles) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Estas tasas solo cambian con una reforma tributaria. Si las tocás sin " +
                            "una fuente oficial delante, el resultado va a estar mal.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                CAMPOS_AVANZADOS.forEach { campo ->
                    CampoParametro(campo, textos[campo.clave].orEmpty()) {
                        textos[campo.clave] = it
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Escala de IRPF", fontWeight = FontWeight.Bold)
                Text(
                    "Los topes van en BPC. La última franja no lleva tope: es todo lo que " +
                        "supera al anterior.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))

                tasas.indices.forEach { i ->
                    Row(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (i == tasas.lastIndex) "sin tope" else topes[i],
                            onValueChange = {
                                if (i != tasas.lastIndex) topes[i] = it.filtrarDecimal()
                            },
                            enabled = i != tasas.lastIndex,
                            label = { Text("Hasta (BPC)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        )
                        OutlinedTextField(
                            value = tasas[i],
                            onValueChange = { tasas[i] = it.filtrarDecimal() },
                            label = { Text("Tasa %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            if (problemas.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("No se pudo guardar:", fontWeight = FontWeight.Bold)
                        problemas.forEach {
                            Text("• $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (guardado) {
                Text("Valores guardados.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
            }

            // Guardado explícito: una tasa a medio escribir contaminaría todos los
            // cálculos siguientes, incluso después de cerrar la app.
            Button(
                onClick = {
                    guardado = false
                    val nuevos = construir()
                    if (nuevos == null) {
                        problemas = listOf("Hay campos vacíos o mal escritos.")
                        return@Button
                    }
                    val fallas = nuevos.errores()
                    problemas = fallas
                    if (fallas.isEmpty()) {
                        alcance.launch { repositorio.guardarParametros(nuevos) }
                        guardado = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar valores")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    alcance.launch { repositorio.restaurarParametrosOficiales() }
                    problemas = emptyList()
                    guardado = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restaurar valores oficiales ${ParametrosFiscales.OFICIALES.ejercicio}")
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Fuentes oficiales: los comunicados del BPS con valores y escalas de IRPF del " +
                    "ejercicio, y la página de tasas de FONASA.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CampoParametro(campo: Campo, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = { onChange(it.filtrarDecimal()) },
        label = { Text(campo.etiqueta) },
        supportingText = if (campo.ayuda.isBlank()) null else {
            { Text(campo.ayuda, style = MaterialTheme.typography.bodySmall) }
        },
        suffix = if (campo.esPorcentaje) {
            { Text("%") }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

/** Deja pasar dígitos y una sola coma decimal. */
private fun String.filtrarDecimal(): String {
    val limpio = filter { it.isDigit() || it == ',' || it == '.' }.replace('.', ',')
    val partes = limpio.split(',')
    return if (partes.size <= 1) limpio else partes[0] + "," + partes.drop(1).joinToString("")
}

private fun String?.aDecimal(): Double? =
    this?.replace(',', '.')?.toDoubleOrNull()

private fun aTexto(valor: Double): String =
    if (valor % 1.0 == 0.0) valor.toLong().toString()
    else String.format(Locale.US, "%.4f", valor).trimEnd('0').trimEnd('.').replace('.', ',')
