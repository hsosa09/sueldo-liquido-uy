# Sueldo Líquido UY — especificación del proyecto

App Android que calcula el sueldo líquido en Uruguay a partir del nominal. Proyecto personal, primer proyecto Android del autor. Destino final: Google Play Store.

Este archivo es la fuente de verdad. Si algo del código contradice lo que dice acá, el código está mal.

---

## 1. Entorno y datos del proyecto

| | |
|---|---|
| Sistema operativo | Ubuntu 26.04 LTS |
| Ruta del proyecto | `/home/horacio/AndroidStudioProjects/SueldoLiquidoUY` |
| Paquete | `uy.horacio.sueldoliquidouy` |
| Nombre del proyecto | `SueldoLiquidoUY` |
| Tema generado | `SueldoLiquidoUYTheme` |
| Lenguaje | Kotlin + Jetpack Compose (Material 3) |
| `minSdk` | **26** (Android 8.0) — requisito explícito, no bajarlo ni subirlo |
| `targetSdk` / `compileSdk` | **36** (Android 16) — exigido por Google Play desde el 31/8/2026 |
| Gradle | Kotlin DSL + version catalog (`gradle/libs.versions.toml`) |

Emuladores de referencia para probar: uno con **API 26** y uno con **API 36**. El cálculo debe dar idéntico en ambos.

---

## 2. Estado actual

**La 1.0 está terminada y verificada.** Compila, los 14 tests unitarios pasan, y
los cuatro casos de la sección 4 dan exacto tanto en los tests como en pantalla.
Los pasos 1 a 7 de la sección 9 están hechos.

Cómo está partido el trabajo en ramas:

| Rama | Qué es |
|---|---|
| `main` | La 1.0: sin publicidad, sin permisos, sin conexión. Es lo que se sube primero a Play. |
| `play` | La 1.1: la misma app más AdMob. Cambia cuatro archivos y nada más: `GestorAnuncios.kt`, `BannerPublicitario.kt`, el manifiesto y `app/build.gradle.kts`. |

En `main`, el paquete `anuncios` existe igual, con todas las operaciones vacías
y `GestorAnuncios.DISPONIBLE = false`. Es una costura, no código muerto: hace
que las pantallas, la navegación y los ajustes sean idénticos en las dos ramas,
así un cambio en la calculadora no hay que hacerlo dos veces.

Material de tienda, en `tienda/`:

- `ficha-play.md` — todos los textos de Play Console, con los límites de
  caracteres, las respuestas del formulario de Seguridad de los datos y qué hay
  que cambiar al subir la 1.1.
- `icono-play-512.png` y `generar-icono.py` — el ícono de la tienda se genera
  del mismo dibujo que el del lanzador, y el script falla si los dos se
  separaron.
- `capturas/` — cinco capturas de teléfono, 1080 × 1920.

`pruebas-manuales.md` tiene la lista de lo que los tests no pueden cubrir:
persistencia, navegación, rotación, parámetros y publicidad.

**Lo que falta para publicar** no es código:

1. El gráfico de funciones de 1024 × 500 que pide Play.
2. Crear la ficha en Play Console con los textos de `tienda/ficha-play.md`.
3. La prueba cerrada con 12 testers durante 14 días corridos, que Google exige
   antes de producción en las cuentas personales. Conviene arrancarla cuanto
   antes: son dos semanas de reloj.

---

## 3. La lógica fiscal (lo más importante del proyecto)

Todos los valores son del **ejercicio 2026**. Fuentes: comunicados del BPS y DGI.

### 3.1 Parámetros base

| Parámetro | Valor 2026 |
|---|---|
| BPC | **$ 6.864** |
| Tope mensual de asignaciones computables jubilatorias | **$ 288.836** |
| Tasa jubilatoria personal | 15 % |
| FRL | 0,1 % |

### 3.2 Aporte jubilatorio

```
jubilatorio = min(nominal, 288.836) × 15 %
```

