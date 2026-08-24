# Ficha de Google Play — Sueldo Líquido UY

Textos listos para pegar en Play Console. Los límites de caracteres son los que
impone Google; entre paréntesis, lo que ocupa cada texto de acá.

Aplica a la **versión 1.0** (`versionCode` 1, `versionName` 1.0), la que no
lleva publicidad. Los cambios que hay que hacerle a esta ficha cuando se publique
la 1.1 están al final.

---

## Nombre de la aplicación (máx. 30 — usa 17)

```
Sueldo Líquido UY
```

El nombre que aparece bajo el ícono en el teléfono es más corto —`Sueldo
Líquido`, en `res/values/strings.xml`— porque los lanzadores cortan alrededor de
los 12 caracteres. Son dos campos distintos y está bien que no coincidan.

No puede llevar "BPS" ni "DGI": Google rechaza las apps que aparentan ser de un
organismo público.

## Descripción breve (máx. 80 — usa 77)

```
Calculá tu sueldo líquido en Uruguay: aportes, FONASA e IRPF, con el detalle.
```

## Descripción completa (máx. 4000 — usa ~1900)

```
Ingresá tu sueldo nominal y mirá cuánto te queda en la mano, con el detalle de
cada descuento a la vista.

QUÉ CALCULA

• Aporte jubilatorio (15 %), con el tope mensual de asignaciones computables.
• FONASA, con la tasa que corresponde según tengas cónyuge o concubino a cargo,
  hijos menores a cargo, ambos o ninguno.
• Fondo de Reconversión Laboral (0,1 %).
• IRPF por franjas, con el crédito por deducciones ya descontado.
• Fondo de Solidaridad, Caja Profesional y cualquier otro descuento que cargues.

CÓMO ES USARLA

• Sin botón "calcular": el resultado se actualiza mientras escribís.
• El desglose muestra cada aporte con su tasa, el total de descuentos y el
  líquido destacado.
• Una segunda tarjeta explica de dónde salió el IRPF: impuesto por franjas,
  deducciones computables y crédito aplicado.
• Podés guardar tus datos para no volver a cargarlos, o desactivar el guardado.
• Funciona entera sin conexión.
• No pide ningún permiso.
• No recolecta datos: todo queda en tu teléfono.

VALORES Y TASAS EDITABLES

Los parámetros vienen cargados con los del ejercicio 2026. Cuando cambia la BPC
—cada 1.º de enero— podés cargar el valor nuevo vos mismo sin esperar a que se
actualice la app: todo lo que la ley expresa en BPC (la escala de IRPF, el
umbral de FONASA, las deducciones por hijo) se recalcula solo.

También sirve para simular escenarios o para revisar un recibo de otro año.
Volver a los valores oficiales es un toque. Si tenés valores modificados, la
pantalla de resultados te lo avisa con un cartel bien visible, para que no se te
escape en una captura de pantalla.

QUÉ NO CALCULA

Aguinaldo, salario vacacional, horas extra, retroactivos, la retención adicional
que anticipa el IRPF del aguinaldo, multiempleo, partidas no gravadas, y la
deducción de alquiler o de cuota hipotecaria, que son crédito de la declaración
jurada anual. Tampoco calcula la devolución anual de FONASA ni de IRPF.

AVISO

El resultado es estimativo y no sustituye la liquidación de tu empleador ni la
información oficial. Es una aplicación independiente, sin vínculo ni afiliación
con el Banco de Previsión Social, la Dirección General Impositiva ni ningún otro
organismo del Estado. Sigue los parámetros que esos organismos publican, pero no
los representa.

El código es abierto: github.com/hsosa09/sueldo-liquido-uy
```

---

## Categorización

| Campo | Valor |
|---|---|
| Tipo de aplicación | Aplicación |
| Categoría | Finanzas |
| Etiquetas | Calculadora, Finanzas personales, Impuestos |
| Correo de contacto | horaciososa99@gmail.com |
| Sitio web | https://github.com/hsosa09/sueldo-liquido-uy |
| Política de privacidad | https://hsosa09.github.io/sueldo-liquido-uy/politica-privacidad/ |

## Países

**Solo Uruguay.** Decidido el 23/8/2026, y no es una preferencia: mientras la
ficha no incluya el Espacio Económico Europeo, el Reino Unido ni Suiza, no rige
la obligación del formulario de consentimiento del SDK de UMP. Si algún día se
amplía la distribución, hay que implementar UMP **antes** de publicar esa
versión.

## Clasificación de contenido

Cuestionario "Utilidad / Productividad / Comunicación". Todo "No". Resultado
esperado: apto para todos.

