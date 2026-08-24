package uy.horacio.sueldoliquidouy

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uy.horacio.sueldoliquidouy.anuncios.GestorAnuncios
import uy.horacio.sueldoliquidouy.datos.Ajustes
import uy.horacio.sueldoliquidouy.datos.NivelAnuncios
import uy.horacio.sueldoliquidouy.datos.RepositorioAjustes
import uy.horacio.sueldoliquidouy.dominio.ParametrosFiscales

private const val URL_GITHUB = "https://github.com/hsosa09/sueldo-liquido-uy"
private const val URL_PRIVACIDAD = "https://hsosa09.github.io/sueldo-liquido-uy/politica-privacidad/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(
    repositorio: RepositorioAjustes,
    ajustes: Ajustes,
    onVolver: () -> Unit,
    onAbrirParametros: () -> Unit
) {
    val alcance = rememberCoroutineScope()
    val contexto = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
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

            // ---------- Tus datos ----------
            Encabezado(Icons.Filled.Info, "Tus datos")

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Recordar mis datos", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Al volver a abrir la app, los campos aparecen como los dejaste. " +
                                        "Los datos quedan solo en este teléfono.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = ajustes.recordarDatos,
                            onCheckedChange = { valor ->
                                alcance.launch { repositorio.guardarRecordar(valor) }
                            }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = { alcance.launch { repositorio.borrarDatos() } }) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Text("  Borrar los datos guardados")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // La sección de publicidad solo existe en la versión de la tienda.
            // En la versión sin anuncios, GestorAnuncios.DISPONIBLE es falso y
            // este bloque entero desaparece de la interfaz.
            if (GestorAnuncios.DISPONIBLE) {
                // ---------- Publicidad ----------
                Encabezado(Icons.Filled.Info, "Publicidad")

                Text(
                    "La app viene sin publicidad. Si querés, podés elegir cuánta aceptás ver. " +
                            "Los cálculos y todas las funciones básicas son iguales en los cuatro niveles, " +
                            "y podés volver a esta pantalla y cambiarlo cuando quieras.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        NivelAnuncios.entries.forEach { nivel ->
                            OpcionNivel(
                                nivel = nivel,
                                seleccionado = ajustes.nivelAnuncios == nivel,
                                onElegir = {
                                    alcance.launch { repositorio.guardarNivelAnuncios(nivel) }
                                    if (nivel != NivelAnuncios.NINGUNO) GestorAnuncios.iniciar(contexto)
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ---------- Valores y tasas ----------
            Encabezado(Icons.Filled.Info, "Valores y tasas")

            // Migración por ejercicio: no se pisa lo que cargó el usuario, se le avisa.
            if (ParametrosFiscales.OFICIALES.ejercicio > ajustes.parametros.ejercicio) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Hay valores oficiales nuevos", fontWeight = FontWeight.Bold)
                        Text(
                            "Esta versión de la app ya trae los valores del ejercicio " +
                                "${ParametrosFiscales.OFICIALES.ejercicio}. Estás usando los " +
                                "del ${ajustes.parametros.ejercicio}, cargados a mano.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            alcance.launch { repositorio.restaurarParametrosOficiales() }
                        }) {
                            Text("Usar los oficiales ${ParametrosFiscales.OFICIALES.ejercicio}")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (ajustes.parametros.sonPersonalizados)
                            "Estás usando valores modificados por vos."
                        else
                            "Usando los valores oficiales del ejercicio " +
                                "${ajustes.parametros.ejercicio}.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onAbrirParametros,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver y editar valores")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Acerca de ----------
            Encabezado(Icons.Filled.Info, "Acerca de")

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Sueldo Líquido UY", fontWeight = FontWeight.Bold)
                    Text(
                        "Versión ${BuildConfig.VERSION_NAME} · parámetros ${ajustes.parametros.ejercicio}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { abrirEnlace(contexto, URL_GITHUB) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ver el código en GitHub")
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = { abrirEnlace(contexto, URL_PRIVACIDAD) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Política de privacidad")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Aplicación independiente. No está afiliada, patrocinada ni avalada por el " +
                        "Banco de Previsión Social, la Dirección General Impositiva ni ningún " +
                        "organismo del Estado uruguayo.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Encabezado(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null)
        Text(
            "  $texto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun OpcionNivel(nivel: NivelAnuncios, seleccionado: Boolean, onElegir: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onElegir)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(selected = seleccionado, onClick = onElegir)
        Column(Modifier.weight(1f).padding(start = 4.dp)) {
            Text(nivel.titulo, style = MaterialTheme.typography.bodyLarge)
            Text(nivel.detalle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun abrirEnlace(contexto: Context, url: String) {
    try {
        contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(contexto, "No se encontró un navegador", Toast.LENGTH_SHORT).show()
    }
}