El tope aplica a quienes están en el régimen mixto BPS + AFAP (la gran mayoría). El aporte patronal no aparece en el recibo del trabajador: no va en la app.

### 3.3 FONASA

Umbral: **2,5 BPC = $ 17.160**. FONASA y FRL van sobre el **nominal completo**, sin tope.

| Situación | ≤ 2,5 BPC | > 2,5 BPC |
|---|---|---|
| Sin cónyuge, sin hijos | 3 % | 4,5 % |
| Sin cónyuge, con hijos | 3 % | 6 % |
| Con cónyuge a cargo, sin hijos | 5 % | 6,5 % |
| Con cónyuge a cargo, con hijos | 5 % | 8 % |

"Cónyuge o concubino a cargo" cuenta solo si esa persona **no tiene cobertura propia del SNIS**. Esa aclaración debe estar visible en la UI.

### 3.4 IRPF

```
IRPF = max(0, impuesto_por_franjas − crédito_por_deducciones)
```

**Escala progresional mensual** (topes expresados en BPC, cada tramo tributa a su propia tasa):

| Hasta (BPC) | Hasta ($) | Tasa |
|---|---|---|
| 7 | 48.048 | 0 % |
| 10 | 68.640 | 10 % |
| 15 | 102.960 | 15 % |
| 30 | 205.920 | 24 % |
| 50 | 343.200 | 25 % |
| 75 | 514.800 | 27 % |
| 115 | 789.360 | 31 % |
| sin tope | — | 36 % |

**Deducciones computables** (se suman y al total se le aplica una tasa; no bajan la base imponible):

- aporte jubilatorio
- aporte FONASA
- aporte FRL
- por cada hijo menor a cargo: **20 BPC anuales** = $ 11.440/mes al 100 %, $ 5.720 al 50 %
- por cada hijo o persona a cargo con discapacidad: **40 BPC anuales** = $ 22.880/mes al 100 %
- Fondo de Solidaridad y Caja Profesional, si el usuario los carga

**Tasa de deducción**: 14 % si los ingresos nominales gravados son ≤ 15 BPC ($ 102.960); 8 % si superan ese monto.

La deducción por hijos se puede tomar 100 % por uno de los padres o 50 % y 50 %. La app debe permitir elegir.

### 3.5 Fórmula final

```
líquido = nominal − jubilatorio − FONASA − FRL − IRPF
                 − Fondo de Solidaridad − Caja Profesional − otros descuentos
```

### 3.6 Fuera de alcance (declarar en el aviso legal)

Aguinaldo, salario vacacional, horas extra, retroactivos, la retención adicional del 6 % que anticipa el IRPF del aguinaldo, multiempleo, partidas no gravadas, deducción de alquiler y cuotas hipotecarias (son crédito de la declaración jurada anual), devolución anual de FONASA e IRPF.

---

## 4. Casos de prueba con valores exactos

Estos números están verificados a mano. Los tests unitarios deben reproducirlos con `delta = 0.01`.

| Entrada | Jubilatorio | FONASA | FRL | IRPF | Líquido |
|---|---|---|---|---|---|
| 15.000, sin cargas | 2.250,00 | 450,00 (3 %) | 15,00 | 0,00 | **12.285,00** |
| 50.000, sin cargas | 7.500,00 | 2.250,00 (4,5 %) | 50,00 | 0,00 | **40.200,00** |
| 120.000, sin cargas | 18.000,00 | 5.400,00 (4,5 %) | 120,00 | 9.415,20 | **87.064,80** |
| 350.000, sin cargas | 43.325,40 (tope) | 15.750,00 | 350,00 | — | — |

Desglose del caso de 120.000, útil para depurar:
- impuesto por franjas = 11.296,80
- deducciones = 18.000 + 5.400 + 120 = 23.520
- tasa de deducción = 8 % (supera 15 BPC) → crédito = 1.881,60
- IRPF = 11.296,80 − 1.881,60 = 9.415,20

