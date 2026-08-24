package uy.horacio.sueldoliquidouy.anuncios

import android.app.Activity
import android.content.Context

/**
 * Versión sin publicidad.
 *
 * Esta rama de la app no incluye ningún SDK de anuncios, no pide permiso de
 * internet y no muestra publicidad de ninguna clase. Este archivo existe
 * únicamente como costura: define la misma superficie que la versión de la
 * tienda, con todas las operaciones vacías, para que el resto del código
 * —pantallas, navegación, ajustes— sea idéntico en las dos ramas y los cambios
 * en la calculadora no haya que hacerlos dos veces.
 *
 * La rama `play` reemplaza este archivo, `BannerPublicitario.kt`, el manifiesto
 * y `app/build.gradle.kts`. Nada más.
 */
object GestorAnuncios {

    /**
     * Interruptor único que consulta la interfaz. En falso, la pantalla de
     * configuración no ofrece el selector de publicidad y el pie de la
     * calculadora nunca reserva espacio para un banner.
     */
    const val DISPONIBLE = false

    /** Sin uso en esta versión. Existe para que la firma coincida. */
    const val ID_BANNER = ""

    fun iniciar(contexto: Context) = Unit

    fun precargarApertura(contexto: Context, alCargar: () -> Unit = {}) = Unit

    fun mostrarApertura(actividad: Activity, alTerminar: () -> Unit = {}) {
        // No hay anuncio que mostrar: se sigue de largo.
        alTerminar()
    }
}