## Seguridad de los datos

Versión 1.0, sin publicidad:

| Pregunta | Respuesta |
|---|---|
| ¿Recopila datos de usuarios? | **No** |
| ¿Comparte datos con terceros? | **No** |
| ¿Los datos se cifran en tránsito? | No aplica (no hay tránsito) |
| ¿El usuario puede pedir que se borren sus datos? | No aplica |

Lo que el usuario escribe queda en el almacenamiento privado de la app, en su
propio teléfono, y se borra al desinstalarla. Play no considera eso
"recopilación": no sale del dispositivo.

## Anuncios

Versión 1.0: **"¿Contiene anuncios?" → No.** Esta rama no incluye ningún SDK de
publicidad, no pide permiso de INTERNET y no muestra anuncios.

## Compras dentro de la aplicación

No.

---

## Novedades de esta versión (máx. 500)

```
Primera versión.

Calcula el sueldo líquido a partir del nominal: aporte jubilatorio con tope,
FONASA según la situación familiar, FRL e IRPF por franjas con el crédito por
deducciones. Muestra el desglose completo y explica cómo se llegó al IRPF.

Los valores y las tasas se pueden editar, para cuando cambia la BPC antes de que
salga una actualización.

Funciona sin conexión, no pide permisos y no recolecta datos.
```

---

## Recursos gráficos

| Recurso | Requisito de Play | Estado |
|---|---|---|
| Ícono | PNG 512 × 512, 32 bits, sin transparencia | `tienda/icono-play-512.png` |
| Gráfico de funciones | PNG o JPG 1024 × 500 | **falta** |
| Capturas de teléfono | 2 a 8, lado mayor 320–3840 px | `tienda/capturas/` |
| Capturas de tablet | Opcionales | no se hacen: la app es de una columna |

### Cómo se sacaron las capturas

Con un emulador dedicado, forzado a 1080 × 1920 —9:16 exacto, que es lo que
pide Play—. La resolución nativa del Pixel 7 es 1080 × 2400, que da 20:9 y
Play a veces lo rechaza.

```bash
export JAVA_HOME=/var/lib/flatpak/app/com.google.AndroidStudio/x86_64/stable/active/files/extra/jbr
~/Android/Sdk/cmdline-tools/latest/bin/avdmanager create avd \
    -n sueldo_capturas -k "system-images;android-36;google_apis_playstore;x86_64" -d pixel_7
~/Android/Sdk/emulator/emulator -avd sueldo_capturas -no-snapshot-save -no-boot-anim &

adb wait-for-device
adb shell wm size 1080x1920
adb shell wm density 420
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb exec-out screencap -p > tienda/capturas/01-calculadora.png
```

Las capturas que están en el repositorio se sacaron con la imagen de sistema
**android-37.2-beta3**, que era la única descargada en la máquina del autor.
Antes de subirlas a Play conviene rehacerlas con una imagen estable —API 36—
para que no aparezca ningún elemento de sistema de una versión que todavía no
salió.

Son capturas de pantalla limpias, sin marco de teléfono ni texto encima. Play
las acepta así. Si algún día se quiere el formato con títulos y fondo de color,
se arma aparte: no lo genera la app.

El ícono de la tienda se genera a partir del mismo dibujo que el del lanzador
(`res/drawable/ic_launcher_*.xml`) con `tienda/generar-icono.py`, así no pueden
quedar desincronizados.

---

## Antes de producción

La cuenta de Play Console es personal, así que Google exige una **prueba cerrada
con 12 testers durante 14 días corridos** antes de habilitar la publicación en
producción. Conviene arrancarla apenas la 1.0 esté subida: son dos semanas de
reloj que corren igual mientras se trabaja en la 1.1.

## Qué cambia cuando se publique la 1.1

La 1.1 sale de la rama `play`, que sí incluye AdMob. Al subirla hay que tocar
tres cosas de esta ficha, y las tres son obligatorias:

1. **Anuncios → Sí.** "¿Contiene anuncios?" pasa a Sí.
2. **Seguridad de los datos.** Hay que declarar el identificador de publicidad
   (Advertising ID): el SDK lo agrega al manifiesto final aunque el usuario deje
   la publicidad apagada.
3. **Política de privacidad.** La versión publicada tiene que describir a AdMob
   como tercero. La actual dice que la app no se conecta a ningún lado, y para
   la 1.1 eso deja de ser cierto.

Con la publicidad desactivada en configuración —que es el valor por defecto— la
calculadora sigue funcionando entera sin conexión, y eso tiene que seguir siendo
verdad en la 1.1.