Tasas de FONASA con nominal 60.000: sin cargas 4,5 %; con hijos 6 %; con cónyuge 6,5 %; con ambos 8 %.

---

## 5. Arquitectura

```
uy.horacio.sueldoliquidouy
├── MainActivity.kt              NavHost + carga inicial + disparo del anuncio de apertura
├── PantallaCalculadora.kt       pantalla principal
├── PantallaConfiguracion.kt     ajustes
├── PantallaParametros.kt        editor de valores y tasas
├── Componentes.kt               composables y utilidades compartidas (sin `private`)
├── dominio/                     Kotlin puro, SIN nada de Android. Testeable sin emulador
│   ├── ParametrosFiscales.kt    parámetros + escala + validación
│   ├── Modelos.kt               Entrada, Resultado
│   └── Calculadora.kt           funciones puras
├── datos/
│   └── Ajustes.kt               DataStore Preferences + RepositorioAjustes + NivelAnuncios
└── anuncios/
    ├── GestorAnuncios.kt        AdMob: apertura + (opcional) recompensado
    └── BannerPublicitario.kt    AdView dentro de AndroidView
```

**Regla de oro**: el paquete `dominio` no importa nada de Android. Es lo que permite testear el cálculo en segundos sin emulador.

`Calculadora.calcular(entrada, parametros = ParametrosFiscales.OFICIALES)` — el valor por defecto existe para que los tests originales sigan pasando sin tocarlos.

### Dependencias

```kotlin
implementation("androidx.datastore:datastore-preferences:1.1.7")
implementation("androidx.navigation:navigation-compose:2.9.5")
implementation("androidx.compose.material:material-icons-extended")
implementation("com.google.android.gms:play-services-ads:25.4.0")
implementation(libs.kotlinx.serialization.json)
```

Para kotlinx.serialization hace falta el plugin. **El plugin debe usar la misma versión que Kotlin**: en `libs.versions.toml`, `kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }`. Si no coinciden, Gradle falla con un mensaje inútil sobre el compilador.

---

## 6. Funcionalidad

### 6.1 Pantalla calculadora

Entradas: nominal, cónyuge a cargo (switch), hijos, hijos con discapacidad, atribución 50 % (switch), Fondo de Solidaridad, Caja Profesional, otros descuentos.

Salida: tarjeta con el desglose completo (nominal, jubilatorio, FONASA con su tasa, FRL, IRPF, total de descuentos, líquido destacado) y una segunda tarjeta que explica cómo se calculó el IRPF.

- **No hay botón "Calcular"**: se recalcula en cada recomposición. La lógica es pura, así que es barato.
- Los campos numéricos filtran a solo dígitos.
- `rememberSaveable` en todo el estado, para sobrevivir a la rotación.
- Ícono de engranaje en la `TopAppBar` que lleva a configuración.
- Aviso legal al pie.

### 6.2 Persistencia de datos del usuario

DataStore Preferences. Autoguardado **con freno**: un `LaunchedEffect` con todas las claves y un `delay(600)` adentro, para que solo se escriba 600 ms después de la última tecla. Se respeta el switch "Recordar mis datos".

`preferencesDataStore(name = "ajustes")` va **a nivel de archivo**, nunca dentro de una clase. El flujo lleva `.catch { if (it is IOException) emit(emptyPreferences()) else throw it }` para que un archivo corrupto no impida abrir la app.

### 6.3 Pantalla de configuración

- Switch "Recordar mis datos" + botón "Borrar los datos guardados"
- Selector de nivel de publicidad (ver sección 7)
- Acceso a la pantalla de valores y tasas
- Botón "Ver el código en GitHub" (`Intent.ACTION_VIEW`; no necesita permisos ni `<queries>`)
- Enlace a la política de privacidad
- Versión vía `BuildConfig.VERSION_NAME`
- Aclaración de independencia respecto de BPS y DGI

