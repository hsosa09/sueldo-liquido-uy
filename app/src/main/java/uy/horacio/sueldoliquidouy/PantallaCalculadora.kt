package uy.horacio.sueldoliquidouy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import uy.horacio.sueldoliquidouy.anuncios.BannerPublicitario
import uy.horacio.sueldoliquidouy.datos.Ajustes
import uy.horacio.sueldoliquidouy.datos.RepositorioAjustes
import uy.horacio.sueldoliquidouy.dominio.Calculadora
import uy.horacio.sueldoliquidouy.dominio.Entrada
import uy.horacio.sueldoliquidouy.dominio.ParametrosFiscales
import uy.horacio.sueldoliquidouy.dominio.Resultado

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaCalculadora(
    repositorio: RepositorioAjustes,
    ajustes: Ajustes,
    onAbrirConfiguracion: () -> Unit,
    onAbrirParametros: () -> Unit
) {
    // rememberSaveable arranca con el valor guardado y no se pisa mientras el usuario escribe.
    var nominal by rememberSaveable { mutableStateOf(ajustes.nominal) }
    var conyuge by rememberSaveable { mutableStateOf(ajustes.conyugeACargo) }
    var hijos by rememberSaveable { mutableStateOf(ajustes.hijos) }
    var hijosDisc by rememberSaveable { mutableStateOf(ajustes.hijosConDiscapacidad) }
    var mitad by rememberSaveable { mutableStateOf(ajustes.atribucionMitad) }
    var fondo by rememberSaveable { mutableStateOf(ajustes.fondoSolidaridad) }
    var caja by rememberSaveable { mutableStateOf(ajustes.cajaProfesional) }
    var otros by rememberSaveable { mutableStateOf(ajustes.otrosDescuentos) }

    // Autoguardado con freno: LaunchedEffect se cancela y reinicia con cada tecla,
    // así que el delay solo se cumple cuando el usuario para de escribir 600 ms.
    LaunchedEffect(nominal, conyuge, hijos, hijosDisc, mitad, fondo, caja, otros, ajustes.recordarDatos) {
        if (!ajustes.recordarDatos) return@LaunchedEffect
        delay(600)
        repositorio.guardarDatos(
            ajustes.copy(
                nominal = nominal,
                conyugeACargo = conyuge,
                hijos = hijos,
                hijosConDiscapacidad = hijosDisc,
                atribucionMitad = mitad,
                fondoSolidaridad = fondo,
                cajaProfesional = caja,
                otrosDescuentos = otros
            )
        )
    }

    val entrada = Entrada(
        nominal = nominal.aNumero(),
        conyugeACargo = conyuge,
        hijos = hijos.aEntero(),
        hijosConDiscapacidad = hijosDisc.aEntero(),
        atribucionHijos = if (mitad) 0.5 else 1.0,
        fondoSolidaridad = fondo.aNumero(),
        cajaProfesional = caja.aNumero(),
        otrosDescuentos = otros.aNumero()
    )
    val parametros = ajustes.parametros
    val r: Resultado = Calculadora.calcular(entrada, parametros)

    // El banner se esconde mientras el teclado está abierto: si no, queda
    // pegado a las teclas y se convierte en una fábrica de toques accidentales.
    val tecladoAbierto = WindowInsets.isImeVisible

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sueldo líquido UY") },
                actions = {
                    IconButton(onClick = onAbrirConfiguracion) {
                        Icon(Icons.Filled.Settings, contentDescription = "Configuración")
                    }
                }
            )
        },
        bottomBar = {
            if (ajustes.nivelAnuncios.muestraBanner && !tecladoAbierto) {
                BannerPublicitario()
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Regla que no se negocia: si los valores están modificados, la
            // pantalla de resultados lo dice, arriba de todo y dentro de la captura.
            if (parametros.sonPersonalizados) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Valores modificados", fontWeight = FontWeight.Bold)
                        Text(
                            "Este cálculo no usa los valores oficiales del ejercicio " +
                                "${ParametrosFiscales.OFICIALES.ejercicio}, sino los que " +
                                "cargaste vos.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(onClick = onAbrirParametros) { Text("Revisar") }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Subtítulo: el título grande ahora vive en la barra superior.
            Text(
                "Parámetros ${parametros.ejercicio} · BPC ${moneda(parametros.bpc)}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(20.dp))

            CampoNumerico("Sueldo nominal mensual", nominal) { nominal = it }

            Spacer(Modifier.height(8.dp))

            FilaSwitch(
                titulo = "Cónyuge o concubino a cargo",
                subtitulo = "Solo si no tiene cobertura propia del SNIS",
                valor = conyuge
            ) { conyuge = it }

            CampoNumerico("Hijos menores a cargo", hijos) { hijos = it }
            CampoNumerico("Hijos/personas a cargo con discapacidad", hijosDisc) { hijosDisc = it }

            FilaSwitch(
                titulo = "Deducción de hijos compartida al 50 %",
                subtitulo = "Activalo si el otro padre deduce la otra mitad",
                valor = mitad
            ) { mitad = it }

            Spacer(Modifier.height(12.dp))
            Text(
                "Opcionales",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            CampoNumerico("Fondo de Solidaridad (mensual)", fondo) { fondo = it }
            CampoNumerico("Caja profesional (mensual)", caja) { caja = it }
            CampoNumerico("Otros descuentos", otros) { otros = it }

            Spacer(Modifier.height(24.dp))

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Detalle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    Fila("Nominal", moneda(r.nominal))
                    Fila("Jubilatorio (15 %)", "− " + moneda(r.jubilatorio))
                    Fila("FONASA (${porcentaje(r.tasaFonasa)})", "− " + moneda(r.fonasa))
                    Fila("FRL (0,1 %)", "− " + moneda(r.frl))
                    Fila("IRPF", "− " + moneda(r.irpf))
                    if (r.otrosDescuentos > 0) {
                        Fila("Otros descuentos", "− " + moneda(r.otrosDescuentos))
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Fila("Total descuentos", "− " + moneda(r.totalDescuentos))
                    Fila("Líquido a cobrar", moneda(r.liquido), destacado = true)
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Cómo se calculó el IRPF",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Fila("Impuesto por franjas", moneda(r.impuestoPorFranjas))
                    Fila("Deducciones computables", moneda(r.totalDeducciones))
                    Fila(
                        "Crédito (${porcentaje(r.tasaDeduccion)} de las deducciones)",
                        "− " + moneda(r.creditoDeducciones)
                    )
                    Fila("IRPF a retener", moneda(r.irpf))
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                if (parametros.sonPersonalizados)
                    "Cálculo hecho con valores cargados manualmente por vos, no con los " +
                        "oficiales. No sirve como referencia de tu liquidación real."
                else
                    "Cálculo estimativo con los parámetros vigentes ${parametros.ejercicio}. " +
                        "No sustituye la liquidación de tu empleador ni la información oficial " +
                        "de BPS y DGI. No contempla aguinaldo, salario vacacional, horas extra, " +
                        "multiempleo, partidas no gravadas ni créditos de la declaración jurada anual.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
