package uy.horacio.sueldoliquidouy.datos

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import uy.horacio.sueldoliquidouy.dominio.ParametrosFiscales
import java.io.IOException

private val json = Json {
    ignoreUnknownKeys = true   // que una versión futura pueda agregar campos
    encodeDefaults = true
}

/**
 * Queda en /data/data/uy.horacio.sueldoliquidouy/files/datastore/ajustes.preferences_pb
 */
private val Context.almacen: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

/**
 * Cantidad de publicidad
 */
enum class NivelAnuncios(val codigo: Int, val titulo: String, val detalle: String) {
    NINGUNO(
        0, "Nivel 0 — sin publicidad",
        "La app no muestra ningún anuncio. Es el valor por defecto."
    ),
    BANNER(
        1, "Nivel 1 — banner al pie",
        "Una franja publicitaria abajo de la calculadora. No tapa el resultado ni interrumpe el uso."
    ),
    APERTURA(
        2, "Nivel 2 — anuncio al abrir la app",
        "Un anuncio de pantalla completa al iniciar, como máximo una vez cada 4 horas."
    ),
    AMBOS(
        3, "Nivel 3 — banner y anuncio al abrir",
        "Combina los niveles 1 y 2."
    );

    val muestraBanner: Boolean get() = this == BANNER || this == AMBOS
    val muestraApertura: Boolean get() = this == APERTURA || this == AMBOS

    companion object {
        fun desde(codigo: Int): NivelAnuncios =
            entries.firstOrNull { it.codigo == codigo } ?: NINGUNO
    }
}

/** Todo lo que la app recuerda entre sesiones. */
data class Ajustes(
    val recordarDatos: Boolean = true,
    val nominal: String = "",
    val conyugeACargo: Boolean = false,
    val hijos: String = "",
    val hijosConDiscapacidad: String = "",
    val atribucionMitad: Boolean = false,
    val fondoSolidaridad: String = "",
    val cajaProfesional: String = "",
    val otrosDescuentos: String = "",
    val nivelAnuncios: NivelAnuncios = NivelAnuncios.NINGUNO,
    /** Hasta cuándo está desbloqueado el comparador, en milisegundos epoch. */
    val comparadorHasta: Long = 0L,
    /** Cuándo se mostró el último anuncio de apertura. Control de frecuencia. */
    val ultimaApertura: Long = 0L,
    /** Falso hasta que el usuario terminó su primera sesión. Ver 6B.10. */
    val huboPrimeraSesion: Boolean = false,
    /** Valores y tasas con los que se calcula. Oficiales salvo que el usuario los cambie. */
    val parametros: ParametrosFiscales = ParametrosFiscales.OFICIALES
)

class RepositorioAjustes(private val contexto: Context) {

    private object Claves {
        val RECORDAR = booleanPreferencesKey("recordar")
        val NOMINAL = stringPreferencesKey("nominal")
        val CONYUGE = booleanPreferencesKey("conyuge")
        val HIJOS = stringPreferencesKey("hijos")
        val HIJOS_DISC = stringPreferencesKey("hijos_disc")
        val MITAD = booleanPreferencesKey("mitad")
        val FONDO = stringPreferencesKey("fondo")
        val CAJA = stringPreferencesKey("caja")
        val OTROS = stringPreferencesKey("otros")
        val ANUNCIOS = intPreferencesKey("nivel_anuncios")
        val COMPARADOR = longPreferencesKey("comparador_hasta")
        val ULTIMA_APERTURA = longPreferencesKey("ultima_apertura")
        val PRIMERA_SESION = booleanPreferencesKey("primera_sesion")
        val PARAMETROS = stringPreferencesKey("parametros_fiscales")
    }

    /** Flujo que emite los ajustes actuales y vuelve a emitir cada vez que cambian. */
    val flujo: Flow<Ajustes> = contexto.almacen.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            Ajustes(
                recordarDatos = p[Claves.RECORDAR] ?: true,
                nominal = p[Claves.NOMINAL] ?: "",
                conyugeACargo = p[Claves.CONYUGE] ?: false,
                hijos = p[Claves.HIJOS] ?: "",
                hijosConDiscapacidad = p[Claves.HIJOS_DISC] ?: "",
                atribucionMitad = p[Claves.MITAD] ?: false,
                fondoSolidaridad = p[Claves.FONDO] ?: "",
                cajaProfesional = p[Claves.CAJA] ?: "",
                otrosDescuentos = p[Claves.OTROS] ?: "",
                nivelAnuncios = NivelAnuncios.desde(p[Claves.ANUNCIOS] ?: 0),
                comparadorHasta = p[Claves.COMPARADOR] ?: 0L,
                ultimaApertura = p[Claves.ULTIMA_APERTURA] ?: 0L,
                huboPrimeraSesion = p[Claves.PRIMERA_SESION] ?: false,
                // Un JSON corrupto o de una versión incompatible no puede impedir
                // que la app arranque: se cae de vuelta a los oficiales.
                parametros = p[Claves.PARAMETROS]
                    ?.let { texto ->
                        runCatching { json.decodeFromString<ParametrosFiscales>(texto) }.getOrNull()
                    }
                    ?: ParametrosFiscales.OFICIALES
            )
        }

    /** Guarda solo los datos del formulario. No toca la configuración de publicidad. */
    suspend fun guardarDatos(a: Ajustes) {
        contexto.almacen.edit { p ->
            p[Claves.NOMINAL] = a.nominal
            p[Claves.CONYUGE] = a.conyugeACargo
            p[Claves.HIJOS] = a.hijos
            p[Claves.HIJOS_DISC] = a.hijosConDiscapacidad
            p[Claves.MITAD] = a.atribucionMitad
            p[Claves.FONDO] = a.fondoSolidaridad
            p[Claves.CAJA] = a.cajaProfesional
            p[Claves.OTROS] = a.otrosDescuentos
        }
    }

    suspend fun guardarRecordar(valor: Boolean) {
        contexto.almacen.edit { it[Claves.RECORDAR] = valor }
    }

    suspend fun guardarNivelAnuncios(nivel: NivelAnuncios) {
        contexto.almacen.edit { it[Claves.ANUNCIOS] = nivel.codigo }
    }

    suspend fun registrarApertura(instante: Long) {
        contexto.almacen.edit { it[Claves.ULTIMA_APERTURA] = instante }
    }

    suspend fun marcarPrimeraSesion() {
        contexto.almacen.edit { it[Claves.PRIMERA_SESION] = true }
    }

    suspend fun guardarParametros(p: ParametrosFiscales) {
        contexto.almacen.edit { it[Claves.PARAMETROS] = json.encodeToString(p) }
    }

    /**
     * Borra la clave en lugar de escribir los oficiales: así, cuando salga una
     * versión de la app con valores nuevos, le llega sola a quien nunca tocó nada.
     */
    suspend fun restaurarParametrosOficiales() {
        contexto.almacen.edit { it.remove(Claves.PARAMETROS) }
    }

    /** Desbloquea el comparador hasta el instante indicado. */
    suspend fun desbloquearComparador(hasta: Long) {
        contexto.almacen.edit { it[Claves.COMPARADOR] = hasta }
    }

    /** Borra los datos del formulario, pero conserva las preferencias de publicidad. */
    suspend fun borrarDatos() {
        contexto.almacen.edit { p ->
            listOf(
                Claves.NOMINAL, Claves.HIJOS, Claves.HIJOS_DISC,
                Claves.FONDO, Claves.CAJA, Claves.OTROS
            ).forEach { p.remove(it) }
            p[Claves.CONYUGE] = false
            p[Claves.MITAD] = false
        }
    }
}