### 6.4 Pantalla de valores y tasas (parámetros editables)

**Motivo**: cuando el Poder Ejecutivo fija la BPC nueva el 1.º de enero, pasan semanas o meses hasta que se publica la actualización. El usuario puede cargar el valor él mismo mientras tanto. Sirve además para simulaciones y para recalcular recibos de otros años.

Diseño clave: **lo que la ley expresa en BPC se guarda en BPC**, no en pesos. Así, cambiar la BPC recalcula sola la escala completa, los umbrales y las deducciones.

Estructura:
- **Básicos** (cambian todos los años): BPC, tope jubilatorio.
- **Avanzados** (detrás de un desplegable con advertencia): tasas de aportes, seis tasas de FONASA, umbrales, tasas de deducción, deducciones por hijo, y la escala de IRPF editable fila por fila.
- No se permite agregar ni quitar franjas: si una reforma cambia la cantidad de tramos, eso requiere actualizar la app.

Persistencia: JSON en una sola clave de DataStore, con `Json { ignoreUnknownKeys = true }` y `runCatching { }.getOrNull() ?: OFICIALES` para que un JSON corrupto no rompa el arranque.

Tres reglas que **no se negocian**:

1. **"Restaurar valores oficiales" siempre a un toque.** Implementado como *borrar la clave*, no como escribir los oficiales: así una futura actualización de la app llega sola a quien nunca tocó nada.
2. **Si los parámetros están modificados, la pantalla de resultados lo dice**, con una tarjeta destacada arriba de todo, y el aviso legal del pie cambia de texto. La gente saca capturas y las comparte; el aviso tiene que salir en la captura. `ParametrosFiscales` es `data class`, así que `parametros != OFICIALES` alcanza como detección.
3. **Guardado explícito y validado.** A diferencia de la calculadora, acá no hay autoguardado: una tasa a medio escribir contaminaría todos los cálculos futuros.

Validación antes de guardar: BPC y tope mayores que cero; todas las tasas entre 0 % y 100 %; topes de franjas estrictamente crecientes; solo la última franja sin tope; y la suma máxima de aportes menor al 100 % del nominal (si no, el líquido daría negativo).

**Migración**: `ParametrosFiscales` tiene un campo `ejercicio`. Si `OFICIALES.ejercicio > parametros.ejercicio`, configuración muestra una tarjeta ofreciendo adoptar los oficiales nuevos. No se pisa lo del usuario en silencio. Cada enero hay que subir `ejercicio` junto con la BPC, o el aviso nunca se dispara.

---

## 7. Publicidad — reglas de cumplimiento

**Esta sección tiene prioridad sobre cualquier consideración de ingresos. Se llegó a este diseño corrigiendo un plan anterior que violaba tres políticas de AdMob. No revertirlo.**

### 7.1 Los cuatro niveles

Configurables por el usuario, persistidos, reversibles en cualquier momento.

| Nivel | Nombre | Formato |
|---|---|---|
| 0 | Sin publicidad — **valor por defecto** | ninguno |
| 1 | Banner al pie de la calculadora | Banner adaptativo anclado |
| 2 | Anuncio al abrir la app | Apertura de la app (app open) |
| 3 | Ambos | Banner + apertura |

### 7.2 Prohibiciones absolutas

- **Ningún anuncio intersticial en ninguna parte de la app.** La app tiene una pantalla de contenido; no existe un momento legítimo de "entre páginas de contenido". Google prohíbe expresamente los intersticiales al abrir o al salir de la app.
- **Ningún anuncio al entrar a configuración, en ningún nivel.** Configuración es donde el usuario apaga la publicidad; cobrarle un anuncio por llegar al interruptor de apagado es un patrón oscuro, y un anuncio por cada acción del usuario incumple la política de frecuencia.
- **Ningún texto que pida, sugiera o agradezca ver anuncios.** Nada de "apoyá el proyecto", "ayudame activando anuncios", "gracias por el apoyo". Eso es solicitación de vistas y AdMob la trata como tráfico inválido. Los textos describen qué pasa y dejan elegir.
- **Ningún anuncio recompensado automático.** La política exige aceptación explícita para cada anuncio recompensado; no puede existir una opción de configuración que los active solos.

