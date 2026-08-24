# Pruebas manuales

Lo que no cubren los tests unitarios. El dominio —el cálculo y la validación de
parámetros— está cubierto por `CalculadoraTest.kt` y corre en segundos con
`./gradlew testDebugUnitTest`. Lo de acá necesita un teléfono o un emulador:
persistencia, navegación, rotación, publicidad.

Recorrer la lista entera antes de subir una versión a Play. Toma unos veinte
minutos.

## Antes de empezar

```bash
export JAVA_HOME=/var/lib/flatpak/app/com.google.AndroidStudio/x86_64/stable/active/files/extra/jbr
./gradlew testDebugUnitTest      # los 14 tests tienen que pasar
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Para arrancar de cero en cualquier momento:

```bash
adb shell pm clear uy.horacio.sueldoliquidouy
```

Hay que probar en **API 26** y en **API 36**. El cálculo tiene que dar idéntico
en las dos: si difiere, casi siempre es formato de número o una API que no
existe en 26.

---

## 1. El cálculo, contra los valores verificados a mano

Los mismos cuatro casos de la sección 4 de `CLAUDE.md`, pero mirando la
pantalla. Los tests ya los verifican; esto atrapa lo que está entre el dominio y
la interfaz: un campo mal conectado, un redondeo del formateador, una tasa que
se muestra distinta de la que se aplicó.

| # | Entrada | Tiene que mostrar |
|---|---|---|
| 1.1 | 15.000, sin cargas | FONASA (3 %) − $ 450 · IRPF $ 0 · **Líquido $ 12.285** |
| 1.2 | 50.000, sin cargas | FONASA (4,5 %) − $ 2.250 · IRPF $ 0 · **Líquido $ 40.200** |
| 1.3 | 120.000, sin cargas | Jubilatorio − $ 18.000 · FONASA (4,5 %) − $ 5.400 · FRL − $ 120 · IRPF − $ 9.415 · **Líquido $ 87.065** |
| 1.4 | 350.000, sin cargas | Jubilatorio − $ 43.325 (el tope, no $ 52.500) · FONASA − $ 15.750 |

En 1.3, la tarjeta "Cómo se calculó el IRPF" tiene que decir: impuesto por
franjas $ 11.297, deducciones $ 23.520, crédito (8 %) − $ 1.882, a retener
$ 9.415.

**1.5 — tasas de FONASA.** Con nominal 60.000, la tasa que aparece entre
paréntesis va cambiando:

| Cargas | Tasa |
|---|---|
| ninguna | 4,5 % |
| hijos | 6 % |
| cónyuge | 6,5 % |
| ambos | 8 % |

**1.6 — el tope no toca a FONASA ni al FRL.** Con 350.000, el jubilatorio se
frena en el tope pero FONASA y FRL siguen calculándose sobre el nominal
completo. Es el error clásico al implementar el tope.

**1.7 — deducción de hijos al 50 %.** Con 120.000 y 2 hijos, prender el switch
"Deducción de hijos compartida al 50 %" tiene que **subir** el IRPF: la
deducción por hijo baja de $ 11.440 a $ 5.720 cada uno.

**1.8 — nominal vacío o cero.** No muestra error, no muestra `NaN`, no rompe.
Todo en cero.

**1.9 — los campos filtran.** Escribir letras, comas, puntos o un signo menos en
cualquier campo numérico: no entran.

---

## 2. Persistencia

**2.1 — sobrevive a cerrar la app.** Cargar nominal, cónyuge, hijos y algún
descuento opcional. Salir con el botón atrás, matar la app desde recientes,
volver a abrir. Todo tiene que estar como se dejó.

**2.2 — el autoguardado tiene freno.** Escribir un nominal largo dígito por
dígito. El guardado sale una sola vez, 600 ms después de la última tecla, no una
vez por tecla. Se nota en logcat o, mejor, no se nota en nada: la app no puede
trabarse mientras se escribe.

**2.3 — "Recordar mis datos" apagado.** Apagarlo en configuración, volver,
cambiar el nominal, matar la app, abrir. Los campos tienen que aparecer vacíos.

**2.4 — "Borrar los datos guardados".** Con datos cargados, tocar el botón.
Los campos se vacían y siguen vacíos después de reabrir.

**2.5 — rotación.** Con datos a medio cargar y el resultado en pantalla, rotar
el teléfono. No se pierde nada, no se reinicia el scroll a cero de golpe, el
resultado sigue siendo el mismo.

**2.6 — archivo corrupto.** El caso que justifica el `.catch { }` del flujo:

```bash
adb shell "run-as uy.horacio.sueldoliquidouy sh -c 'echo basura > files/datastore/ajustes.preferences_pb'"
```

La app tiene que **abrir igual**, con los campos vacíos. Si crashea, el `catch`
del `IOException` se rompió.

---

## 3. Navegación

**3.1 — ida y vuelta.** Calculadora → engranaje → configuración → "Ver y editar
valores" → atrás → atrás. Termina en la calculadora con los datos intactos.

**3.2 — el botón atrás del sistema** hace lo mismo que la flecha de la
`TopAppBar`, en las dos pantallas.

**3.3 — atrás desde la calculadora** cierra la app. No deja una pantalla en
blanco ni vuelve a una pantalla anterior.

**3.4 — GitHub.** "Ver el código en GitHub" abre el navegador en
`github.com/hsosa09/sueldo-liquido-uy`. Con el navegador desinstalado no puede
crashear.

**3.5 — política de privacidad.** Mismo caso, con la URL de la política.

**3.6 — la versión.** Configuración muestra la de `versionName`. Si dice
"1.0-SNAPSHOT" o queda vacía, `BuildConfig` no está bien conectado.

---

## 4. Valores y tasas

**4.1 — la BPC arrastra todo.** Cambiar la BPC a 7.500 y guardar. Con nominal
120.000 el IRPF tiene que **bajar** a $ 7.793 (las franjas se corren hacia
arriba). Es la prueba de que la escala está guardada en BPC y no en pesos.

**4.2 — el aviso sale en la captura.** Con valores modificados:

- arriba de la calculadora aparece la tarjeta **"Valores modificados"**;
- el aviso legal del pie cambia y dice que el cálculo no usa los valores
  oficiales.

Los dos, no uno. La gente comparte capturas.

**4.3 — restaurar es un toque.** "Restaurar valores oficiales 2026" deja todo
como venía y la tarjeta de aviso desaparece.

**4.4 — restaurar borra la clave, no escribe los oficiales.** Después de
restaurar, la app no puede quedar con una copia congelada de los valores de hoy:
si mañana sale una versión con otra BPC, ese usuario tiene que recibirla sola.
Se verifica levantando `OFICIALES.bpc` en el código, recompilando e instalando
encima: el valor nuevo tiene que aparecer sin tocar nada.

**4.5 — la validación frena lo imposible.** Ninguno de estos puede guardarse, y
cada uno tiene que explicar por qué:

| Qué cargar | Qué tiene que decir |
|---|---|
| BPC = 0 | la BPC tiene que ser mayor que cero |
| una tasa = 150 % | tiene que estar entre 0 % y 100 % |
| franjas desordenadas (30 antes que 15) | los topes van de menor a mayor |
| un tope en la última franja | la última franja no puede tener tope |
| jubilatorio 60 % + FONASA 50 % | los aportes suman más del 100 % |

**4.6 — no hay autoguardado acá.** Escribir media tasa y salir con atrás sin
guardar: el cálculo de la calculadora no puede haber cambiado.

**4.7 — JSON corrupto.** Igual que 2.6, pero sobre la clave de parámetros: la
app abre con los valores oficiales, no crashea.

**4.8 — migración de ejercicio.** Con parámetros propios guardados, subir
`OFICIALES.ejercicio` a 2027, recompilar e instalar encima. Configuración tiene
que ofrecer adoptar los oficiales nuevos, **sin** pisar lo del usuario solo.

---

## 5. Publicidad — solo en la rama `play`

La rama `main` no incluye ningún SDK de anuncios: `GestorAnuncios.DISPONIBLE`
está en `false` y no hay nada que probar. Estos casos son para la 1.1.

**5.1 — el valor por defecto es sin publicidad.** Instalación limpia, nivel 0.
Ni banner ni anuncio de apertura.

**5.2 — nunca en la primera sesión.** Con nivel 2 o 3 recién elegido, cerrar y
volver a abrir: el anuncio de apertura **no** sale. Recién a partir de la
segunda sesión.

**5.3 — uno cada 4 horas.** Después de ver uno, cerrar y abrir varias veces
seguidas: no aparece otro. Para no esperar cuatro horas, atrasar el timestamp
persistido `ultimaApertura`.

**5.4 — una sola evaluación por arranque.** Con la app abierta, mandarla a
segundo plano y traerla de vuelta. No aparece un anuncio al volver.

**5.5 — el banner se esconde con el teclado.** Tocar un campo numérico: el
banner desaparece mientras el teclado está arriba, y vuelve al cerrarlo. Nunca
puede quedar un banner pegado al teclado.

**5.6 — el banner tiene su propio espacio.** Va en el `bottomBar`, con la
etiqueta "Publicidad" arriba. No se superpone al contenido ni tapa el líquido.

**5.7 — apagar la publicidad la apaga de verdad.** Bajar a nivel 0 y reabrir: no
queda ningún banner ni anuncio.

**5.8 — nada de anuncios camino a configuración.** Entrar y salir de
configuración veinte veces, en cualquier nivel. Cero anuncios.

**5.9 — sin conexión.** Con el modo avión prendido y la publicidad apagada, la
calculadora funciona entera. Con la publicidad prendida, el hueco del anuncio no
puede romper la pantalla ni mostrar un error.

**5.10 — identificadores de prueba.** Antes de subir nada: confirmar que los
`ad unit id` son los de prueba de Google, salvo que el autor haya puesto los
reales a propósito.

---

## 6. Compatibilidad

**6.1 — API 26.** Todo lo de arriba, en un emulador de Android 8.0. Especial
atención al formato de los montos: separador de miles y decimales tienen que
verse igual que en API 36.

**6.2 — texto grande.** Ajustes del sistema, tamaño de fuente al máximo. Las
etiquetas de la calculadora no pueden quedar cortadas ni pisar los montos.

**6.3 — modo oscuro.** Prender el tema oscuro del sistema. Todo legible; nada
en gris sobre gris. Ojo con la tarjeta de "Valores modificados", que usa el
color de error.

**6.4 — pantalla chica.** Un emulador de 5", densidad alta. La tarjeta del
desglose no puede desbordar a lo ancho.
