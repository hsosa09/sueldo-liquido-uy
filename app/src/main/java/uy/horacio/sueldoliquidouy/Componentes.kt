package uy.horacio.sueldoliquidouy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

/**
 * Componentes y utilidades compartidos entre la calculadora y la configuración.
 *
 * Estas funciones estaban dentro de MainActivity.kt en la versión 1.0. Al partir
 * la app en varias pantallas hay que sacarlas a un archivo propio y quitarles el
 * modificador `private`, o el resto de los archivos no las ve.
 */

// ---------------------------------------------------------------- composables

@Composable
fun CampoNumerico(
    etiqueta: String,
    valor: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = { texto -> onChange(texto.filter { it.isDigit() }.take(9)) },
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun FilaSwitch(
    titulo: String,
    subtitulo: String,
    valor: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = valor, onCheckedChange = onChange)
    }
}

@Composable
fun Fila(
    etiqueta: String,
    valor: String,
    destacado: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            etiqueta,
            style = if (destacado) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            valor,
            style = if (destacado) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ----------------------------------------------------------------- utilidades

private val formatoNumero: NumberFormat =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("es-UY")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

/** Formatea un importe como "$ 87.065". */
fun moneda(valor: Double): String = "$ " + formatoNumero.format(valor)

/** Formatea una tasa en tanto por uno como "4,5 %". */
fun porcentaje(tasa: Double): String {
    val v = tasa * 100
    return if (v % 1.0 == 0.0) "${v.toInt()} %"
    else String.format(Locale.US, "%.1f %%", v).replace(".", ",")
}

/** Texto del campo -> número. Vacío o inválido se toma como 0. */
fun String.aNumero(): Double = toDoubleOrNull() ?: 0.0

/** Texto del campo -> entero. Vacío o inválido se toma como 0. */
fun String.aEntero(): Int = toIntOrNull() ?: 0