### 7.3 Frenos del anuncio de apertura

Los tres, obligatorios:

1. Nunca en la primera sesión del usuario (bandera persistida `huboPrimeraSesion`).
2. Máximo uno cada 4 horas (timestamp persistido `ultimaApertura`).
3. Una sola evaluación por arranque (`rememberSaveable`), para que no reaparezca al volver de segundo plano.

### 7.4 Reglas del banner

- Va en el `bottomBar` del `Scaffold`, con su propio espacio; nunca superpuesto al contenido.
- **Se esconde mientras el teclado está abierto** (`WindowInsets.isImeVisible`). Un banner pegado al teclado numérico genera clics accidentales, que son tráfico inválido.
- Separación visual clara y etiqueta "Publicidad" arriba.
- Sin forzar el refresco: el ritmo por defecto del SDK.
- `onRelease = { it.destroy() }` en el `AndroidView`, o el `AdView` filtra memoria.

### 7.5 Identificadores

Durante todo el desarrollo, **solo los de prueba de Google**:

| | |
|---|---|
| App ID | `ca-app-pub-3940256099942544~3347511713` |
| Banner adaptativo | `ca-app-pub-3940256099942544/9214589741` |
| Apertura | `ca-app-pub-3940256099942544/9257395921` |
| Recompensado | `ca-app-pub-3940256099942544/5224354917` |

**Nunca poner identificadores reales en el código sin que el autor lo pida explícitamente.** Tocar los propios anuncios reales implica suspensión de cuenta y retención de lo acumulado.

App ID lleva `~` y va en el manifiesto. Ad Unit ID lleva `/` y va en el código. Confundirlos hace crashear la app al arrancar.

### 7.6 Consecuencias de tener publicidad

Con AdMob la app deja de ser "sin permisos y sin conexión": usa `INTERNET` y el SDK agrega `AD_ID` al manifiesto final. Eso obliga a una política de privacidad distinta, a declarar "Contiene anuncios: Sí" en Play Console y a declarar el identificador de publicidad en el formulario de Seguridad de los datos. Con los anuncios desactivados, la calculadora funciona completamente sin conexión, y eso debe seguir siendo cierto.

### 7.7 Decisión abierta: el anuncio recompensado

Un anuncio recompensado necesita una recompensa real, anunciada de antemano, con aceptación explícita en cada vista. Se evaluaron tres opciones:

- **Quitar la publicidad por 2 horas** — válido por política, pero **descartado**: no tiene valor, porque el usuario ya puede apagar la publicidad para siempre y gratis desde configuración.
- **Comparador de sueldos** (dos nominales, dos líquidos lado a lado, reutilizando `Calculadora.calcular`) — la mejor recompensa, pero hay que construir la función.
- **Compartir el desglose** por `Intent` de compartir — quince líneas, útil de verdad, no bloquea nada esencial.

**Default para implementar: no incluir recompensado en la 1.1.** Los cuatro niveles solos ya son un diseño coherente. Si el autor pide agregarlo, la opción recomendada es *compartir el desglose*.

**Nunca publicar un botón que prometa una recompensa que la app todavía no entrega.**

---

## 8. Restricciones de producto y de tienda

