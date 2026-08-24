package uy.horacio.sueldoliquidouy

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uy.horacio.sueldoliquidouy.anuncios.GestorAnuncios
import uy.horacio.sueldoliquidouy.datos.RepositorioAjustes
import uy.horacio.sueldoliquidouy.ui.theme.SueldoLiquidoUYTheme

/** Cuatro horas: el intervalo mínimo entre dos anuncios de apertura. */
private const val ESPERA_ENTRE_APERTURAS = 4L * 60 * 60 * 1000

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repositorio = RepositorioAjustes(applicationContext)
        setContent {
            SueldoLiquidoUYTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(repositorio)
                }
            }
        }
    }
}

@Composable
fun App(repositorio: RepositorioAjustes) {
    val ajustes by repositorio.flujo.collectAsStateWithLifecycle(initialValue = null)
    val contexto = LocalContext.current
    val actividad = contexto as? Activity

    val actuales = ajustes
    if (actuales == null) {
        // Medio segundo, a lo sumo: lo que tarda en leerse el archivo de ajustes.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Marca que el usuario ya tuvo su primera sesión. A partir de la segunda,
    // y solo si él lo habilitó, puede aparecer el anuncio de apertura.
    LaunchedEffect(actuales.huboPrimeraSesion) {
        if (!actuales.huboPrimeraSesion) repositorio.marcarPrimeraSesion()
    }

    // Niveles 2 y 3: anuncio al abrir. Con tres condiciones de freno.
    var yaSeEvaluoApertura by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(actuales.nivelAnuncios, actuales.huboPrimeraSesion) {
        if (yaSeEvaluoApertura || actividad == null) return@LaunchedEffect
        yaSeEvaluoApertura = true

        if (!actuales.nivelAnuncios.muestraApertura) return@LaunchedEffect
        if (!actuales.huboPrimeraSesion) return@LaunchedEffect          // nunca en la 1.ª sesión

        val ahora = System.currentTimeMillis()
        if (ahora - actuales.ultimaApertura < ESPERA_ENTRE_APERTURAS) return@LaunchedEffect

        GestorAnuncios.iniciar(contexto)
        GestorAnuncios.precargarApertura(contexto) {
            GestorAnuncios.mostrarApertura(actividad) {
                // Solo cuenta si efectivamente se mostró.
            }
        }
        repositorio.registrarApertura(ahora)
    }

    // Si hay banner habilitado, el SDK tiene que estar arrancado.
    LaunchedEffect(actuales.nivelAnuncios) {
        if (actuales.nivelAnuncios != uy.horacio.sueldoliquidouy.datos.NivelAnuncios.NINGUNO) {
            GestorAnuncios.iniciar(contexto)
        }
    }

    val navegador = rememberNavController()

    NavHost(navController = navegador, startDestination = "calculadora") {
        composable("calculadora") {
            PantallaCalculadora(
                repositorio = repositorio,
                ajustes = actuales,
                onAbrirConfiguracion = { navegador.navigate("configuracion") },
                onAbrirParametros = { navegador.navigate("parametros") }
            )
        }
        composable("configuracion") {
            PantallaConfiguracion(
                repositorio = repositorio,
                ajustes = actuales,
                onVolver = { navegador.popBackStack() },
                onAbrirParametros = { navegador.navigate("parametros") }
            )
        }
        composable("parametros") {
            PantallaParametros(
                repositorio = repositorio,
                ajustes = actuales,
                onVolver = { navegador.popBackStack() }
            )
        }
    }
}