- El nombre, el ícono y las capturas **no** pueden usar "BPS", "DGI" ni sus logos. Google rechaza apps que aparentan ser oficiales de un organismo público. Mencionar en la descripción que se siguen los parámetros publicados por esos organismos sí está bien.
- Aviso legal visible en la app: cálculo estimativo, no sustituye la liquidación del empleador ni la información oficial, aplicación independiente sin afiliación estatal.
- La app no recolecta datos del usuario. Todo queda en el dispositivo.
- **Distribución restringida a Uruguay** (decidido el 23/8/2026). De esto depende una obligación: el formulario de consentimiento del SDK de UMP es exigible solo para usuarios del Espacio Económico Europeo, Reino Unido y Suiza. Mientras la ficha de Play no los incluya, UMP no hace falta y la política de privacidad no debe prometerlo. **Si algún día se amplía la distribución, hay que implementar UMP antes de publicar esa versión.**
- La cuenta de Play Console es personal, así que antes de producción hace falta una prueba cerrada con 12 testers durante 14 días corridos.
- Recomendación de secuencia: **publicar primero la 1.0 sin publicidad**, pasar la revisión con la app más simple posible, y subir la 1.1 con configuración y anuncios como actualización.

---

## 9. Qué hacer, en orden

Los siete pasos del plan original están hechos. Se dejan anotados porque
sirven para entender por qué el proyecto está armado como está.

1. ~~Dejar el proyecto compilando.~~
2. ~~Verificar los tests unitarios y los 4 casos de la sección 4.~~ Son 14 tests.
3. ~~Migración a varias pantallas: `Componentes.kt`, navegación, DataStore, configuración, botón a GitHub.~~
4. ~~Paquete `anuncios` con banner y apertura, identificadores de prueba y los tres frenos.~~ Vive en la rama `play`; en `main` quedó la versión vacía.
5. ~~Pantalla de valores y tasas, con validación, aviso en resultados y migración por ejercicio.~~
6. ~~Batería de tests.~~ Los unitarios en `CalculadoraTest.kt`; los manuales en `pruebas-manuales.md`.
7. ~~Pulido: ícono, textos de la ficha, capturas.~~ Todo en `tienda/`.

Lo que sigue, en orden:

8. El gráfico de funciones de 1024 × 500.
9. Subir la 1.0 a Play Console y arrancar la prueba cerrada de 14 días.
10. Recién con la 1.0 aprobada, publicar la 1.1 desde la rama `play`. Antes de
    subirla hay que actualizar tres cosas de la ficha —anuncios, identificador de
    publicidad y política de privacidad—; están detalladas al final de
    `tienda/ficha-play.md`.

---

## 10. Cosas que no hay que hacer

- No bajar ni subir `minSdk`. Si aparece *"Call requires API level X (current min is 26)"*, buscar la alternativa de AndroidX o usar `if (Build.VERSION.SDK_INT >= …)`.
- No agregar intersticiales, ni anuncios en la ruta hacia configuración, ni copy que solicite vistas de anuncios.
- No quitar el aviso de "valores modificados" de la pantalla de resultados.
- No tocar el keystore ni subirlo al repositorio. `*.jks` y `*.keystore` deben estar en `.gitignore`.
- No poner identificadores reales de AdMob.
- No inventar valores fiscales. Si hace falta un dato que no está en la sección 3, preguntar en vez de estimar: un número inventado en una calculadora de sueldos es peor que una función faltante.
- No convertir el paquete `dominio` en dependiente de Android.

---

## 11. Documentos complementarios

| Archivo | Para qué |
|---|---|
| `puesta-en-marcha.md` | Dejar el proyecto compilando en una máquina nueva, y qué cosas no viajan en el repositorio. |
| `pruebas-manuales.md` | Lo que los tests unitarios no cubren: persistencia, navegación, rotación, parámetros y publicidad. |
| `tienda/ficha-play.md` | Los textos de Play Console y las respuestas del formulario de Seguridad de los datos. |

Existe además una guía larga (`guia-app-sueldo-liquido-uruguay.md`) con el instructivo completo paso a paso: instalación en Ubuntu, código de referencia de cada archivo, explicación de las políticas de AdMob citadas acá, proceso de publicación en Play Console y mantenimiento anual. Consultarla cuando haga falta el detalle o la justificación de alguna decisión.
