# Guía completa: app Android de salario líquido (Uruguay), de cero a Google Play

**Última actualización de datos: agosto 2026 · Parámetros fiscales del ejercicio 2026 (BPC $ 6.864).**

Esta guía asume que nunca programaste una app Android. Todo lo que hace falta está acá: herramientas, instalación, el cálculo fiscal, el código completo, las pruebas, y el proceso de publicación en Google Play.

> **Supuestos de esta versión de la guía**
> - **Entorno de desarrollo: Ubuntu 26.04 LTS** (*Resolute Raccoon*). Todos los comandos, rutas y pasos de instalación son los de Ubuntu.
> - **Compatibilidad de la app: desde Android 8.0 (API 26) hasta Android 16 (API 36) y lo que venga.** En términos del proyecto: `minSdk = 26`, `targetSdk = 36`.

---

## Índice

0. [Mapa general y tiempos realistas](#0-mapa-general-y-tiempos-realistas)
1. [Decisiones previas](#1-decisiones-previas)
2. [Herramientas, requisitos y costos](#2-herramientas-requisitos-y-costos)
3. [La lógica del cálculo (lo más importante)](#3-la-lógica-del-cálculo-lo-más-importante)
4. [Instalar y configurar el entorno](#4-instalar-y-configurar-el-entorno)
5. [Crear el proyecto y entender su estructura](#5-crear-el-proyecto-y-entender-su-estructura)
6. [El código completo](#6-el-código-completo)
6B. [Ampliación: configuración, GitHub y anuncios](#6b-ampliación-configuración-persistente-github-y-anuncios-opcionales)
6C. [Parámetros fiscales editables por el usuario](#6c-parámetros-fiscales-editables-por-el-usuario)
7. [Pruebas](#7-pruebas)
8. [Pulido: ícono, nombre, aviso legal](#8-pulido-ícono-nombre-aviso-legal)
9. [Preparar el paquete firmado para publicar](#9-preparar-el-paquete-firmado-para-publicar)
10. [Política de privacidad (obligatoria)](#10-política-de-privacidad-obligatoria)
11. [Cuenta de Google Play Console](#11-cuenta-de-google-play-console)
12. [Crear la app en Play Console y completar la ficha](#12-crear-la-app-en-play-console-y-completar-la-ficha)
13. [Prueba cerrada: 12 testers, 14 días](#13-prueba-cerrada-12-testers-14-días)
14. [Producción: publicar de verdad](#14-producción-publicar-de-verdad)
15. [Mantenimiento anual](#15-mantenimiento-anual)
16. [Problemas frecuentes](#16-problemas-frecuentes)
17. [Checklist final](#17-checklist-final)
18. [Enlaces oficiales](#18-enlaces-oficiales)

---

## 0. Mapa general y tiempos realistas

| Fase | Qué hacés | Tiempo estimado |
|---|---|---|
| A | Instalar herramientas y crear el proyecto | 2–4 h (la descarga es lo lento) |
| B | Escribir la lógica de cálculo + tests | 4–8 h |
| C | Armar la pantalla (UI) | 4–8 h |
| D | Probar en emulador y en tu celular | 2–4 h |
| E | Ícono, textos, aviso legal, capturas | 2–4 h |
| F | Crear cuenta Play Console + verificación de identidad | 1 h de trámite + 2–7 días de espera |
| G | Prueba cerrada con 12 testers | **14 días corridos, mínimo** |
| H | Solicitar acceso a producción + revisión de Google | 1–7 días |
| I | Revisión final de la app y publicación | 1–7 días |

**Total realista: entre 4 y 8 semanas**, de las cuales casi 3 son esperas administrativas. Buena noticia: las fases se solapan. Podés arrancar el trámite de la cuenta (F) mientras todavía estás programando (B/C), y podés reclutar testers mientras terminás la app.

> **Consejo de orden**: no dejes la cuenta de Play Console para el final. El reloj de los 14 días no arranca hasta que tengas la cuenta verificada y una versión subida a la pista de prueba cerrada.

---

## 1. Decisiones previas

### 1.1 ¿Con qué tecnología?

| Opción | Ventajas | Desventajas | Veredicto |
|---|---|---|---|
| **Kotlin + Jetpack Compose** (nativo, Android Studio) | Es lo oficial de Google; toda la documentación y los tutoriales apuntan acá; no dependés de terceros | Solo Android | ✅ **Recomendado** |
| Flutter (Dart) | Sirve para Android e iOS con un solo código | Otra cadena de herramientas más para aprender; iOS te obliga a una Mac y a US$ 99/año | Solo si querés iOS también |
| Constructores no-code (Appy Pie, Glide…) | Rápido | Suscripción mensual, poco control, difícil pasar la revisión de Google con una app genérica | ❌ |

Esta guía usa **Kotlin + Jetpack Compose**. Es la opción con menos fricción, y una calculadora es el proyecto ideal para aprender: la app entera cabe en cuatro archivos.

### 1.2 Alcance de la versión 1

Definir el alcance ahora te evita quedarte tres meses "casi terminando". La v1 hace esto:

**Entra:**
- Sueldo nominal mensual
- Situación familiar para FONASA (cónyuge/concubino a cargo, hijos a cargo)
- Hijos a cargo para deducción de IRPF (comunes y con discapacidad), con atribución 100 % o 50 %
- Fondo de Solidaridad / Caja Profesional (opcional)
- Otros descuentos fijos (opcional)

**Sale:**
- Aporte jubilatorio, FONASA, FRL, IRPF, total de descuentos y **sueldo líquido**, con el desglose completo

**Queda para la versión 1.1 (secciones 6B y 6C):**
- Pantalla de configuración
- Datos que se recuerdan al cerrar la app
- Botón al repositorio de GitHub
- Anuncios opcionales, apagados por defecto, activables por niveles
- Valores y tasas fiscales editables por el usuario, con aviso visible y restauración a los oficiales

**Queda para la v2 (anotalo y seguí):**
- Aguinaldo y salario vacacional
- Retención adicional del 6 % que se aplica para anticipar el IRPF del aguinaldo
- Multiempleo (dos trabajos que se acumulan)
- Deducción de alquiler y de cuotas hipotecarias (son crédito de la declaración jurada anual, no de la retención mensual)
- Devolución anual de FONASA e IRPF
- Historial de cálculos guardados

### 1.3 El nombre de la app

Elegí algo descriptivo y **neutral**: por ejemplo *"Sueldo Líquido UY"* o *"Calculadora de Sueldo Uruguay"*.

> ⚠️ **Importante para pasar la revisión**: no uses "BPS", "DGI" ni sus logos en el nombre, el ícono ni las capturas. Google rechaza apps que aparentan ser oficiales de un organismo público (política de *Suplantación de identidad / Tergiversación*). Mencionar en la descripción que el cálculo "sigue los parámetros publicados por BPS y DGI" está bien; llamarla "Calculadora BPS oficial" te la baja.

En esta guía voy a usar:
- Nombre del proyecto: `SueldoLiquidoUY`
- Paquete (identificador único de la app): `uy.tunombre.sueldoliquido` ← **reemplazá `tunombre` por algo propio**. Nunca uses `com.example`, Google lo rechaza y no se puede cambiar después de publicar.

### 1.4 Rango de compatibilidad: de Android 8 hasta hoy

Todo proyecto Android define dos niveles de API, y confundirlos es el malentendido más común:

| Propiedad | Valor en este proyecto | Qué significa |
|---|---|---|
| `minSdk` | **26** | La versión **más vieja** de Android donde la app se puede instalar. API 26 = **Android 8.0 Oreo** (API 27 = Android 8.1). |
| `targetSdk` | **36** | La versión **más nueva** contra la que probaste y cuyo comportamiento moderno adoptás. Google Play la exige en 36 desde el 31/8/2026. |

Que `targetSdk` sea 36 **no** deja afuera a los celulares viejos: Android es retrocompatible. Un equipo con Android 8 instala perfectamente una app que apunta a API 36, siempre que el `minSdk` se lo permita. Los dos números conviven.

**Por qué API 26 es un piso conveniente, más allá de que vos lo hayas pedido:**

- **Íconos adaptativos nativos.** Se introdujeron justamente en API 26. Con `minSdk = 26` no necesitás mantener una versión "legacy" del ícono en paralelo.
- **`java.time` de fábrica.** Si más adelante agregás fechas (historial de cálculos, por ejemplo), no vas a necesitar activar *core library desugaring*. Por debajo de API 26 sí haría falta.
- **Canales de notificación y APIs modernas** ya disponibles, sin condicionales `if (Build.VERSION.SDK_INT >= …)` desparramados por el código.
- Android 8.0 salió en 2017: el costo en alcance de usuarios es prácticamente nulo.

Jetpack Compose funciona desde API 21, así que 26 no te limita en nada de lo que hace esta app. Si algún día quisieras bajar a API 24 (Android 7.0), el código de esta guía funciona igual; simplemente perdés las ventajas de arriba a cambio de un puñado de equipos de 2016.

**La contrapartida de soportar un rango tan amplio es que hay que probar en los dos extremos.** Eso está resuelto en la matriz de compatibilidad de la sección 7.3.

---

## 2. Herramientas, requisitos y costos

### 2.1 Computadora (Ubuntu 26.04)

| Requisito | Mínimo | Recomendado | Cómo verificarlo en Ubuntu |
|---|---|---|---|
| Arquitectura | **x86_64 / amd64** | — | `uname -m` → debe decir `x86_64` |
| glibc | 2.31 o superior | — | `ldd --version` (Ubuntu 26.04 la supera de sobra) |
| RAM | 8 GB | **16 GB** (el emulador come mucho) | `free -h` |
| Disco libre | 20 GB | 40 GB | `df -h ~` |
| Virtualización | VT-x / AMD-V habilitada en BIOS | — | `grep -Eoc '(vmx|svm)' /proc/cpuinfo` → un número mayor a 0 |

> ⚠️ **Android Studio para Linux no soporta CPUs ARM.** Si tu equipo es ARM (poco probable en un desktop/notebook común), el camino cambia por completo.

Si tenés 8 GB de RAM, saltate el emulador y probá directo en tu celular (sección 4.5). Funciona igual de bien y es más rápido.

Comprobación rápida de todo junto, copiá y pegá en la terminal:

```bash
uname -m && ldd --version | head -1 && free -h | head -2 && df -h ~ | tail -1
grep -Eoc '(vmx|svm)' /proc/cpuinfo
```

### 2.2 Software (todo gratis)

| Herramienta | Para qué | Cómo lo instalás en Ubuntu |
|---|---|---|
| **Android Studio** | Donde escribís, ejecutás y compilás la app. Trae el compilador de Kotlin, el SDK, el emulador y Gradle. | Tarball oficial (sección 4.1) |
| **KVM** | Aceleración por hardware del emulador. En Linux es **imprescindible**, sin esto el emulador es inusable. | `sudo apt install qemu-kvm cpu-checker` |
| **Git** | Versionar tu código y poder volver atrás cuando rompas algo | `sudo apt install git` |
| **Cuenta de GitHub** (opcional) | Respaldo en la nube + hospedaje gratis de la política de privacidad | github.com |
| **GIMP / Inkscape** (opcional) | Ícono 512×512 y gráfico destacado 1024×500 | `sudo apt install gimp inkscape` |

Preparación de una sola vez:

```bash
sudo apt update
sudo apt install -y git curl unzip qemu-kvm cpu-checker
```

> **No instales `openjdk` por apt.** Android Studio trae su propio JDK (JetBrains Runtime) y tener otro Java en el sistema es una fuente clásica de conflictos de `JAVA_HOME`. Si ya lo tenés instalado por otra cosa, no pasa nada: simplemente no lo apuntes desde Android Studio.

> **Sobre Snap y Flatpak**: existen paquetes de Android Studio en ambos, pero suelen ir por detrás de la versión oficial y el confinamiento complica el acceso a `/dev/kvm` y a los dispositivos USB. Para desarrollo Android, el tarball oficial de Google es el camino con menos sorpresas.

### 2.3 Dispositivo de prueba

Tu propio celular Android sirve perfecto. Vas a necesitarlo con la depuración USB activada, un cable de **datos** (ojo: hay cables que solo cargan) y, en Linux, las **reglas udev** configuradas para que el sistema te deje hablar con el equipo sin ser root. Todo eso está en la sección 4.5.

Como vas a soportar Android 8 en adelante, lo ideal es probar en al menos dos dispositivos con versiones distintas de Android. Si solo tenés uno, el emulador cubre el resto (sección 7.3).

### 2.4 Cuentas y plata

| Concepto | Costo |
|---|---|
| Android Studio, SDK, emulador | US$ 0 |
| Cuenta de Google (podés usar la que ya tenés) | US$ 0 |
| **Cuenta de desarrollador de Google Play** | **US$ 25, pago único, no reembolsable** |
| Hospedaje de la política de privacidad (GitHub Pages o Google Sites) | US$ 0 |
| **Total** | **US$ 25** |

La tarifa se paga **con tarjeta de crédito o débito a tu nombre legal**. No se aceptan tarjetas prepagas. Es un pago único de por vida, no una suscripción, y te habilita a publicar apps ilimitadas.

---

## 3. La lógica del cálculo (lo más importante)

Esta es la parte que hace valiosa a la app. Si el cálculo está mal, no importa lo linda que sea la pantalla. Leé esta sección con calma: el código de la sección 6 es simplemente esto traducido a Kotlin.

### 3.0 Valores de referencia 2026

| Parámetro | Valor 2026 |
|---|---|
| **BPC** (Base de Prestaciones y Contribuciones) | **$ 6.864** (fijada por decreto, vigente desde el 1/1/2026) |
| 2,5 BPC (umbral de FONASA) | $ 17.160 |
| 7 BPC (mínimo no imponible de IRPF, mensual) | $ 48.048 |
| 15 BPC (umbral de tasa de deducción) | $ 102.960 |
| Tope mensual de asignaciones computables jubilatorias | $ 288.836 |

La BPC se actualiza cada 1° de enero por decreto del Poder Ejecutivo. **Todo el cálculo depende de ella**, por eso en el código va en un solo lugar (ver 6.2).

### 3.1 Aporte jubilatorio (montepío) — 15 %

```
Jubilatorio = min(nominal, 288.836) × 15 %
```

- La tasa personal para trabajadores dependientes es **15 %** del nominal.
- Existe un **tope de cotización**: por encima de $ 288.836 mensuales no se realizan aportes jubilatorios obligatorios. (Ese tope aplica a quienes están en el régimen mixto BPS + AFAP, que es la enorme mayoría de la gente activa hoy.)
- El aporte patronal (7,5 %) **no** aparece en el recibo del trabajador: lo paga el empleador aparte. No va en la app.

### 3.2 FONASA — entre 3 % y 8 %

La tasa depende de **cuánto ganás** y de **tu situación familiar**. Tabla oficial del BPS:

**Si la remuneración es hasta 2,5 BPC ($ 17.160):**

| Situación | Tasa |
|---|---|
| Sin cónyuge/concubino a cargo (con o sin hijos) | 3 % |
| Con cónyuge/concubino a cargo (con o sin hijos) | 5 % |

**Si la remuneración supera 2,5 BPC ($ 17.160):**

| Situación | Tasa |
|---|---|
| Sin cónyuge, sin hijos | 4,5 % |
| Sin cónyuge, con hijos | 6 % |
| Con cónyuge, sin hijos | 6,5 % |
| Con cónyuge, con hijos | 8 % |

Detalles a respetar:
- "Cónyuge o concubino a cargo" cuenta **solo si esa persona no tiene cobertura propia del SNIS**. En la app poné exactamente esa aclaración debajo del switch.
- Para determinar si superás 2,5 BPC se consideran todas las remuneraciones gravadas del mes, **excluido el aguinaldo**.
- FONASA **no tiene tope mensual**: se calcula sobre el nominal completo. (Sí existe un tope anual que genera la *devolución FONASA* al año siguiente; eso queda fuera de la v1.)

### 3.3 FRL (Fondo de Reconversión Laboral) — 0,1 %

```
FRL = nominal × 0,1 %
```

Es el aporte más chico del recibo y también va sobre el nominal completo, sin tope.

### 3.4 IRPF

Acá está la complejidad real. El IRPF de un dependiente se calcula en **dos partes que se restan**:

```
IRPF = máximo( 0 ,  impuesto_por_franjas − crédito_por_deducciones )
```

#### Parte 1: impuesto por franjas (escala progresional mensual 2026)

Es progresiva: cada tramo del sueldo paga su propia tasa, no se aplica una sola tasa a todo.

| Franja | Desde | Hasta | Tasa |
|---|---|---|---|
| Hasta 7 BPC | $ 0 | $ 48.048 | 0 % |
| Más de 7 a 10 BPC | $ 48.049 | $ 68.640 | 10 % |
| Más de 10 a 15 BPC | $ 68.641 | $ 102.960 | 15 % |
| Más de 15 a 30 BPC | $ 102.961 | $ 205.920 | 24 % |
| Más de 30 a 50 BPC | $ 205.921 | $ 343.200 | 25 % |
| Más de 50 a 75 BPC | $ 343.201 | $ 514.800 | 27 % |
| Más de 75 a 115 BPC | $ 514.801 | $ 789.360 | 31 % |
| Más de 115 BPC | $ 789.361 | — | 36 % |

#### Parte 2: crédito por deducciones

Las deducciones **no bajan la base imponible**: se suman, y al total se le aplica un porcentaje que se descuenta directamente del impuesto.

Deducciones admitidas que entran en la v1:

| Concepto | Monto mensual |
|---|---|
| Aporte jubilatorio | lo calculado en 3.1 |
| Aporte FONASA | lo calculado en 3.2 |
| Aporte FRL | lo calculado en 3.3 |
| Por cada hijo menor a cargo (20 BPC anuales) | $ 11.440 al 100 % · $ 5.720 al 50 % |
| Por cada hijo/persona con discapacidad a cargo (40 BPC anuales) | $ 22.880 al 100 % · $ 11.440 al 50 % |
| Fondo de Solidaridad (según categoría) | Cat. 1 $ 286 · Cat. 2 $ 572 · Cat. 3 $ 1.144 · Cat. 4 $ 1.049 · Cat. 5 $ 1.621 |
| Aportes a Caja de Profesionales (CJPPU) o Caja Notarial | según categoría |

Sobre ese total se aplica la **tasa de deducción**:

| Ingresos nominales gravados por IRPF | Tasa |
|---|---|
| ≤ $ 102.960 (15 BPC) | **14 %** |
| > $ 102.960 | **8 %** |

```
crédito = (suma de deducciones) × tasa_de_deducción
```

Sobre la atribución de hijos: la deducción se puede tomar **100 % por uno de los padres o 50 % y 50 %**. Como suele convenir que la tome quien está en la franja más alta, la app debe permitir elegir.

### 3.5 Fórmula final

```
líquido = nominal
        − jubilatorio
        − fonasa
        − frl
        − irpf
        − fondo de solidaridad (si corresponde)
        − caja profesional (si corresponde)
        − otros descuentos
```

### 3.6 Ejemplo completo, paso a paso

**Nominal $ 120.000, sin cónyuge a cargo, sin hijos.**

1. **Jubilatorio**: 120.000 < 288.836 → 120.000 × 15 % = **$ 18.000**
2. **FONASA**: 120.000 > 17.160, sin cónyuge, sin hijos → tasa 4,5 % → **$ 5.400**
3. **FRL**: 120.000 × 0,1 % = **$ 120**
4. **Impuesto por franjas**:
   - primeros 48.048 → 0 % → $ 0
   - de 48.048 a 68.640 = 20.592 → 10 % → $ 2.059,20
   - de 68.640 a 102.960 = 34.320 → 15 % → $ 5.148,00
   - de 102.960 a 120.000 = 17.040 → 24 % → $ 4.089,60
   - **total = $ 11.296,80**
5. **Deducciones**: 18.000 + 5.400 + 120 = $ 23.520. Como 120.000 > 102.960, la tasa es 8 % → crédito = **$ 1.881,60**
6. **IRPF** = 11.296,80 − 1.881,60 = **$ 9.415,20**
7. **Líquido** = 120.000 − 18.000 − 5.400 − 120 − 9.415,20 = **$ 87.064,80**

Guardá este ejemplo: es tu primer caso de prueba.

### 3.7 Limitaciones honestas (van en la app y en la ficha de Play)

Tu calculadora estima; no liquida. Estas cosas hacen que el número real difiera:

- Rubros no gravados (viáticos con tope, partidas específicas de cada convenio)
- Aguinaldo, salario vacacional, horas extra, licencia, retroactivos
- La retención adicional del 6 % que aplican los empleadores para anticipar el IRPF del aguinaldo cuando el sueldo supera 10 BPC
- Multiempleo (cada empleador retiene como si fuera el único ingreso)
- Créditos que se reclaman en la declaración jurada anual (alquiler 6 %, cuotas hipotecarias)
- Devolución anual de FONASA y de IRPF
- Convenios colectivos, seguros de salud complementarios, préstamos, embargos

Poné un aviso visible: *"Cálculo estimativo con parámetros 2026. No sustituye la liquidación de tu empleador ni la información oficial de BPS/DGI."*

---

## 4. Instalar y configurar el entorno

### 4.1 Instalar Android Studio en Ubuntu 26.04

1. Andá a **https://developer.android.com/studio** y descargá la versión **estable** para **Linux** (`android-studio-XXXX.X.X.X-linux.tar.gz`, entre 1 y 1,5 GB). Aceptá los términos.

2. Descomprimilo en `/opt` (ubicación estándar para software de terceros):

```bash
cd ~/Descargas
sudo tar -xzf android-studio-*-linux.tar.gz -C /opt
```

3. Ejecutalo por primera vez desde la terminal:

```bash
/opt/android-studio/bin/studio.sh
```

> La primera vez **arrancalo siempre desde la terminal**. Si algo falla (falta una librería, un problema de permisos, un error de Wayland), el mensaje aparece ahí y no en ningún lado más.

4. **Ubuntu 26.04 es Wayland puro**: GNOME 50 eliminó la sesión de X11. Android Studio está basado en IntelliJ y corre a través de **XWayland**, que sigue disponible y funciona bien. Si en un monitor HiDPI ves las fuentes borrosas, tenés dos salidas:
   - Ajustar el escalado del IDE en `Ayuda → Cambiar tamaño de fuente del IDE`, o
   - Probar el soporte nativo de Wayland (experimental) agregando `-Dawt.toolkit.name=WLToolkit` en `Ayuda → Editar opciones de VM personalizadas`.

5. **Subí el límite de archivos vigilados.** IntelliJ te va a avisar de esto tarde o temprano en Linux; mejor resolverlo antes:

```bash
echo "fs.inotify.max_user_watches = 524288" | sudo tee /etc/sysctl.d/60-inotify.conf
sudo sysctl -p /etc/sysctl.d/60-inotify.conf
```

6. **Creá el lanzador del menú de aplicaciones** una vez que el IDE esté abierto: menú **Tools → Create Desktop Entry**. Después vas a poder abrirlo desde el menú de GNOME como cualquier otra aplicación.

**Actualizaciones**: con el tarball, Android Studio se actualiza solo desde `Help → Check for Updates`. Como está en `/opt`, la primera vez te va a pedir la contraseña de sudo o va a caer a una actualización manual; si eso te molesta, movelo a tu carpeta personal (`~/android-studio`) en lugar de `/opt` y el auto-update funciona sin permisos especiales.

### 4.2 Primer arranque (Setup Wizard)

Al abrirlo por primera vez:

1. *"Import Settings"* → **Do not import settings**.
2. Aceptá o rechazá el envío de estadísticas, da igual.
3. Tipo de instalación → **Standard**.
4. Elegí tema claro u oscuro.
5. En "Verify Settings" te muestra qué va a descargar: el **Android SDK**, las **Platform Tools** y la **imagen del emulador**. Aceptá las licencias y dale Finish.
6. Esperá. Son otros 3–6 GB de descarga. Andá a hacer otra cosa.

Cuando termina, aparece la pantalla de bienvenida con el botón **New Project**.

**Dónde quedó cada cosa en Ubuntu:**

| Qué | Ruta |
|---|---|
| El IDE | `/opt/android-studio/` |
| El SDK de Android | `~/Android/Sdk/` |
| `adb` y demás herramientas | `~/Android/Sdk/platform-tools/` |
| Configuración del IDE | `~/.config/Google/AndroidStudio*/` |
| Caché de Gradle | `~/.gradle/` |

Agregá las herramientas al PATH para poder usar `adb` desde cualquier terminal:

```bash
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator' >> ~/.bashrc
source ~/.bashrc
adb --version
```

### 4.3 Instalar el SDK que vas a necesitar

Google Play exige un nivel de API mínimo que sube todos los años. **Desde el 31 de agosto de 2026, las apps nuevas y las actualizaciones deben apuntar a Android 16 (API 36) o superior.** Como estás empezando ahora, arrancá directo con API 36.

1. En la pantalla de bienvenida: **More Actions → SDK Manager** (o menú `Tools → SDK Manager` si ya tenés un proyecto abierto).
2. Pestaña **SDK Platforms** → tildá **Android 16.0 (API 36)**.
3. En esa misma pestaña, tildá abajo **Show Package Details** y agregá también **Android 8.0 (API 26)**. Como tu `minSdk` es 26, vas a querer una imagen de sistema de esa versión para probar el extremo viejo del rango.
4. Pestaña **SDK Tools** → verificá que estén tildados:
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
   - Android Emulator
   - Android SDK Command-line Tools (latest)

   (En Linux **no** existe HAXM ni el "Intel Emulator Accelerator": la aceleración la da KVM, que instalás aparte en el paso 4.4.)
5. **Apply** → aceptar licencias → esperar.

### 4.4 KVM y emuladores

En Linux, la aceleración por hardware del emulador la provee **KVM**. Sin KVM el emulador arranca en modo software y es tan lento que no sirve para nada. Este paso no es opcional.

**Paso 1 — Instalar y habilitar KVM:**

```bash
# 1. Confirmar que el procesador soporta virtualización
grep -Eoc '(vmx|svm)' /proc/cpuinfo        # tiene que dar un número > 0

# 2. Instalar KVM
sudo apt update
sudo apt install -y qemu-kvm cpu-checker

# 3. Verificar
kvm-ok
# Salida esperada: "INFO: /dev/kvm exists" y "KVM acceleration can be used"

# 4. Darle permiso a tu usuario (sin esto el emulador falla con "/dev/kvm permission denied")
sudo adduser $USER kvm

# 5. Cerrá sesión y volvé a entrar (o abrí una shell nueva con el grupo aplicado)
newgrp kvm

# 6. Comprobar
ls -l /dev/kvm      # debería mostrar   crw-rw---- 1 root kvm
groups              # tiene que aparecer "kvm" en la lista
```

Si `grep` devuelve `0`, la virtualización está apagada en la BIOS/UEFI: reiniciá, entrá al setup y activá **Intel VT-x** o **AMD-V** (a veces figura como "SVM Mode").

Si `kvm-ok` dice que KVM no puede usarse pero la BIOS está bien, revisá que no tengas VirtualBox o VMware corriendo: se pelean por el módulo de virtualización.

```bash
lsmod | grep -E "(vbox|vmware)"     # si aparece algo, descargá esos módulos
```

**Paso 2 — Crear dos emuladores.** Como soportás de Android 8 a Android 16, necesitás probar los dos extremos.

`Tools → Device Manager → Create Virtual Device`:

| AVD | Dispositivo | Imagen de sistema | Para qué |
|---|---|---|---|
| `Pixel7_API36` | Pixel 7 | **API 36** (Android 16), x86_64 | Probar el comportamiento moderno |
| `Pixel2_API26` | Pixel 2 | **API 26** (Android 8.0), x86_64 | Probar el piso de compatibilidad |

Para cada uno: elegir el dispositivo → elegir la imagen (descargarla si dice "Download") → ponerle nombre → **Finish** → probar con ▶. La primera vez tarda un par de minutos en arrancar.

> Elegí siempre imágenes **x86_64**, no ARM. Las ARM se emulan por software y son lentísimas en una PC común.

Si el emulador se queja de gráficos en Wayland, arrancalo desde terminal forzando el renderizado por software:

```bash
emulator @Pixel2_API26 -gpu swiftshader_indirect
```

Y si el emulador se niega a arrancar sin dar razón, revisá el espacio en disco: por debajo de 5 GB libres se planta sin mensaje claro.

### 4.5 Preparar tu celular como dispositivo de prueba

Es la opción más rápida y la más fiel a la realidad.

**Paso 0 — Reglas udev (esto es específico de Linux y es la causa nº 1 de que el celular "no aparezca").**

En Ubuntu, un usuario común no tiene permiso para hablar con dispositivos USB arbitrarios. Hay un paquete oficial que instala las reglas de todos los fabricantes conocidos:

```bash
sudo apt install -y android-sdk-platform-tools-common
sudo usermod -aG plugdev $USER
```

Cerrá sesión y volvé a entrar para que el grupo `plugdev` tome efecto.

Si tu equipo igual no aparece, agregá la regla a mano. Primero averiguá el ID de fabricante con el celular conectado:

```bash
lsusb
# Buscá tu equipo. El primer bloque de 4 dígitos hexadecimales es el idVendor.
# Xiaomi = 2717 · Samsung = 04e8 · Motorola = 22b8 · Google = 18d1 · Huawei = 12d1
```

Y creá la regla (reemplazá `2717` por el tuyo):

```bash
echo 'SUBSYSTEM=="usb", ATTR{idVendor}=="2717", MODE="0666", GROUP="plugdev"' \
  | sudo tee /etc/udev/rules.d/51-android.rules
sudo chmod a+r /etc/udev/rules.d/51-android.rules
sudo udevadm control --reload-rules
sudo udevadm trigger
```

Desconectá y volvé a conectar el cable.

1. En el celular: **Ajustes → Acerca del teléfono** → tocá **7 veces** sobre "Número de compilación" (en algunas capas se llama "Versión de MIUI" o similar). Aparece "Ya eres desarrollador".
2. Volvé a **Ajustes → Sistema → Opciones de desarrollador** y activá **Depuración por USB**.
3. Conectá el cable a la PC. En el celular va a aparecer *"¿Permitir depuración USB?"* → **Permitir siempre desde esta computadora**.
4. En Android Studio, tu dispositivo debería aparecer en el desplegable de arriba a la derecha.

> **Nota para Xiaomi (MIUI/HyperOS)**: además de "Depuración por USB" tenés que activar **"Instalar vía USB"** dentro de Opciones de desarrollador, y para que esa opción se habilite el equipo debe estar con sesión iniciada en una cuenta Mi. Si no lo hacés, Android Studio compila bien pero la instalación falla con un error tipo `INSTALL_FAILED_USER_RESTRICTED`. También conviene desactivar "Optimización MIUI" si te siguen fallando las instalaciones.

Si el dispositivo no aparece, diagnosticá desde la terminal en este orden:

```bash
lsusb          # ¿el sistema ve el hardware? Si no aparece: cable o puerto.
adb devices    # ¿adb lo ve?
```

Lecturas de `adb devices`:

| Salida | Qué pasa | Solución |
|---|---|---|
| Lista vacía | El celular no está en modo depuración, o faltan reglas udev | Revisar paso 0 y que "Depuración por USB" esté activa |
| `????????????  no permissions` | Reglas udev mal o falta grupo `plugdev` | Rehacer el paso 0 y volver a iniciar sesión |
| `XXXX  unauthorized` | Falta aceptar el diálogo en el celular | Desconectar, en el celular ir a Opciones de desarrollador → **Revocar autorizaciones de depuración USB**, reconectar y aceptar |
| `XXXX  device` | ✅ Todo bien | — |

Si nada funciona, reiniciá el servidor de adb:

```bash
adb kill-server && adb start-server && adb devices
```

Y probá otro cable: en Linux, como en cualquier sistema, un cable de solo carga no transmite datos.

---

## 5. Crear el proyecto y entender su estructura

### 5.1 New Project

1. **New Project** → plantilla **Empty Activity** (la que dice Compose; no elijas "Empty Views Activity").
2. Completá:
   - **Name**: `SueldoLiquidoUY`
   - **Package name**: `uy.tunombre.sueldoliquido`
   - **Save location**: donde quieras (evitá rutas con espacios o tildes)
   - **Language**: Kotlin
   - **Minimum SDK**: **API 26 (Android 8.0 Oreo)** — el piso que definimos en la sección 1.4
   - **Build configuration language**: Kotlin DSL (`build.gradle.kts`)
3. **Finish**. La primera sincronización de Gradle descarga dependencias y tarda varios minutos. Esperá a que la barra de abajo deje de moverse.

Probá que todo funcione: presioná ▶ (Run). Debería aparecer una pantalla en blanco que dice "Hello Android!". Si llegaste acá, lo más difícil de la infraestructura ya pasó.

### 5.2 Qué es cada cosa

Cambiá la vista del panel izquierdo a **Android** (desplegable arriba del árbol de archivos):

```
app/
├── manifests/
│   └── AndroidManifest.xml      ← permisos, nombre, ícono, actividad de inicio
├── kotlin+java/
│   └── uy.tunombre.sueldoliquido/
│       ├── MainActivity.kt      ← la pantalla
│       └── ui/theme/            ← colores y tipografía (generados por la plantilla)
├── res/
│   ├── drawable/                ← imágenes vectoriales
│   ├── mipmap/                  ← el ícono de la app
│   ├── values/
│   │   ├── strings.xml          ← todos los textos
│   │   └── themes.xml
└── Gradle Scripts/
    ├── build.gradle.kts (Module :app)   ← versiones de SDK, dependencias
    ├── build.gradle.kts (Project)
    └── libs.versions.toml               ← catálogo de versiones de librerías
```

Conceptos mínimos para no perderte:

- **Gradle** es el sistema que compila. Cuando tocás un `build.gradle.kts` te va a pedir *"Sync Now"*: siempre aceptá.
- **Composable**: una función marcada con `@Composable` que describe un pedazo de pantalla. Compose redibuja solo cuando cambia el estado.
- **Estado (`remember { mutableStateOf(...) }`)**: una variable que, al cambiar, hace que la pantalla se redibuje sola.
- **Separación por capas**: la lógica del cálculo va en un paquete `dominio`, sin nada de Android adentro. Eso permite testearla sin abrir el emulador. Es la decisión de diseño más importante de todo el proyecto.

---

## 6. El código completo

Vas a crear cuatro archivos y modificar dos. Para crear un archivo: clic derecho sobre el paquete → **New → Kotlin Class/File → File**.

### 6.1 `app/build.gradle.kts` (módulo :app)

Ajustá el bloque `android` para que quede así (el resto del archivo dejalo como está):

```kotlin
android {
    namespace = "uy.tunombre.sueldoliquido"
    compileSdk = 36

    defaultConfig {
        applicationId = "uy.tunombre.sueldoliquido"
        minSdk = 26        // Android 8.0 Oreo — el equipo más viejo que soportamos
        targetSdk = 36     // Android 16 — exigido por Google Play
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}
```

Verificá también que en `dependencies` exista `testImplementation(libs.junit)`. La plantilla ya lo incluye.

Después de editar: **Sync Now**.

> **Nota de compatibilidad**: todo el código de esta guía funciona sin cambios desde API 26. Jetpack Compose requiere API 21; `Locale.forLanguageTag` existe desde API 21; `NumberFormat` es parte de Java desde siempre. No hay ni una sola línea que necesite un `if (Build.VERSION.SDK_INT >= …)`.
>
> Si en el futuro agregás algo, la regla es simple: cuando uses una API introducida después de Android 8, Android Studio te la subraya en rojo con el mensaje *"Call requires API level X (current min is 26)"*. Ahí tenés que decidir entre usar la alternativa de AndroidX (casi siempre existe) o poner el condicional de versión. **No subas el `minSdk` para callar el error**: eso deja gente afuera sin que te des cuenta.

### 6.2 `dominio/Parametros.kt`

Creá el paquete: clic derecho sobre `uy.tunombre.sueldoliquido` → New → Package → `dominio`. Adentro, este archivo.

**Este es el único archivo que vas a tocar cada enero.**

> Si pensás hacer la sección **6C** (parámetros editables por el usuario), este archivo va a cambiar de forma: las constantes se convierten en propiedades de un objeto que se puede modificar en tiempo de ejecución. Podés escribirlo así ahora y refactorizarlo después —el refactor está explicado paso a paso— o saltar directo a 6C.3 si ya sabés que lo vas a hacer.

```kotlin
package uy.tunombre.sueldoliquido.dominio

/**
 * Parámetros fiscales del ejercicio. Se actualizan una vez por año,
 * cuando el Poder Ejecutivo fija el nuevo valor de la BPC (1° de enero).
 * Fuentes: BPS (tasas y escalas IRPF) y DGI.
 */
object Parametros {

    const val EJERCICIO = 2026

    /** Base de Prestaciones y Contribuciones vigente. */
    const val BPC = 6_864.0

    // ---------- Aportes personales ----------
    const val TASA_JUBILATORIA = 0.15
    const val TASA_FRL = 0.001

    /** Tope mensual de asignaciones computables jubilatorias. */
    const val TOPE_JUBILATORIO = 288_836.0

    // ---------- FONASA ----------
    /** Umbral que separa las dos tablas de tasas: 2,5 BPC. */
    const val UMBRAL_FONASA = 2.5 * BPC

    // ---------- IRPF ----------
    /** Escala progresional mensual. `hasta` es el límite superior de cada franja. */
    val ESCALA_IRPF: List<Franja> = listOf(
        Franja(hasta = 7.0 * BPC, tasa = 0.00),
        Franja(hasta = 10.0 * BPC, tasa = 0.10),
        Franja(hasta = 15.0 * BPC, tasa = 0.15),
        Franja(hasta = 30.0 * BPC, tasa = 0.24),
        Franja(hasta = 50.0 * BPC, tasa = 0.25),
        Franja(hasta = 75.0 * BPC, tasa = 0.27),
        Franja(hasta = 115.0 * BPC, tasa = 0.31),
        Franja(hasta = Double.MAX_VALUE, tasa = 0.36)
    )

    /** Por encima de 15 BPC la tasa de deducción baja de 14 % a 8 %. */
    const val UMBRAL_TASA_DEDUCCION = 15.0 * BPC
    const val TASA_DEDUCCION_ALTA = 0.14
    const val TASA_DEDUCCION_BAJA = 0.08

    /** Deducción mensual por hijo a cargo: 20 BPC anuales. */
    const val DEDUCCION_HIJO = 20.0 * BPC / 12.0

    /** Hijo o persona a cargo con discapacidad: 40 BPC anuales. */
    const val DEDUCCION_HIJO_DISCAPACIDAD = 40.0 * BPC / 12.0
}

data class Franja(val hasta: Double, val tasa: Double)
```

### 6.3 `dominio/Modelos.kt`

```kotlin
package uy.tunombre.sueldoliquido.dominio

data class Entrada(
    val nominal: Double = 0.0,
    /** Cónyuge o concubino a cargo SIN cobertura propia del SNIS. */
    val conyugeACargo: Boolean = false,
    val hijos: Int = 0,
    val hijosConDiscapacidad: Int = 0,
    /** 1.0 = tomo el 100 % de la deducción; 0.5 = la comparto con el otro padre. */
    val atribucionHijos: Double = 1.0,
    val fondoSolidaridad: Double = 0.0,
    val cajaProfesional: Double = 0.0,
    val otrosDescuentos: Double = 0.0,
    /** Casi todos los activos están en el régimen mixto, donde el tope aplica. */
    val aplicaTopeJubilatorio: Boolean = true
)

data class Resultado(
    val nominal: Double,
    val jubilatorio: Double,
    val fonasa: Double,
    val tasaFonasa: Double,
    val frl: Double,
    val impuestoPorFranjas: Double,
    val totalDeducciones: Double,
    val tasaDeduccion: Double,
    val creditoDeducciones: Double,
    val irpf: Double,
    val otrosDescuentos: Double,
    val totalDescuentos: Double,
    val liquido: Double
)
```

### 6.4 `dominio/Calculadora.kt`

```kotlin
package uy.tunombre.sueldoliquido.dominio

import kotlin.math.max
import kotlin.math.min

object Calculadora {

    fun calcular(e: Entrada): Resultado {
        val nominal = max(0.0, e.nominal)

        // 1. Aporte jubilatorio (con tope de cotización)
        val baseJubilatoria =
            if (e.aplicaTopeJubilatorio) min(nominal, Parametros.TOPE_JUBILATORIO) else nominal
        val jubilatorio = baseJubilatoria * Parametros.TASA_JUBILATORIA

        // 2. FONASA
        val tieneHijos = (e.hijos + e.hijosConDiscapacidad) > 0
        val tasaFonasa = tasaFonasa(nominal, tieneHijos, e.conyugeACargo)
        val fonasa = nominal * tasaFonasa

        // 3. FRL
        val frl = nominal * Parametros.TASA_FRL

        // 4. IRPF
        val impuesto = impuestoPorFranjas(nominal)

        val deduccionHijos =
            (e.hijos * Parametros.DEDUCCION_HIJO +
             e.hijosConDiscapacidad * Parametros.DEDUCCION_HIJO_DISCAPACIDAD) * e.atribucionHijos

        val totalDeducciones =
            jubilatorio + fonasa + frl + deduccionHijos + e.fondoSolidaridad + e.cajaProfesional

        val tasaDeduccion =
            if (nominal <= Parametros.UMBRAL_TASA_DEDUCCION) Parametros.TASA_DEDUCCION_ALTA
            else Parametros.TASA_DEDUCCION_BAJA

        val credito = totalDeducciones * tasaDeduccion
        val irpf = max(0.0, impuesto - credito)

        // 5. Líquido
        val otros = e.fondoSolidaridad + e.cajaProfesional + e.otrosDescuentos
        val totalDescuentos = jubilatorio + fonasa + frl + irpf + otros

        return Resultado(
            nominal = nominal,
            jubilatorio = jubilatorio,
            fonasa = fonasa,
            tasaFonasa = tasaFonasa,
            frl = frl,
            impuestoPorFranjas = impuesto,
            totalDeducciones = totalDeducciones,
            tasaDeduccion = tasaDeduccion,
            creditoDeducciones = credito,
            irpf = irpf,
            otrosDescuentos = otros,
            totalDescuentos = totalDescuentos,
            liquido = nominal - totalDescuentos
        )
    }

    /** Tabla oficial de tasas personales de FONASA. */
    fun tasaFonasa(nominal: Double, tieneHijos: Boolean, conyugeACargo: Boolean): Double =
        if (nominal <= Parametros.UMBRAL_FONASA) {
            if (conyugeACargo) 0.05 else 0.03
        } else {
            when {
                conyugeACargo && tieneHijos -> 0.08
                conyugeACargo && !tieneHijos -> 0.065
                !conyugeACargo && tieneHijos -> 0.06
                else -> 0.045
            }
        }

    /** Aplica la escala progresional: cada tramo tributa a su propia tasa. */
    fun impuestoPorFranjas(renta: Double): Double {
        var restante = renta
        var pisoAnterior = 0.0
        var total = 0.0
        for (franja in Parametros.ESCALA_IRPF) {
            if (restante <= 0.0) break
            val ancho = franja.hasta - pisoAnterior
            val gravado = min(restante, ancho)
            total += gravado * franja.tasa
            restante -= gravado
            pisoAnterior = franja.hasta
        }
        return total
    }
}
```

### 6.5 `MainActivity.kt` (la pantalla)

Reemplazá **todo** el contenido del archivo por esto.

> El nombre del tema (`SueldoLiquidoUYTheme`) lo generó la plantilla a partir del nombre del proyecto. Si llamaste distinto al proyecto, Android Studio te va a marcar el import en rojo: poné el cursor encima y aceptá la sugerencia con `Alt+Enter`.

```kotlin
package uy.tunombre.sueldoliquido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import uy.tunombre.sueldoliquido.dominio.Calculadora
import uy.tunombre.sueldoliquido.dominio.Entrada
import uy.tunombre.sueldoliquido.dominio.Parametros
import uy.tunombre.sueldoliquido.dominio.Resultado
import uy.tunombre.sueldoliquido.ui.theme.SueldoLiquidoUYTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SueldoLiquidoUYTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    PantallaCalculadora(Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun PantallaCalculadora(modifier: Modifier = Modifier) {
    var nominal by remember { mutableStateOf("") }
    var conyuge by remember { mutableStateOf(false) }
    var hijos by remember { mutableStateOf("") }
    var hijosDisc by remember { mutableStateOf("") }
    var mitad by remember { mutableStateOf(false) }
    var fondo by remember { mutableStateOf("") }
    var caja by remember { mutableStateOf("") }
    var otros by remember { mutableStateOf("") }

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
    val r: Resultado = Calculadora.calcular(entrada)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Sueldo líquido UY",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Parámetros ${Parametros.EJERCICIO} · BPC ${moneda(Parametros.BPC)}",
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
        Text("Opcionales", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        CampoNumerico("Fondo de Solidaridad (mensual)", fondo) { fondo = it }
        CampoNumerico("Caja profesional (mensual)", caja) { caja = it }
        CampoNumerico("Otros descuentos", otros) { otros = it }

        Spacer(Modifier.height(24.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Detalle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                Fila("Nominal", moneda(r.nominal))
                Fila("Jubilatorio (15 %)", "− " + moneda(r.jubilatorio))
                Fila("FONASA (${porcentaje(r.tasaFonasa)})", "− " + moneda(r.fonasa))
                Fila("FRL (0,1 %)", "− " + moneda(r.frl))
                Fila("IRPF", "− " + moneda(r.irpf))
                if (r.otrosDescuentos > 0) Fila("Otros descuentos", "− " + moneda(r.otrosDescuentos))

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
                Text("Cómo se calculó el IRPF", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Fila("Impuesto por franjas", moneda(r.impuestoPorFranjas))
                Fila("Deducciones computables", moneda(r.totalDeducciones))
                Fila("Crédito (${porcentaje(r.tasaDeduccion)} de las deducciones)", "− " + moneda(r.creditoDeducciones))
                Fila("IRPF a retener", moneda(r.irpf))
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Cálculo estimativo con los parámetros vigentes ${Parametros.EJERCICIO}. " +
                "No sustituye la liquidación de tu empleador ni la información oficial de BPS y DGI. " +
                "No contempla aguinaldo, salario vacacional, horas extra, multiempleo, " +
                "partidas no gravadas ni créditos de la declaración jurada anual.",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CampoNumerico(etiqueta: String, valor: String, onChange: (String) -> Unit) {
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
private fun FilaSwitch(titulo: String, subtitulo: String, valor: Boolean, onChange: (Boolean) -> Unit) {
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
private fun Fila(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            etiqueta,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ---------- utilidades ----------

private val formatoNumero: NumberFormat =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("es-UY")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

private fun moneda(valor: Double): String = "$ " + formatoNumero.format(valor)

private fun porcentaje(tasa: Double): String {
    val v = tasa * 100
    return if (v % 1.0 == 0.0) "${v.toInt()} %" else String.format(Locale.US, "%.1f %%", v).replace(".", ",")
}

private fun String.aNumero(): Double = toDoubleOrNull() ?: 0.0
private fun String.aEntero(): Int = toIntOrNull() ?: 0
```

### 6.6 `res/values/strings.xml`

```xml
<resources>
    <string name="app_name">Sueldo Líquido UY</string>
</resources>
```

### 6.7 Ejecutar

Presioná ▶. Escribí `120000` en el campo de nominal y verificá que el líquido dé **$ 87.065** (redondeado). Si te da eso, la lógica está bien.

---

## 6B. Ampliación: configuración persistente, GitHub y anuncios opcionales

Esta parte convierte la app de una sola pantalla en una app de dos pantallas, con datos que sobreviven al cierre, un enlace al repositorio y un sistema de anuncios que **arranca apagado** y que el usuario puede encender por niveles.

**Hacé todo lo anterior primero.** Esta ampliación asume que la versión 1.0 ya te compila y que los tests pasan. Si algo se rompe acá, vas a querer saber que el problema está en lo nuevo.

### 6B.1 Qué vamos a construir

```
┌──────────────────────┐                    ┌──────────────────────┐
│  Pantalla            │   toca el engra-   │  Pantalla            │
│  calculadora         │   naje ⚙           │  configuración       │
│                      │ ─────────────────► │                      │
│  • campos            │                    │  • recordar datos    │
│  • resultado         │ ◄───────────────── │  • borrar datos      │
│  • ⚙ arriba a la     │   botón atrás      │  • nivel de anuncios │
│    derecha           │                    │  • ver anuncio ahora │
└──────────────────────┘                    │  • GitHub            │
           ▲                                │  • versión / aviso   │
           │                                └──────────────────────┘
           │  lee al abrir / escribe al editar
           ▼
┌──────────────────────────────────────────────────────────────────┐
│  DataStore  ·  archivo ajustes.preferences_pb en la app          │
│  nominal · cónyuge · hijos · … · nivel_anuncios · recordar       │
│  comparador_hasta · ultima_apertura · primera_sesion             │
└──────────────────────────────────────────────────────────────────┘
```

Cuatro piezas nuevas:

| Pieza | Para qué | Archivo |
|---|---|---|
| **DataStore** | Guardar datos que sobreviven al cierre de la app | `datos/Ajustes.kt` |
| **Navigation Compose** | Moverse entre las dos pantallas y manejar el botón atrás | dentro de `MainActivity.kt` |
| **Pantalla de configuración** | Ajustes, GitHub, control de anuncios | `PantallaConfiguracion.kt` |
| **AdMob** | Los anuncios | `anuncios/GestorAnuncios.kt` |

### 6B.2 Agregar las dependencias

En `app/build.gradle.kts`, dentro del bloque `dependencies` que ya existe, agregá estas líneas antes del cierre:

```kotlin
dependencies {
    // … todo lo que ya estaba …

    // Guardar datos que sobreviven al cierre de la app
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Navegación entre pantallas
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Íconos de Material (Icons.Filled.Settings, ArrowBack, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    // Anuncios (podés agregarla recién cuando llegues a 6B.11)
    implementation("com.google.android.gms:play-services-ads:25.4.0")
}
```

Y en el bloque `android` del mismo archivo, habilitá `BuildConfig`, que la pantalla de configuración usa para mostrar la versión:

```kotlin
android {
    // … lo que ya estaba …
    buildFeatures {
        compose = true
        buildConfig = true     // ← agregá esta línea
    }
}
```

**Sync Now** y esperá la descarga.

Dos notas sobre esto:

- **Los números de versión envejecen.** Si Android Studio te subraya una en amarillo diciendo "newer version available", aceptá la sugerencia con `Alt+Enter`. Si Gradle no logra resolver alguna, buscá la versión vigente en la documentación oficial de esa librería.
- **Los íconos van sin versión** porque los maneja el BOM de Compose que ya trae la plantilla. **Esta dependencia no es opcional**: desde hace unas versiones, Material 3 dejó de arrastrar `material-icons-core` de forma transitiva, así que sin esta línea todos los `import androidx.compose.material.icons.*` fallan con *Unresolved reference 'icons'*. Como el paquete además está marcado como obsoleto, la alternativa a futuro es bajar los SVG de Material Symbols e importarlos con `New → Vector Asset`, pero para arrancar la dependencia es lo práctico.
- El SDK de anuncios versión 24 en adelante **exige `minSdk` 23 o superior**. Vos tenés 26, así que estás cubierto. Es una de las razones por las que Android 8 como piso fue una buena decisión.

### 6B.3 Guardar datos que sobreviven al cierre

Android tiene dos mecanismos para guardar datos chicos: el viejo `SharedPreferences` y el moderno **DataStore**. Usamos DataStore: es asíncrono (no bloquea la interfaz), maneja errores de lectura, y es lo que Google recomienda hoy.

Creá el paquete `datos` (clic derecho sobre `uy.tunombre.sueldoliquido` → New → Package → `datos`) y adentro el archivo `Ajustes.kt`:

```kotlin
package uy.tunombre.sueldoliquido.datos

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
import java.io.IOException

/**
 * Crea (una sola vez para toda la app) el archivo donde se guardan los ajustes.
 * Queda en /data/data/uy.tunombre.sueldoliquido/files/datastore/ajustes.preferences_pb
 * y solo tu app puede leerlo. Se borra cuando el usuario desinstala o limpia datos.
 */
private val Context.almacen: DataStore<Preferences> by preferencesDataStore(name = "ajustes")

/**
 * Cuánta publicidad acepta ver el usuario. Los cuatro niveles son acumulativos
 * y cada uno usa un formato que Google admite para ese lugar concreto.
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
    val huboPrimeraSesion: Boolean = false
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
                huboPrimeraSesion = p[Claves.PRIMERA_SESION] ?: false
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
```

**Cuatro cosas que vale la pena entender de este archivo:**

1. **`by preferencesDataStore(...)` va a nivel de archivo, nunca dentro de una clase.** Si lo ponés adentro, la app crashea con "There are multiple DataStores active for the same file" en cuanto se cree una segunda instancia.
2. **El `.catch { }` no es adorno.** Si el archivo se corrompe (batería que se corta a mitad de una escritura), sin ese bloque la app crashea al abrir. Con él, arranca con los valores por defecto.
3. **Guardo `hijos` como texto, no como número.** Así el campo puede quedar vacío en pantalla en lugar de mostrar un `0` que el usuario tiene que borrar. La conversión a número la sigue haciendo la calculadora.
4. **`ultimaApertura` y `huboPrimeraSesion` existen por motivos de política, no de funcionalidad.** Son los que impiden que el anuncio de apertura se repita demasiado o aparezca en la primera experiencia del usuario. En 6B.10 está el porqué.

### 6B.4 Navegar entre dos pantallas

Reemplazá **todo** el contenido de `MainActivity.kt` por esto. El código de `PantallaCalculadora` que ya tenías se mueve a su propio archivo en el paso siguiente.

```kotlin
package uy.tunombre.sueldoliquido

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
import uy.tunombre.sueldoliquido.anuncios.GestorAnuncios
import uy.tunombre.sueldoliquido.datos.RepositorioAjustes
import uy.tunombre.sueldoliquido.ui.theme.SueldoLiquidoUYTheme

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
        if (actuales.nivelAnuncios != uy.tunombre.sueldoliquido.datos.NivelAnuncios.NINGUNO) {
            GestorAnuncios.iniciar(contexto)
        }
    }

    val navegador = rememberNavController()

    NavHost(navController = navegador, startDestination = "calculadora") {
        composable("calculadora") {
            PantallaCalculadora(
                repositorio = repositorio,
                ajustes = actuales,
                onAbrirConfiguracion = { navegador.navigate("configuracion") }
            )
        }
        composable("configuracion") {
            PantallaConfiguracion(
                repositorio = repositorio,
                ajustes = actuales,
                onVolver = { navegador.popBackStack() }
            )
        }
    }
}
```

`collectAsStateWithLifecycle` viene de `androidx.lifecycle:lifecycle-runtime-compose`, que ya entra como dependencia transitiva de la plantilla. Si Android Studio no lo encuentra, agregá `implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")` y sincronizá. La diferencia con el `collectAsState` común es que este deja de escuchar el flujo cuando la app pasa a segundo plano: menos batería, mismo resultado.

El botón atrás del sistema funciona solo. `NavHost` lo maneja: estando en configuración, atrás vuelve a la calculadora; estando en la calculadora, atrás cierra la app.

> **Fijate en lo que NO hay acá**: la navegación a configuración no muestra ningún anuncio, en ningún nivel. Es deliberado y está explicado en 6B.10. La pantalla donde el usuario apaga la publicidad tiene que ser siempre gratuita de llegar.

### 6B.5 La calculadora, con datos precargados y hueco para el banner

Creá `PantallaCalculadora.kt` en el paquete raíz y mové ahí la pantalla, con estos cambios:

```kotlin
package uy.tunombre.sueldoliquido

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import uy.tunombre.sueldoliquido.anuncios.BannerPublicitario
import uy.tunombre.sueldoliquido.datos.Ajustes
import uy.tunombre.sueldoliquido.datos.RepositorioAjustes
import uy.tunombre.sueldoliquido.dominio.Calculadora
import uy.tunombre.sueldoliquido.dominio.Entrada

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaCalculadora(
    repositorio: RepositorioAjustes,
    ajustes: Ajustes,
    onAbrirConfiguracion: () -> Unit
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
    val r = Calculadora.calcular(entrada)

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
            // … acá va exactamente el mismo contenido que tenías en la versión 1.0,
            // desde el subtítulo con la BPC hasta el aviso legal, sin el título grande
            // (ahora está en la barra superior).
        }
    }
}
```

#### Y ahora el paso que se saltea todo el mundo: `Componentes.kt`

En la versión 1.0, las funciones auxiliares vivían dentro de `MainActivity.kt` y estaban marcadas como `private`. Al partir la app en varias pantallas, **dejan de ser visibles** y vas a comer una tanda de errores `Unresolved reference 'aNumero'`, `'aEntero'`, `'CampoNumerico'`, `'Fila'`, `'moneda'`.

Creá `Componentes.kt` en el paquete raíz y mové ahí las siete funciones, **sin el modificador `private`**:

```kotlin
package uy.tunombre.sueldoliquido

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

@Composable
fun CampoNumerico(etiqueta: String, valor: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valor,
        onValueChange = { texto -> onChange(texto.filter { it.isDigit() }.take(9)) },
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

@Composable
fun FilaSwitch(titulo: String, subtitulo: String, valor: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
fun Fila(etiqueta: String, valor: String, destacado: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
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

private val formatoNumero: NumberFormat =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("es-UY")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

fun moneda(valor: Double): String = "$ " + formatoNumero.format(valor)

fun porcentaje(tasa: Double): String {
    val v = tasa * 100
    return if (v % 1.0 == 0.0) "${v.toInt()} %"
    else String.format(Locale.US, "%.1f %%", v).replace(".", ",")
}

fun String.aNumero(): Double = toDoubleOrNull() ?: 0.0
fun String.aEntero(): Int = toIntOrNull() ?: 0
```

Como están en el mismo paquete que las pantallas, no hace falta importarlas en ningún lado.

El autoguardado con `delay(600)` es el patrón más limpio de Compose para esto: cada vez que cambia cualquiera de las claves, la corrutina anterior se cancela y arranca una nueva. Si el usuario escribe "120000" de un tirón, se descarta la escritura de "1", "12", "120"… y solo se guarda una vez, 600 ms después de la última tecla.

**Sobre el `bottomBar`**: dos decisiones que parecen cosméticas y son de cumplimiento.

- **El banner nunca aparece si el nivel es 0.** Va en el `bottomBar` del `Scaffold`, así que ocupa su propio espacio y no se superpone a nada del contenido.
- **Se esconde con el teclado abierto.** Un banner pegado al borde superior del teclado numérico genera toques accidentales, y los clics accidentales son *tráfico inválido*: el mismo problema que tocar tus propios anuncios, solo que provocado por el diseño. Google penaliza esto.

### 6B.6 La pantalla de configuración

Creá `PantallaConfiguracion.kt` en el paquete raíz. Es larga, pero es casi toda maquetado:

```kotlin
package uy.tunombre.sueldoliquido

import android.app.Activity
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
import androidx.compose.material.icons.filled.Star
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
import uy.tunombre.sueldoliquido.anuncios.GestorAnuncios
import uy.tunombre.sueldoliquido.datos.Ajustes
import uy.tunombre.sueldoliquido.datos.NivelAnuncios
import uy.tunombre.sueldoliquido.datos.RepositorioAjustes
import uy.tunombre.sueldoliquido.dominio.Parametros
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val URL_GITHUB = "https://github.com/TUUSUARIO/sueldo-liquido-uy"
private const val URL_PRIVACIDAD = "https://TUUSUARIO.github.io/politica-privacidad/"
private const val DIAS_DESBLOQUEO = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(
    repositorio: RepositorioAjustes,
    ajustes: Ajustes,
    onVolver: () -> Unit
) {
    val alcance = rememberCoroutineScope()
    val contexto = LocalContext.current
    val actividad = contexto as? Activity
    val ahora = System.currentTimeMillis()
    val comparadorActivo = ajustes.comparadorHasta > ahora

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

            // ---------- Comparador (recompensado) ----------
            Encabezado(Icons.Filled.Star, "Comparador de sueldos")

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    if (comparadorActivo) {
                        Text("Desbloqueado", fontWeight = FontWeight.Bold)
                        Text(
                            "Podés comparar dos sueldos hasta el " +
                                SimpleDateFormat("dd/MM/yyyy", Locale("es", "UY"))
                                    .format(Date(ajustes.comparadorHasta)) + ".",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            "El comparador te deja poner dos sueldos nominales y ver los dos " +
                                "líquidos uno al lado del otro. Es una función extra: la " +
                                "calculadora funciona igual sin ella.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(12.dp))

                        // Toda la información del intercambio, ANTES de que el usuario acepte.
                        Text(
                            "Si mirás un video publicitario completo, se desbloquea el " +
                                "comparador por $DIAS_DESBLOQUEO días. Podés cerrar el video " +
                                "en cualquier momento; si lo cerrás antes de que termine, no " +
                                "se desbloquea y no pasa nada más.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (actividad == null) return@Button
                                GestorAnuncios.iniciar(contexto)
                                GestorAnuncios.mostrarRecompensado(
                                    actividad = actividad,
                                    alGanarRecompensa = {
                                        alcance.launch {
                                            repositorio.desbloquearComparador(
                                                System.currentTimeMillis() +
                                                    DIAS_DESBLOQUEO * 24L * 60 * 60 * 1000
                                            )
                                        }
                                    },
                                    alFallar = {
                                        Toast.makeText(
                                            contexto,
                                            "No hay ningún video disponible en este momento",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Ver el video y desbloquear $DIAS_DESBLOQUEO días")
                        }
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
                        "Versión ${BuildConfig.VERSION_NAME} · parámetros ${Parametros.EJERCICIO}",
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
```

**Las palabras de esta pantalla son parte del cumplimiento, no relleno.** Fijate en lo que el texto NO dice: no dice "apoyá el proyecto", no dice "ayudame activando anuncios", no dice "gracias por el apoyo". Pedirle al usuario que vea anuncios para colaborar con vos es **solicitación**, y AdMob la trata como tráfico inválido. El texto describe qué pasa y deja que el usuario elija; nada más.

El bloque del comparador, en cambio, sí puede invitar a ver un video, porque cumple las tres condiciones del formato recompensado: dice **qué se pide** (mirar un video completo), dice **qué se obtiene** (30 días de una función concreta), y el usuario **acepta cada vez** tocando el botón.

Otros detalles:

- **`BuildConfig.VERSION_NAME`** lee automáticamente el `versionName` de tu `build.gradle.kts`, así no tenés dos lugares donde actualizar el número. Si aparece en rojo, agregá `buildFeatures { buildConfig = true }` al bloque `android` y sincronizá.
- **Los íconos usados son los del paquete básico** (`Settings`, `ArrowBack`, `Info`, `Delete`, `Star`). Si querés otros, agregá `implementation("androidx.compose.material:material-icons-extended:1.7.8")`, que trae unos 2.000 más pero engorda el APK.
- **El botón de GitHub no necesita ningún permiso ni declaración extra.** Android 11 restringió la visibilidad entre apps, pero los intents web con `http`/`https` están exceptuados. El `try/catch` cubre el equipo sin navegador.
- **Reemplazá `TUUSUARIO`** en las dos constantes del principio del archivo.
- **El comparador en sí queda por implementar.** Es una pantalla con dos campos de nominal y dos tarjetas de resultado, reutilizando `Calculadora.calcular` tal cual está. Mientras no exista, no publiques el botón de desbloqueo: prometer una recompensa que no se entrega es, además de feo, una infracción directa de la política de anuncios recompensados.

### 6B.7 Crear el repositorio de GitHub

Para que el botón lleve a algún lado:

```bash
cd ~/ruta/a/SueldoLiquidoUY
git init
printf '%s\n' \
  '*.iml' '.gradle/' 'local.properties' '.idea/' 'build/' \
  'captures/' '.externalNativeBuild/' '.cxx/' '*.jks' '*.keystore' \
  > .gitignore
git add .
git commit -m "Versión inicial de la calculadora de sueldo líquido"
```

Después creá el repositorio vacío en github.com (sin README, para que no haya conflicto) y:

```bash
git remote add origin https://github.com/TUUSUARIO/sueldo-liquido-uy.git
git branch -M main
git push -u origin main
```

> ⚠️ Fijate que `*.jks` y `*.keystore` estén en el `.gitignore` **antes** del primer `git add`. Subir tu clave de firma a un repositorio público es irreversible: aunque borres el archivo después, queda en el historial, y cualquiera puede firmar actualizaciones falsas de tu app. Si te pasa, generá una clave nueva y contactá al soporte de Play.

---

### 6B.8 Cómo funciona realmente el sistema de anuncios

Antes de escribir una línea de código conviene entender qué está pasando, porque casi todos los errores de principiante en anuncios son conceptuales, no técnicos.

#### Quién le paga a quién

```
  Anunciante                Red publicitaria              Vos
  (una marca que      →     (Google AdMob:          →     (publisher:
   quiere vender)            hace de intermediario         prestás el
                             y corre una subasta)          espacio)

  Paga por que su          Se queda con una             Cobrás el resto
  anuncio se vea           comisión                      cuando juntás
  o se toque                                             US$ 100
```

**AdMob** es la plataforma de Google para poner anuncios dentro de apps móviles. Es el primo de AdSense (que es para sitios web). Vos no negociás con ningún anunciante: cada vez que tu app pide un anuncio, AdMob corre una **subasta en tiempo real** (dura milisegundos) entre todos los anunciantes que quieren llegar a un usuario con ese perfil, en ese país, en ese momento. Gana el que más paga, y su anuncio se muestra.

#### El vocabulario que vas a ver en los informes

| Término | Qué significa |
|---|---|
| **Solicitud** (ad request) | Tu app le pidió un anuncio a AdMob |
| **Impresión** | El anuncio efectivamente se mostró en pantalla |
| **Tasa de coincidencia** (match rate / fill rate) | Qué porcentaje de tus solicitudes se llenaron con un anuncio. Nunca es 100 % |
| **Clic** | El usuario tocó el anuncio |
| **CTR** | Clics ÷ impresiones. En banners es bajísimo; en formatos de pantalla completa, algo mayor |
| **eCPM** | **El número que importa.** Cuánto te pagan por cada **1.000 impresiones**. La "e" es de "efectivo": es un promedio calculado hacia atrás, no un precio fijo |
| **Mediación** | Conectar varias redes además de Google para que compitan entre sí. No lo necesitás al principio |

La fórmula para estimar ingresos es siempre la misma:

```
ingreso ≈ (impresiones ÷ 1000) × eCPM
```

El eCPM depende sobre todo de **en qué país están tus usuarios** y **qué formato usás**. Uruguay es un mercado chico: los eCPM de la región suelen estar bastante por debajo de Estados Unidos o Europa occidental. Entre formatos, el orden habitual de mayor a menor es recompensado, apertura, banner: el banner es el que menos paga por impresión, pero también el que menos molesta. No tomes ningún número de internet como garantía: el único dato real es el de tu propio panel de AdMob después de un mes.

#### Qué NO es AdMob

- **No es un sueldo.** Ver la sección 6B.16 antes de hacerte expectativas.
- **No paga por instalaciones**, ni por usuarios registrados. Paga por impresiones y clics reales.
- **No podés tocar tus propios anuncios.** Es la forma más rápida de que te suspendan la cuenta y te retengan lo acumulado. Google llama a esto *tráfico inválido* y lo detecta con facilidad. Por eso existen los identificadores de prueba, que usamos en todo el desarrollo.

### 6B.9 Qué necesitás para implementarlo

| Requisito | Costo | Cuánto tarda | Nota |
|---|---|---|---|
| Cuenta de Google | — | — | Podés usar la misma de Play Console |
| Cuenta de AdMob | US$ 0 | minutos a 48 h | admob.google.com |
| App registrada en AdMob → **App ID** | — | minutos | Formato `ca-app-pub-…~…` (con **virgulilla**) |
| Bloques de anuncios → **Ad Unit ID** | — | minutos | Formato `ca-app-pub-…/…` (con **barra**) |
| Perfil de pagos con datos fiscales | — | 1–2 días | Incluye formulario fiscal de EE. UU. (para no residentes suele ser el W-8BEN) |
| Verificación de dirección por PIN postal | — | 2–4 semanas | Se dispara al llegar a US$ 10 acumulados. Llega una carta física |
| Cuenta bancaria para el cobro | — | — | AdMob paga por transferencia; el umbral es **US$ 100** |

**Los dos identificadores se confunden constantemente. No son lo mismo:**

- **App ID** — identifica a *toda tu aplicación*. Va una sola vez, en el `AndroidManifest.xml`. Lleva `~`.
- **Ad Unit ID** — identifica *un espacio publicitario concreto* (un banner, un video recompensado). Va en el código, uno por cada lugar donde mostrás publicidad. Lleva `/`.

Si ponés un Ad Unit ID donde va el App ID, **la app crashea al arrancar** con un mensaje sobre `com.google.android.gms.ads.APPLICATION_ID`. Es literalmente el error número uno de quien integra AdMob por primera vez.

#### Pasos en la web de AdMob

1. Entrá a **https://admob.google.com** e iniciá sesión.
2. **Apps → Agregar app**. Como todavía no está publicada, elegí **"No"** en "¿Está publicada en una tienda?" y ponele el nombre. Cuando la publiques, la vinculás a la ficha de Play.
3. Anotá el **App ID** que te da.
4. **Bloques de anuncios → Crear bloque de anuncios**. Vas a necesitar tres:
   - Uno de tipo **Banner**, llamalo `banner_calculadora`
   - Uno de tipo **Apertura de la app**, llamalo `app_open_inicio`
   - Uno de tipo **Recompensado**, llamalo `rewarded_comparador`
5. Anotá los tres **Ad Unit ID**.

   No vas a crear ningún bloque **intersticial**. En 6B.10 está el porqué.
6. **Pagos → Configurar** y completá el perfil, la información fiscal y el método de cobro. Podés dejarlo para después, pero hasta que no esté no se acumula nada cobrable.

> **Sobre impuestos en Uruguay**: los ingresos de AdMob son ingresos del exterior y tienen tratamiento fiscal propio. Yo no soy contador y esto no es asesoramiento: si el proyecto llega a generar algo, consultá con un profesional cómo declararlo.

### 6B.10 Formatos, niveles y por qué el plan cambió

Acá está el corazón de esta sección. Tu idea original —publicidad apagada de fábrica y activable por niveles— es buena y se mantiene. Lo que cambia es **con qué formato y en qué momento** se muestra cada cosa, porque tres de las ideas originales chocan con políticas concretas de AdMob.

#### Los formatos disponibles

| Formato | Cómo se ve | Requiere consentimiento del usuario para cada vista |
|---|---|---|
| **Banner** | Franja fija, arriba o abajo | No |
| **Intersticial** | Pantalla completa, con una X | No |
| **Apertura** (app open) | Pantalla completa mientras la app carga | No |
| **Recompensado** | Video que el usuario elige ver a cambio de algo | **Sí, cada vez** |
| **Nativo** | Se integra con el diseño de la app | No |

#### Qué se cambió y por qué

**Problema 1 — El intersticial al abrir la app.**
Google prohíbe expresamente colocar intersticiales al abrir o al salir de la app: deben ir **entre páginas de contenido**. Para el momento de la apertura, la documentación recomienda el formato **apertura de la app**.
*Solución*: el nivel "al abrir" usa formato de apertura. Esto ya estaba bien encaminado y se mantiene.

**Problema 2 — El intersticial al entrar a configuración.** Este es el cambio más importante y tiene dos motivos independientes, cualquiera de los dos alcanza:

- **Frecuencia.** La política dice que no hay que abrumar al usuario, y menciona explícitamente como incumplimiento colocar un intersticial *después de cada acción del usuario*. Un anuncio cada vez que se abre una pantalla, sin ningún tope, cae ahí.
- **Es un patrón oscuro.** Configuración es exactamente el lugar donde el usuario va a **apagar la publicidad**. Cobrarle un anuncio de pantalla completa por llegar hasta el interruptor de apagado es ponerle un peaje a la salida. Google Play tiene una política de comportamiento engañoso que apunta justo a este tipo de diseños, y aunque no te la aplicaran, sigue siendo una mala idea.

*Solución*: **configuración es siempre gratis de llegar, en todos los niveles.** El "nivel intermedio" pasa a ser un **banner al pie de la calculadora**, que es el formato pensado para publicidad persistente y de baja fricción.

**Problema 3 — El botón "ver un anuncio ahora" para apoyar el proyecto.** Este era el más riesgoso de los tres. Pedirle al usuario que vea anuncios para colaborar con el desarrollador es **solicitación de vistas**, y AdMob la trata igual que pedir clics: tráfico inválido, con suspensión de cuenta y retención de lo acumulado como sanción típica.

Existe un único mecanismo sancionado para que un usuario vea un anuncio a propósito: el **formato recompensado**. Sus reglas son estrictas y hay que cumplirlas las tres:

1. Solo puede servirse **después de que el usuario acepte de forma afirmativa e inequívoca**, tocando un botón que represente "sí" o "aceptar".
2. El usuario debe aceptar **cada anuncio individualmente**. No se puede configurar una vez y que después aparezcan solos.
3. Hay que **declarar de antemano qué se pide y qué se obtiene**, y el usuario tiene que poder cerrar o saltear el anuncio.

La recompensa puede ser algo no monetario: un descuento, puntos, o el acceso temporal a una función. Por eso el "botón de apoyo" se convirtió en **"ver un video y desbloquear el comparador 30 días"**.

> La regla 2 tiene una consecuencia que conviene tener clara: **no existe un nivel de configuración que active anuncios recompensados automáticos.** Si lo hubiera, sería una infracción. Por eso el comparador quedó fuera de la escalera de niveles, como un botón aparte.

#### El plan nuevo

| Lo que pediste | Qué queda | Formato | Por qué |
|---|---|---|---|
| Por defecto sin anuncios | **Nivel 0 — sin publicidad** | ninguno | Igual que lo pensaste |
| Nivel 0: un anuncio en el momento | **Botón del comparador**, fuera de los niveles | Recompensado | Único camino permitido para una vista pedida por el usuario |
| Nivel 1: al entrar a configuración | **Nivel 1 — banner al pie de la calculadora** | Banner | Configuración no puede tener peaje |
| Nivel 2: al abrir la app | **Nivel 2 — anuncio al abrir** | Apertura | Formato correcto para ese momento |
| Nivel 3: ambos | **Nivel 3 — banner y anuncio al abrir** | Banner + apertura | Igual que lo pensaste |

Seguís teniendo cuatro niveles, con el nivel 0 apagado de fábrica y control total del usuario. Lo que cambia es que ninguno de los cuatro depende de un formato colocado donde no corresponde.

#### Los tres frenos del anuncio de apertura

El formato de apertura es correcto para el arranque, pero sin límites igual abruma. Por eso el código de 6B.4 tiene tres condiciones, y las tres importan:

| Freno | Qué evita |
|---|---|
| **Nunca en la primera sesión** | Que la primera impresión de la app sea un anuncio a pantalla completa antes de haber visto nada. Es lo que hunde las valoraciones en Play |
| **Máximo uno cada 4 horas** | El "después de cada acción" que la política señala. Alguien que abre la app cinco veces seguidas ve un anuncio, no cinco |
| **Una sola evaluación por arranque** | Que reaparezca cada vez que el usuario vuelve de otra app |

Ninguno de los tres es un requisito literal escrito con un número en la política. Son la traducción práctica de "no abrumes al usuario", que sí lo es, y son la diferencia entre una app que monetiza tranquila y una que junta advertencias.

#### Reglas de colocación del banner

- **Nunca pegado a un control.** Separación mínima con el último elemento tocable. Un banner adyacente a un botón produce clics accidentales, y los clics accidentales son tráfico inválido.
- **Nunca debajo del teclado.** Por eso se esconde cuando el teclado está abierto.
- **Nunca superpuesto al contenido.** Va en el `bottomBar`, con su propio espacio.
- **No acelerar el refresco.** El SDK refresca solo cada 30-60 segundos; forzarlo más rápido infla impresiones y es infracción.
- **Distinguible del contenido.** El SDK ya marca sus anuncios, y en el código le agregamos una línea divisoria y la etiqueta "Publicidad" arriba. Lo prohibido es lo contrario: disfrazar el anuncio de contenido de la app.

### 6B.11 Configurar el proyecto para anuncios

**Paso 1 — La dependencia** (ya la agregaste en 6B.2):

```kotlin
implementation("com.google.android.gms:play-services-ads:25.4.0")
```

**Paso 2 — El `AndroidManifest.xml`.** Abrí `app/src/main/AndroidManifest.xml` y agregá el permiso antes de `<application>` y el `meta-data` adentro:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.SueldoLiquidoUY">

        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Ese App ID es **el de ejemplo de Google**, seguro para desarrollo. Se cambia por el tuyo recién en 6B.15.

**Paso 3 — Reconocer lo que el SDK agrega solo.** El SDK de anuncios declara por su cuenta el permiso `com.google.android.gms.permission.AD_ID`, que le da acceso al **identificador de publicidad** del dispositivo. No lo escribís vos, pero aparece en el manifiesto final (`Merged Manifest`, la pestaña abajo del editor del manifiesto). Es importante que lo sepas por dos motivos:

- Es lo que hay que declarar en el formulario de **Seguridad de los datos** de Play Console.
- Es la razón por la que **tu app deja de ser "sin permisos y sin conexión"** en el momento en que agregás anuncios. Hay que actualizar la política de privacidad y las declaraciones. Ver 6B.17.

### 6B.12 El código de los anuncios

Creá el paquete `anuncios` y adentro `GestorAnuncios.kt`:

```kotlin
package uy.tunombre.sueldoliquido.anuncios

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Todo el trato con AdMob pasa por acá.
 *
 * Reglas de oro:
 *  - un anuncio se carga ANTES de necesitarlo y se muestra después
 *  - un objeto de anuncio se usa UNA sola vez; después hay que pedir otro
 *  - si no hay anuncio disponible, la app sigue funcionando como si nada
 *  - el recompensado solo se muestra con una aceptación explícita del usuario
 */
object GestorAnuncios {

    private const val TAG = "Anuncios"

    // ⚠️ IDENTIFICADORES DE PRUEBA DE GOOGLE.
    // Son seguros: no generan ingresos ni tráfico inválido.
    // Se cambian por los tuyos recién el día de publicar (ver 6B.15).
    const val ID_BANNER = "ca-app-pub-3940256099942544/9214589741"
    private const val ID_APERTURA = "ca-app-pub-3940256099942544/9257395921"
    private const val ID_RECOMPENSADO = "ca-app-pub-3940256099942544/5224354917"

    private var iniciado = false
    private var apertura: AppOpenAd? = null
    private var recompensado: RewardedAd? = null
    private var cargandoApertura = false
    private var cargandoRecompensado = false
    private var mostrandoAlgo = false

    /** Arranca el SDK. Se puede llamar muchas veces: solo la primera hace algo. */
    fun iniciar(contexto: Context) {
        if (iniciado) return
        iniciado = true
        MobileAds.initialize(contexto) {
            Log.d(TAG, "SDK de anuncios listo")
        }
    }

    // ---------------- Apertura de la app (niveles 2 y 3) ----------------

    fun precargarApertura(contexto: Context, alCargar: () -> Unit = {}) {
        if (apertura != null) { alCargar(); return }
        if (cargandoApertura) return
        cargandoApertura = true
        AppOpenAd.load(
            contexto,
            ID_APERTURA,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(anuncio: AppOpenAd) {
                    apertura = anuncio
                    cargandoApertura = false
                    alCargar()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    apertura = null
                    cargandoApertura = false
                    Log.w(TAG, "Apertura no cargó: ${error.code} ${error.message}")
                }
            }
        )
    }

    fun mostrarApertura(actividad: Activity, alTerminar: () -> Unit = {}) {
        val anuncio = apertura
        if (anuncio == null || mostrandoAlgo) { alTerminar(); return }
        anuncio.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { mostrandoAlgo = true }

            override fun onAdDismissedFullScreenContent() {
                apertura = null
                mostrandoAlgo = false
                alTerminar()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                apertura = null
                mostrandoAlgo = false
                Log.w(TAG, "No se pudo mostrar la apertura: ${error.message}")
                alTerminar()
            }
        }
        anuncio.show(actividad)
    }

    // ---------------- Recompensado (desbloqueo del comparador) ----------------

    /**
     * Carga y muestra un video recompensado.
     *
     * Se llama SOLO desde el botón que el usuario tocó después de leer qué se
     * pide y qué se obtiene. Nunca automáticamente: la política exige una
     * aceptación explícita para cada anuncio recompensado.
     *
     * [alGanarRecompensa] se ejecuta únicamente si el usuario miró el video
     * completo. Si lo cierra antes, no se llama y no se desbloquea nada.
     */
    fun mostrarRecompensado(
        actividad: Activity,
        alGanarRecompensa: () -> Unit,
        alFallar: () -> Unit = {}
    ) {
        val disponible = recompensado
        if (disponible != null) {
            presentar(disponible, actividad, alGanarRecompensa)
            return
        }
        if (cargandoRecompensado) return
        cargandoRecompensado = true
        RewardedAd.load(
            actividad,
            ID_RECOMPENSADO,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(anuncio: RewardedAd) {
                    recompensado = anuncio
                    cargandoRecompensado = false
                    presentar(anuncio, actividad, alGanarRecompensa)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    recompensado = null
                    cargandoRecompensado = false
                    Log.w(TAG, "Recompensado no cargó: ${error.message}")
                    alFallar()
                }
            }
        )
    }

    private fun presentar(
        anuncio: RewardedAd,
        actividad: Activity,
        alGanarRecompensa: () -> Unit
    ) {
        anuncio.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { mostrandoAlgo = true }

            override fun onAdDismissedFullScreenContent() {
                recompensado = null
                mostrandoAlgo = false
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                recompensado = null
                mostrandoAlgo = false
            }
        }
        anuncio.show(actividad) { premio ->
            Log.d(TAG, "Recompensa ganada: ${premio.amount} ${premio.type}")
            alGanarRecompensa()
        }
    }
}
```

Y el banner, que en Compose se inserta con `AndroidView` porque `AdView` es una vista clásica de Android. Creá `anuncios/BannerPublicitario.kt`:

```kotlin
package uy.tunombre.sueldoliquido.anuncios

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerPublicitario(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        // Separación clara respecto del contenido y de cualquier control.
        HorizontalDivider()
        Text(
            text = "Publicidad",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 2.dp)
        )
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { contexto ->
                AdView(contexto).apply {
                    adUnitId = GestorAnuncios.ID_BANNER
                    setAdSize(tamanioAdaptativo(contexto))
                    loadAd(AdRequest.Builder().build())
                }
            },
            // Sin esto el AdView queda vivo y filtra memoria al salir de la pantalla.
            onRelease = { vista -> vista.destroy() }
        )
    }
}

/** Banner adaptativo anclado: se ajusta al ancho real del dispositivo. */
private fun tamanioAdaptativo(contexto: Context): AdSize {
    val metricas = contexto.resources.displayMetrics
    val anchoDp = (metricas.widthPixels / metricas.density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(contexto, anchoDp)
}
```

**El ciclo de vida de un anuncio de pantalla completa**, que es lo que cuesta entender al principio:

```
   precargar()  ──►  el SDK pide el anuncio por internet  ──►  onAdLoaded
                                                                    │
                                                    queda en memoria │
                                                                    ▼
   mostrar()    ──►  pantalla completa  ──►  el usuario cierra  ──►  onAdDismissed
                                                                    │
                                              el objeto ya no sirve │
                                                                    ▼
                                                          precargar() de nuevo
```

Tres consecuencias prácticas:

1. **Un anuncio cargado se usa una sola vez.** Después de mostrarlo hay que poner la variable en `null` y pedir otro. Si intentás mostrar el mismo dos veces, no pasa nada (silenciosamente).
2. **Cargar tarda.** Entre medio segundo y varios segundos según la conexión. Por eso el de apertura se precarga y recién se muestra en el callback.
3. **Puede fallar y es normal.** Sin conexión, sin inventario para ese país, o simplemente porque no había puja. Por eso todos los caminos terminan llamando a `alTerminar()` o `alFallar()`.

**Sobre el recompensado hay una diferencia clave**: acá sí se carga y se muestra en el mismo gesto, porque el usuario ya tocó el botón y está esperando. La contrapartida es la espera de unos segundos. Si te molesta, podés precargarlo al entrar a configuración — pero **precargar no es mostrar**: la aceptación explícita sigue siendo obligatoria antes de cada `show()`.

El `anuncio.show(actividad) { premio -> ... }` es la parte que importa del recompensado. Esa lambda se ejecuta **solo si el usuario miró el video completo**. Si lo cierra antes, no se llama, y el comparador no se desbloquea. Eso no es una decisión de diseño tuya: es cómo tiene que funcionar el formato.

### 6B.13 Consentimiento: el SDK de UMP

Si tu app llega a usuarios del **Espacio Económico Europeo, Reino Unido o Suiza**, Google exige que antes de mostrar anuncios personalizados aparezca un formulario de consentimiento gestionado por una plataforma certificada. Desde el 16 de enero de 2024 es obligatorio, y Google ofrece la suya gratis: el **UMP SDK** (User Messaging Platform).

¿Te aplica? Uruguay no está en esa lista, pero si publicás en Play Store para todos los países, **sí**. Dos caminos:

- **Camino corto**: en Play Console, limitá la distribución a países de América Latina. Te ahorrás el UMP.
- **Camino correcto**: implementá el UMP. Son unas veinte líneas.

Para el camino correcto:

1. En AdMob: **Privacidad y mensajes → Regulaciones europeas → Crear mensaje**, elegí los idiomas y publicalo. **Sin este paso el formulario nunca aparece**, por más código que escribas.
2. Agregá la dependencia (puede que ya venga con el SDK de anuncios; si Gradle se queja de duplicados, sacala):

```kotlin
implementation("com.google.android.ump:user-messaging-platform:3.2.0")
```

3. En `MainActivity.onCreate`, antes de iniciar el SDK de anuncios:

```kotlin
val parametros = ConsentRequestParameters.Builder().build()
val consentimiento = UserMessagingPlatform.getConsentInformation(this)

consentimiento.requestConsentInfoUpdate(
    this,
    parametros,
    {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(this) { error ->
            if (error != null) Log.w("Consentimiento", error.message)
            if (consentimiento.canRequestAds()) {
                GestorAnuncios.iniciar(applicationContext)
            }
        }
    },
    { error -> Log.w("Consentimiento", error.message) }
)
```

La regla general: **no pidas ni muestres anuncios hasta que `canRequestAds()` devuelva `true`**. Para un usuario uruguayo devuelve `true` de entrada, sin mostrar ningún formulario.

### 6B.14 Probar sin que te suspendan la cuenta

Esta es la parte donde la gente arruina su cuenta de AdMob antes de publicar. Tres reglas:

**1. Durante todo el desarrollo, usá los identificadores de prueba de Google.** Son estos, y son públicos:

| Formato | ID de prueba (Android) | ¿Lo usa esta app? |
|---|---|---|
| App ID | `ca-app-pub-3940256099942544~3347511713` | Sí, en el manifiesto |
| Banner adaptativo anclado | `ca-app-pub-3940256099942544/9214589741` | Sí, niveles 1 y 3 |
| Apertura de la app | `ca-app-pub-3940256099942544/9257395921` | Sí, niveles 2 y 3 |
| Recompensado | `ca-app-pub-3940256099942544/5224354917` | Sí, comparador |
| Banner de tamaño fijo | `ca-app-pub-3940256099942544/6300978111` | No |
| Intersticial | `ca-app-pub-3940256099942544/1033173712` | **No** (ver 6B.10) |

No están asociados a tu cuenta, así que no hay riesgo de generar tráfico inválido. Los anuncios que devuelven tienen la etiqueta **"Test Ad"** arriba y se pueden tocar tranquilamente.

**2. Si querés probar con tus IDs reales, registrá tu dispositivo como dispositivo de prueba.** La primera vez que la app pida un anuncio con tu ID real, en logcat aparece una línea con el identificador cifrado de tu equipo. Filtrá por `Ads` en logcat y buscá algo como `setTestDeviceIds(Arrays.asList("ABCDEF0123456789"))`. Después:

```kotlin
MobileAds.setRequestConfiguration(
    RequestConfiguration.Builder()
        .setTestDeviceIds(listOf("ABCDEF0123456789"))
        .build()
)
```

**Acordate de sacar esa línea antes de publicar**, o tu propio teléfono nunca va a generar ingresos.

**3. Nunca, bajo ninguna circunstancia, toques tus anuncios reales.** Ni vos, ni para "ver si funciona", ni pidiéndole a un amigo. Google lo detecta y la sanción típica es la inhabilitación de la cuenta con retención de lo acumulado.

**Herramienta útil**: agitá el teléfono con la app abierta en modo depuración para abrir el **Ad Inspector**, un panel del propio SDK que te muestra en tiempo real qué solicitudes se hicieron, cuáles fallaron y por qué. Se habilita con `MobileAds.openDebugMenu(contexto, ID_INTERSTICIAL)` o desde el gesto de agitar si lo activás en AdMob.

**Cómo leer los errores de carga más comunes:**

| Código | Significado | Qué hacer |
|---|---|---|
| 0 | Error interno | Reintentar |
| 1 | Solicitud inválida | El Ad Unit ID está mal escrito o no corresponde al formato |
| 2 | Error de red | Sin conexión. Normal, no es un bug |
| 3 | Sin relleno (no fill) | No había anuncio disponible. **Es normalísimo**, sobre todo con IDs nuevos y en mercados chicos |

El código 3 con IDs recién creados es tan común que muchos creen que integraron mal. Un bloque de anuncios nuevo puede tardar **hasta 24 horas** en empezar a servir.

### 6B.15 El día del lanzamiento: pasar a los IDs reales

Este cambio se hace **una sola vez**, justo antes de generar el `.aab` definitivo:

1. En `GestorAnuncios.kt`, reemplazá `ID_BANNER`, `ID_APERTURA` e `ID_RECOMPENSADO` por los tuyos.
2. En `AndroidManifest.xml`, reemplazá el `APPLICATION_ID` por tu App ID real.
3. Sacá cualquier `setTestDeviceIds(...)`.
4. En AdMob, vinculá la app a su ficha de Play Store una vez publicada (**Apps → tu app → Configuración de la app**). Esto mejora bastante el eCPM porque los anunciantes ven una app verificada.
5. Compilá el release, instalalo en tu teléfono y **verificá que los anuncios aparezcan sin la etiqueta "Test Ad"** — pero no los toques.
6. Verificá que el comparador exista y funcione de verdad antes de publicar el botón que lo promete.

> **Truco recomendable**: en lugar de editar constantes a mano cada vez, poné los IDs en `build.gradle.kts` con `buildConfigField`, uno para `debug` y otro para `release`. Así el modo depuración usa siempre los de prueba y el release los reales, sin que puedas olvidarte.

### 6B.16 Cuánta plata es esto en la vida real

Te debo una respuesta honesta acá, porque la diferencia entre lo que la gente espera y lo que pasa es grande.

Hagamos la cuenta con números optimistas para tu caso:

```
5.000 instalaciones en el primer año                (sería un éxito para una app local)
  × 5 % que además elige un nivel con publicidad    (viene apagada de fábrica)
  = 250 personas con publicidad activa
  × 2 usos al mes                                   (una calculadora de sueldo no es Instagram)
  = 500 impresiones de banner por mes
  ÷ 1000 × eCPM de US$ 1                            (orden de magnitud de un banner en la región)
  = US$ 0,50 por mes
```

Con esos números, llegar al **umbral de pago de US$ 100** tomaría más de una década.

Esto no significa que hayas hecho algo mal, ni que no valga la pena implementarlo. Significa que el sistema de anuncios de tu app es, en la práctica, **un frasco de propinas**: una forma de que a quien le resultó útil pueda devolver algo simbólico. Tratalo así y no te vas a frustrar.

Lo que sí cambia el orden de magnitud, si alguna vez te interesa:

- **Que los anuncios estén activos por defecto.** Multiplica las impresiones por treinta o más. Vos elegiste lo contrario a propósito, y es una decisión de producto respetable: la app es más agradable así.
- **Volumen.** Los números empiezan a tener sentido a partir de decenas de miles de usuarios activos.
- **Frecuencia de uso.** Las apps que se abren todos los días monetizan; las que se abren dos veces al mes, no.

Alternativas que quizá encajen mejor con una herramienta como esta:

- **Una versión de pago única** con alguna función extra, cobrada a través de Facturación de Google Play. Es el camino sancionado por la plataforma para cobrar dentro de la app.
- **Un enlace de donación externo.** Ojo: la política de pagos de Google Play regula qué se puede cobrar fuera de su sistema y cambia seguido. Antes de poner un botón de "invitame un café", leé la política vigente en el Centro de Políticas para Desarrolladores.
- **El proyecto como carta de presentación.** Una app publicada, con código abierto en GitHub y tests unitarios, vale bastante más en una entrevista de trabajo que sesenta centavos por mes.

### 6B.17 Lo que cambia en el resto de la guía al agregar anuncios

Agregar AdMob rompe tres afirmaciones que la versión 1.0 podía hacer con tranquilidad. Hay que corregirlas o Play te rechaza la app.

| Antes (v1.0) | Ahora (v1.1 con anuncios) |
|---|---|
| Sin permisos | Usa `INTERNET` y el SDK agrega `AD_ID` |
| Funciona sin conexión | La calculadora sí; los anuncios no |
| No recolecta ningún dato | El SDK de Google accede al identificador de publicidad |
| "No contiene anuncios" en Play | **Contiene anuncios: Sí** |
| Seguridad de los datos: no recolecta nada | Hay que declarar identificadores del dispositivo |

Las tres secciones que tenés que revisar antes de publicar son la **10** (política de privacidad), la **12.2** (declaraciones de Play Console) y la **7** (casos de prueba). Están actualizadas más abajo con las dos variantes.

> Una decisión que te simplifica la vida: **publicá primero la versión 1.0 sin anuncios**, pasá la prueba cerrada y la revisión con la app más simple posible, y recién después subí la 1.1 con configuración y anuncios como actualización. Las revisiones de apps nuevas con publicidad son más estrictas, y si algo falla vas a saber exactamente qué cambió.
---

## 6C. Parámetros fiscales editables por el usuario

Hasta acá, los valores fiscales viven dentro del código y solo cambian cuando vos publicás una actualización. Esta sección los saca afuera: el usuario puede verlos, modificarlos y volver a los oficiales de un toque.

### 6C.1 Para qué sirve realmente

Hay dos casos de uso, y el primero es el que le da valor a toda la app.

**Caso 1 — El hueco de enero.** El Poder Ejecutivo fija la BPC nueva el 1.º de enero. Entre ese día y el día en que tu actualización pasa la revisión de Google puede pasar de una semana a varios meses, según cuánto tiempo libre tengas. Durante todo ese período tu app calcula mal, y el usuario no tiene forma de saberlo. Con esta sección, cualquiera puede escribir la BPC nueva y seguir usando la app correctamente mientras vos preparás la actualización con calma.

**Caso 2 — La medición concreta.** Recalcular un recibo del año pasado. Ver cuánto cambiaría el líquido si una reforma anunciada modifica una tasa. Simular con la BPC de otro ejercicio. Todo eso hoy es imposible y pasa a ser trivial.

Hay un efecto secundario que conviene tener claro: **esto convierte un riesgo de exactitud en un riesgo de confianza.** Antes, el peor caso era que la app quedara desactualizada. Ahora el peor caso es que alguien cambie una tasa, se olvide, y crea que el resultado es oficial. Las tres reglas de la sección siguiente existen para eso.

### 6C.2 Tres reglas que no se negocian

**Regla 1 — Los oficiales siempre están a un toque.** Los valores publicados por BPS y DGI quedan en el código como punto de partida inamovible. Un botón "Restaurar valores oficiales" tiene que devolver todo a su lugar sin preguntas ni pasos intermedios.

**Regla 2 — Si el usuario cambió algo, la app lo dice en la pantalla de resultados.** No en configuración, no en un menú: en la pantalla donde aparece el número. Alguien va a sacar una captura de pantalla de un cálculo y mandarla por WhatsApp; el aviso tiene que salir en esa captura.

**Regla 3 — Se valida antes de guardar, y el guardado es explícito.** Un cálculo silenciosamente absurdo es peor que un error visible. Y a diferencia del formulario de la calculadora, que se autoguarda, acá **no** hay autoguardado: una tasa a medio escribir ("1" mientras el usuario va camino a "15") no puede entrar nunca al cálculo.

### 6C.3 De constantes a parámetros que se pueden cambiar

El problema técnico es concreto: `const val` se resuelve en tiempo de compilación. Un valor declarado así **no se puede cambiar nunca** en tiempo de ejecución. Hay que convertir el objeto de constantes en una clase de datos que se pase como argumento.

Reemplazá `dominio/Parametros.kt` por `dominio/ParametrosFiscales.kt`:

```kotlin
package uy.tunombre.sueldoliquido.dominio

import kotlinx.serialization.Serializable

@Serializable
data class FranjaIrpf(
    /** Tope de la franja, expresado en BPC. null = última franja, sin tope. */
    val hastaEnBpc: Double?,
    /** Tasa en tanto por uno: 0.24 significa 24 %. */
    val tasa: Double
)

/** La escala publicada por BPS y DGI. Los topes van en BPC, como los define la ley. */
val ESCALA_IRPF_OFICIAL: List<FranjaIrpf> = listOf(
    FranjaIrpf(7.0, 0.00),
    FranjaIrpf(10.0, 0.10),
    FranjaIrpf(15.0, 0.15),
    FranjaIrpf(30.0, 0.24),
    FranjaIrpf(50.0, 0.25),
    FranjaIrpf(75.0, 0.27),
    FranjaIrpf(115.0, 0.31),
    FranjaIrpf(null, 0.36)
)

/**
 * Todos los valores fiscales que intervienen en el cálculo.
 *
 * Los que la ley expresa en BPC se guardan en BPC, no en pesos. Eso hace que
 * cambiar un solo número —la BPC— actualice de una vez las ocho franjas de
 * IRPF, el umbral de FONASA, el umbral de la tasa de deducción y las
 * deducciones por hijo. Es la razón por la que, en un enero normal, el usuario
 * toca un campo y la app vuelve a estar correcta.
 */
@Serializable
data class ParametrosFiscales(
    val ejercicio: Int = 2026,

    // --- Lo que cambia todos los años ---
    val bpc: Double = 6_864.0,
    val topeJubilatorio: Double = 288_836.0,

    // --- Lo que cambia solo con una reforma ---
    val tasaJubilatoria: Double = 0.15,
    val tasaFrl: Double = 0.001,

    val umbralFonasaEnBpc: Double = 2.5,
    val fonasaBajoSinConyuge: Double = 0.03,
    val fonasaBajoConConyuge: Double = 0.05,
    val fonasaAltoSolo: Double = 0.045,
    val fonasaAltoConHijos: Double = 0.06,
    val fonasaAltoConConyuge: Double = 0.065,
    val fonasaAltoConAmbos: Double = 0.08,

    val escalaIrpf: List<FranjaIrpf> = ESCALA_IRPF_OFICIAL,
    val umbralTasaDeduccionEnBpc: Double = 15.0,
    val tasaDeduccionAlta: Double = 0.14,
    val tasaDeduccionBaja: Double = 0.08,
    val deduccionHijoAnualEnBpc: Double = 20.0,
    val deduccionHijoDiscapacidadAnualEnBpc: Double = 40.0
) {
    // Valores derivados: no se guardan, se calculan a partir de la BPC.
    val umbralFonasa: Double get() = umbralFonasaEnBpc * bpc
    val umbralTasaDeduccion: Double get() = umbralTasaDeduccionEnBpc * bpc
    val deduccionHijo: Double get() = deduccionHijoAnualEnBpc * bpc / 12.0
    val deduccionHijoDiscapacidad: Double get() = deduccionHijoDiscapacidadAnualEnBpc * bpc / 12.0

    /** Tope de una franja convertido a pesos. La última franja no tiene tope. */
    fun topeEnPesos(franja: FranjaIrpf): Double =
        franja.hastaEnBpc?.times(bpc) ?: Double.MAX_VALUE

    /** true si el usuario tocó algo. Gracias a que es data class, es una sola comparación. */
    val sonPersonalizados: Boolean get() = this != OFICIALES

    companion object {
        /** Los valores publicados por BPS y DGI. Se actualizan con cada versión de la app. */
        val OFICIALES = ParametrosFiscales()
    }
}
```

> **Detalle de Kotlin**: `ESCALA_IRPF_OFICIAL` va **fuera** de la clase, a nivel de archivo. Si la ponés dentro del `companion object` y encima la usás como valor por defecto del constructor, entrás en un orden de inicialización frágil. Afuera no hay ambigüedad.

### 6C.4 Adaptar la calculadora

`Calculadora.kt` cambia poco: donde decía `Parametros.X` ahora dice `p.x`, y `p` entra como argumento con un valor por defecto.

```kotlin
package uy.tunombre.sueldoliquido.dominio

import kotlin.math.max
import kotlin.math.min

object Calculadora {

    fun calcular(
        e: Entrada,
        p: ParametrosFiscales = ParametrosFiscales.OFICIALES
    ): Resultado {
        val nominal = max(0.0, e.nominal)

        // 1. Aporte jubilatorio (con tope de cotización)
        val baseJubilatoria =
            if (e.aplicaTopeJubilatorio) min(nominal, p.topeJubilatorio) else nominal
        val jubilatorio = baseJubilatoria * p.tasaJubilatoria

        // 2. FONASA
        val tieneHijos = (e.hijos + e.hijosConDiscapacidad) > 0
        val tasaFonasa = tasaFonasa(nominal, tieneHijos, e.conyugeACargo, p)
        val fonasa = nominal * tasaFonasa

        // 3. FRL
        val frl = nominal * p.tasaFrl

        // 4. IRPF
        val impuesto = impuestoPorFranjas(nominal, p)

        val deduccionHijos =
            (e.hijos * p.deduccionHijo +
             e.hijosConDiscapacidad * p.deduccionHijoDiscapacidad) * e.atribucionHijos

        val totalDeducciones =
            jubilatorio + fonasa + frl + deduccionHijos + e.fondoSolidaridad + e.cajaProfesional

        val tasaDeduccion =
            if (nominal <= p.umbralTasaDeduccion) p.tasaDeduccionAlta else p.tasaDeduccionBaja

        val credito = totalDeducciones * tasaDeduccion
        val irpf = max(0.0, impuesto - credito)

        // 5. Líquido
        val otros = e.fondoSolidaridad + e.cajaProfesional + e.otrosDescuentos
        val totalDescuentos = jubilatorio + fonasa + frl + irpf + otros

        return Resultado(
            nominal = nominal,
            jubilatorio = jubilatorio,
            fonasa = fonasa,
            tasaFonasa = tasaFonasa,
            frl = frl,
            impuestoPorFranjas = impuesto,
            totalDeducciones = totalDeducciones,
            tasaDeduccion = tasaDeduccion,
            creditoDeducciones = credito,
            irpf = irpf,
            otrosDescuentos = otros,
            totalDescuentos = totalDescuentos,
            liquido = nominal - totalDescuentos
        )
    }

    fun tasaFonasa(
        nominal: Double,
        tieneHijos: Boolean,
        conyugeACargo: Boolean,
        p: ParametrosFiscales = ParametrosFiscales.OFICIALES
    ): Double =
        if (nominal <= p.umbralFonasa) {
            if (conyugeACargo) p.fonasaBajoConConyuge else p.fonasaBajoSinConyuge
        } else {
            when {
                conyugeACargo && tieneHijos -> p.fonasaAltoConAmbos
                conyugeACargo -> p.fonasaAltoConConyuge
                tieneHijos -> p.fonasaAltoConHijos
                else -> p.fonasaAltoSolo
            }
        }

    fun impuestoPorFranjas(
        renta: Double,
        p: ParametrosFiscales = ParametrosFiscales.OFICIALES
    ): Double {
        var restante = renta
        var pisoAnterior = 0.0
        var total = 0.0
        for (franja in p.escalaIrpf) {
            if (restante <= 0.0) break
            val tope = p.topeEnPesos(franja)
            val gravado = min(restante, tope - pisoAnterior)
            total += gravado * franja.tasa
            restante -= gravado
            pisoAnterior = tope
        }
        return total
    }
}
```

**El valor por defecto no es un detalle de comodidad: es lo que hace que tus siete tests sigan pasando sin tocarlos.** `Calculadora.calcular(Entrada(nominal = 120_000.0))` sigue usando los oficiales y sigue dando $ 87.064,80.

La única línea de test que hay que cambiar es la del caso de atribución de hijos, porque nombraba una constante que ya no existe:

```kotlin
// antes:  Parametros.DEDUCCION_HIJO * 2 * 0.5
// ahora:
ParametrosFiscales.OFICIALES.deduccionHijo * 2 * 0.5
```

Y en la pantalla de la calculadora, el subtítulo y la llamada al cálculo pasan a usar los parámetros vigentes:

```kotlin
val r = Calculadora.calcular(entrada, ajustes.parametros)
// …
Text("Parámetros ${ajustes.parametros.ejercicio} · BPC ${moneda(ajustes.parametros.bpc)}")
```

### 6C.5 Guardar los parámetros

Son unos veinte números más una lista de ocho franjas. Guardarlos como veinte claves sueltas de DataStore sería tedioso y frágil; se guardan como **un solo texto en formato JSON**.

**Paso 1 — Habilitar la serialización.** En `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.1.20"                  # ← ya existe. Anotá el valor que tengas.
kotlinxSerialization = "1.8.1"

[libraries]
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

En el `build.gradle.kts` **del proyecto** (el de arriba de todo):

```kotlin
plugins {
    // … lo que ya estaba …
    alias(libs.plugins.kotlin.serialization) apply false
}
```

En el `build.gradle.kts` **del módulo `:app`**:

```kotlin
plugins {
    // … lo que ya estaba …
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    // … lo que ya estaba …
    implementation(libs.kotlinx.serialization.json)
}
```

> ⚠️ **El error número uno acá**: el plugin de serialización tiene que ir en **la misma versión que Kotlin**. Por eso en el `[plugins]` dice `version.ref = "kotlin"` y no una versión propia. Si no coinciden, Gradle falla con un mensaje sobre incompatibilidad de versiones del compilador que no dice nada útil.

**Paso 2 — Agregar el campo a los ajustes.** En `datos/Ajustes.kt`, sumá a la clase `Ajustes`:

```kotlin
val parametros: ParametrosFiscales = ParametrosFiscales.OFICIALES
```

Una clave nueva en el objeto `Claves`:

```kotlin
val PARAMETROS = stringPreferencesKey("parametros_fiscales")
```

Un `Json` configurado, arriba de la clase `RepositorioAjustes`:

```kotlin
private val json = Json {
    ignoreUnknownKeys = true   // que una versión futura pueda agregar campos
    encodeDefaults = true
}
```

En el `map { }` del flujo:

```kotlin
parametros = p[Claves.PARAMETROS]
    ?.let { texto -> runCatching { json.decodeFromString<ParametrosFiscales>(texto) }.getOrNull() }
    ?: ParametrosFiscales.OFICIALES,
```

Y dos funciones nuevas:

```kotlin
suspend fun guardarParametros(p: ParametrosFiscales) {
    contexto.almacen.edit { it[Claves.PARAMETROS] = json.encodeToString(p) }
}

suspend fun restaurarParametrosOficiales() {
    contexto.almacen.edit { it.remove(Claves.PARAMETROS) }
}
```

Necesitás estos imports en el archivo: `kotlinx.serialization.json.Json`, `kotlinx.serialization.encodeToString` y `uy.tunombre.sueldoliquido.dominio.ParametrosFiscales`.

**Dos decisiones defensivas que valen la pena entender:**

- **`runCatching { … }.getOrNull() ?: OFICIALES`** — si el JSON guardado está corrupto o quedó de una versión incompatible, la app vuelve a los oficiales en lugar de crashear al arrancar. Sin esto, un archivo dañado deja la app inutilizable hasta desinstalarla.
- **`restaurarParametrosOficiales()` borra la clave en lugar de escribir los oficiales.** Es más limpio: si el año que viene publicás una actualización con la BPC nueva, quien nunca tocó nada la recibe automáticamente, porque no hay nada guardado que le haga sombra.

### 6C.6 Validar antes de guardar

Agregá al final de `ParametrosFiscales.kt`:

```kotlin
/** Devuelve la lista de problemas. Vacía significa que los parámetros son usables. */
fun ParametrosFiscales.errores(): List<String> {
    val fallas = mutableListOf<String>()

    fun exigirTasa(valor: Double, nombre: String) {
        if (valor.isNaN() || valor < 0.0 || valor > 1.0) {
            fallas += "$nombre tiene que estar entre 0 % y 100 %."
        }
    }

    if (bpc <= 0.0) fallas += "La BPC tiene que ser mayor que cero."
    if (topeJubilatorio <= 0.0) fallas += "El tope jubilatorio tiene que ser mayor que cero."
    if (umbralFonasaEnBpc < 0.0) fallas += "El umbral de FONASA no puede ser negativo."
    if (umbralTasaDeduccionEnBpc < 0.0) fallas += "El umbral de la tasa de deducción no puede ser negativo."
    if (deduccionHijoAnualEnBpc < 0.0) fallas += "La deducción por hijo no puede ser negativa."
    if (deduccionHijoDiscapacidadAnualEnBpc < 0.0) fallas += "La deducción por discapacidad no puede ser negativa."

    exigirTasa(tasaJubilatoria, "El aporte jubilatorio")
    exigirTasa(tasaFrl, "El FRL")
    exigirTasa(tasaDeduccionAlta, "La tasa de deducción alta")
    exigirTasa(tasaDeduccionBaja, "La tasa de deducción baja")
    listOf(
        fonasaBajoSinConyuge to "FONASA (bajo el umbral, sin cónyuge)",
        fonasaBajoConConyuge to "FONASA (bajo el umbral, con cónyuge)",
        fonasaAltoSolo to "FONASA (sobre el umbral, sin cargas)",
        fonasaAltoConHijos to "FONASA (sobre el umbral, con hijos)",
        fonasaAltoConConyuge to "FONASA (sobre el umbral, con cónyuge)",
        fonasaAltoConAmbos to "FONASA (sobre el umbral, con ambos)"
    ).forEach { (valor, nombre) -> exigirTasa(valor, nombre) }

    if (escalaIrpf.isEmpty()) {
        fallas += "La escala de IRPF no puede quedar vacía."
    } else {
        if (escalaIrpf.last().hastaEnBpc != null) {
            fallas += "La última franja de IRPF no puede tener tope."
        }
        if (escalaIrpf.dropLast(1).any { it.hastaEnBpc == null }) {
            fallas += "Solo la última franja puede quedar sin tope."
        }
        val topes = escalaIrpf.dropLast(1).mapNotNull { it.hastaEnBpc }
        if (topes.any { it <= 0.0 }) {
            fallas += "Los topes de las franjas tienen que ser mayores que cero."
        }
        if (topes != topes.sorted() || topes.distinct().size != topes.size) {
            fallas += "Los topes de las franjas tienen que ir de menor a mayor, sin repetirse."
        }
        escalaIrpf.forEachIndexed { i, f -> exigirTasa(f.tasa, "La tasa de la franja ${i + 1}") }
    }

    val aportesMaximos = tasaJubilatoria + fonasaAltoConAmbos + tasaFrl
    if (aportesMaximos >= 1.0) {
        fallas += "Los aportes suman más del 100 % del nominal: el líquido daría negativo."
    }

    return fallas
}
```

La última comprobación es la que más veces te va a salvar: detecta la combinación de tasas que produce un sueldo líquido negativo, que es el tipo de resultado que hace que alguien crea que la app está rota.

### 6C.7 La pantalla de parámetros

Creá `PantallaParametros.kt`. Los campos se describen una sola vez en una lista y esa lista maneja tanto el formulario como la escritura: si mañana agregás un parámetro, tocás un solo lugar.

```kotlin
package uy.tunombre.sueldoliquido

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
import uy.tunombre.sueldoliquido.datos.Ajustes
import uy.tunombre.sueldoliquido.datos.RepositorioAjustes
import uy.tunombre.sueldoliquido.dominio.FranjaIrpf
import uy.tunombre.sueldoliquido.dominio.ParametrosFiscales
import uy.tunombre.sueldoliquido.dominio.errores
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
    Campo("bpc", "BPC del ejercicio", "Base de Prestaciones y Contribuciones. Se fija por decreto cada 1.º de enero. Al cambiarla se recalculan todas las franjas.", false,
        { it.bpc }, { p, v -> p.copy(bpc = v) }),
    Campo("tope", "Tope jubilatorio mensual", "Por encima de este monto no se realizan aportes jubilatorios obligatorios.", false,
        { it.topeJubilatorio }, { p, v -> p.copy(topeJubilatorio = v) })
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
        { it.deduccionHijoDiscapacidadAnualEnBpc }, { p, v -> p.copy(deduccionHijoDiscapacidadAnualEnBpc = v) })
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

    // Texto en edición, uno por campo. Se siembra una sola vez.
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
                Text(if (avanzadosVisibles) "Ocultar valores avanzados" else "Mostrar valores avanzados")
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
                    CampoParametro(campo, textos[campo.clave].orEmpty()) { textos[campo.clave] = it }
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
                            onValueChange = { if (i != tasas.lastIndex) topes[i] = it.filtrarDecimal() },
                            enabled = i != tasas.lastIndex,
                            label = { Text("Hasta (BPC)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).padding(end = 6.dp)
                        )
                        OutlinedTextField(
                            value = tasas[i],
                            onValueChange = { tasas[i] = it.filtrarDecimal() },
                            label = { Text("Tasa %") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).padding(start = 6.dp)
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
                        problemas.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (guardado) {
                Text("Valores guardados.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(12.dp))
            }

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
                    "ejercicio, y la página de tasas de FONASA. Los enlaces están en el " +
                    "repositorio del proyecto.",
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
        suffix = { Text(if (campo.esPorcentaje) "%" else "") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
```

Enganchá la pantalla al navegador. En `MainActivity.kt`, dentro del `NavHost`:

```kotlin
composable("parametros") {
    PantallaParametros(
        repositorio = repositorio,
        ajustes = actuales,
        onVolver = { navegador.popBackStack() }
    )
}
```

Y en `PantallaConfiguracion`, agregá una sección que lleve hasta ahí. La firma pasa a recibir `onAbrirParametros: () -> Unit`:

```kotlin
Encabezado(Icons.Filled.Info, "Valores y tasas")

Card(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
        Text(
            if (ajustes.parametros.sonPersonalizados)
                "Estás usando valores modificados por vos."
            else
                "Usando los valores oficiales del ejercicio ${ajustes.parametros.ejercicio}.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onAbrirParametros, modifier = Modifier.fillMaxWidth()) {
            Text("Ver y editar valores")
        }
    }
}
```

**Por qué el guardado es explícito acá y automático en la calculadora**: en la calculadora, un valor a medio escribir produce un resultado raro que el usuario ve y corrige en el acto. Acá, una tasa a medio escribir se guardaría y contaminaría **todos** los cálculos siguientes, incluso después de cerrar la app. El botón "Guardar valores" es la barrera entre las dos cosas.

### 6C.8 El aviso en la pantalla de resultados

Esta es la regla 2 y es la parte que no se puede omitir. En `PantallaCalculadora`, arriba de todo dentro del `Column`:

```kotlin
if (ajustes.parametros.sonPersonalizados) {
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
                    "${ParametrosFiscales.OFICIALES.ejercicio}, sino los que cargaste vos.",
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onAbrirParametros) { Text("Revisar") }
        }
    }
    Spacer(Modifier.height(16.dp))
}
```

Y el aviso legal del pie cambia de texto según el caso:

```kotlin
Text(
    if (ajustes.parametros.sonPersonalizados)
        "Cálculo hecho con valores cargados manualmente por vos, no con los oficiales. " +
            "No sirve como referencia de tu liquidación real."
    else
        "Cálculo estimativo con los parámetros vigentes ${ajustes.parametros.ejercicio}. " +
            "No sustituye la liquidación de tu empleador ni la información oficial de BPS y DGI.",
    style = MaterialTheme.typography.bodySmall
)
```

`sonPersonalizados` funciona con una sola comparación porque `ParametrosFiscales` es una `data class`: Kotlin genera el `equals` que compara los veinte campos y la lista de franjas. No hay que mantener ninguna bandera aparte, y por lo tanto no se puede desincronizar.

### 6C.9 Cuando publiques la actualización con los valores nuevos

Este es el punto que casi siempre se olvida en este tipo de funciones. Escenario: en enero el usuario cargó a mano la BPC de 2027. En marzo vos publicás la versión con los valores oficiales de 2027. Sin nada más, el JSON guardado le sigue haciendo sombra a los valores nuevos **para siempre**.

La solución no es pisar lo que el usuario cargó —podría haberlo hecho a propósito—, sino avisarle. En `PantallaConfiguracion`:

```kotlin
val hayEjercicioNuevo =
    ParametrosFiscales.OFICIALES.ejercicio > ajustes.parametros.ejercicio

if (hayEjercicioNuevo) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Hay valores oficiales nuevos", fontWeight = FontWeight.Bold)
            Text(
                "Esta versión de la app ya trae los valores del ejercicio " +
                    "${ParametrosFiscales.OFICIALES.ejercicio}. Estás usando los del " +
                    "${ajustes.parametros.ejercicio}, cargados a mano.",
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
}
```

Para que esto funcione, cada enero tenés que **subir el campo `ejercicio`** junto con la BPC en `ParametrosFiscales`. Si te olvidás de ese número, la comparación nunca se dispara y el aviso nunca aparece.

### 6C.10 Ejercicios anteriores (opcional)

Como toda la escala está expresada en BPC, recalcular un recibo de otro año es casi gratis: alcanza con cambiar dos números. Podés dejar unos accesos rápidos en la pantalla de parámetros:

```kotlin
private val BPC_POR_EJERCICIO = mapOf(
    2022 to 5_164.0,
    2023 to 5_660.0,
    2024 to 6_177.0,
    2025 to 6_576.0,
    2026 to 6_864.0
)
```

> ⚠️ **Cargar solo la BPC de otro año no reconstruye ese año.** El tope jubilatorio, y a veces la escala o las deducciones, también eran distintos. La deducción por hijo, por ejemplo, no siempre fue de 20 BPC anuales. Si vas a ofrecer ejercicios anteriores, cargá el conjunto completo de cada año desde el comunicado del BPS correspondiente, o dejá claro en la pantalla que es una aproximación.

### 6C.11 Tests nuevos

Agregá a `CalculadoraTest.kt`:

```kotlin
@Test
fun `los parametros oficiales son validos`() {
    assertTrue(ParametrosFiscales.OFICIALES.errores().isEmpty())
}

@Test
fun `subir la BPC arrastra todas las franjas`() {
    val subida = ParametrosFiscales.OFICIALES.copy(bpc = 7_500.0)
    // El mínimo no imponible pasa de 7 x 6.864 = 48.048 a 7 x 7.500 = 52.500
    assertEquals(0.0, Calculadora.impuestoPorFranjas(52_000.0, subida), delta)
    assertTrue(Calculadora.impuestoPorFranjas(52_000.0) > 0.0)
}

@Test
fun `parametros a medida cambian el resultado`() {
    val entrada = Entrada(nominal = 120_000.0)
    val oficial = Calculadora.calcular(entrada)
    val sinJubilatorio = Calculadora.calcular(
        entrada,
        ParametrosFiscales.OFICIALES.copy(tasaJubilatoria = 0.0)
    )
    assertEquals(0.0, sinJubilatorio.jubilatorio, delta)
    assertTrue(sinJubilatorio.liquido > oficial.liquido)
}

@Test
fun `la validacion detecta franjas desordenadas`() {
    val rotos = ParametrosFiscales.OFICIALES.copy(
        escalaIrpf = listOf(
            FranjaIrpf(30.0, 0.10),
            FranjaIrpf(10.0, 0.15),
            FranjaIrpf(null, 0.36)
        )
    )
    assertTrue(rotos.errores().isNotEmpty())
}

@Test
fun `la validacion detecta tasas imposibles`() {
    val rotos = ParametrosFiscales.OFICIALES.copy(tasaJubilatoria = 1.5)
    assertTrue(rotos.errores().isNotEmpty())
}

@Test
fun `la validacion detecta liquido negativo`() {
    val rotos = ParametrosFiscales.OFICIALES.copy(
        tasaJubilatoria = 0.60, fonasaAltoConAmbos = 0.45
    )
    assertTrue(rotos.errores().any { it.contains("100 %") })
}
```

Necesitás `import org.junit.Assert.assertTrue` arriba.

El segundo test es el que demuestra que el diseño en BPC funciona: con un solo campo modificado, el mínimo no imponible se movió y el impuesto cambió, sin haber tocado ninguna de las ocho franjas.

### 6C.12 Casos de prueba manuales

| # | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| 43 | Estado inicial | App recién instalada → Configuración → Ver y editar valores | BPC 6.864, tope 288.836, sin aviso de "valores modificados" |
| 44 | Cambiar la BPC | Poner 7.500, Guardar, volver a la calculadora con 120000 | El IRPF baja y el líquido sube. El subtítulo muestra la BPC nueva |
| 45 | Aviso visible | Con la BPC cambiada, mirar la pantalla de resultados | Aparece la tarjeta "Valores modificados" **arriba**, y el aviso legal cambió de texto |
| 46 | Persistencia | Forzar cierre y reabrir | Los valores modificados siguen ahí, y el aviso también |
| 47 | Restaurar | Tocar "Restaurar valores oficiales" | Todo vuelve a 2026 y el aviso desaparece |
| 48 | Campo vacío | Borrar la BPC y tocar Guardar | "Hay campos vacíos o mal escritos". **No se guarda nada** |
| 49 | BPC en cero | Poner 0 y Guardar | Error de validación. No se guarda |
| 50 | Tasa imposible | Aporte jubilatorio en 150 % | Error de validación |
| 51 | Líquido negativo | Jubilatorio 60 % y FONASA con ambos 45 % | Error indicando que los aportes superan el 100 % |
| 52 | Franjas desordenadas | Poner el tope de la franja 2 más chico que el de la 1 | Error de validación |
| 53 | Escala editada | Cambiar la tasa de la última franja a 40 % y probar un nominal de 900.000 | El impuesto sube solo en el tramo que supera 115 BPC |
| 54 | Decimales con coma | Escribir "0,5" en una tasa | Se acepta y se guarda bien |
| 55 | Sin autoguardado | Escribir un valor y volver atrás **sin** tocar Guardar | El cálculo sigue usando los valores anteriores |
| 56 | Aviso de ejercicio nuevo | Con parámetros de 2026 guardados a mano, subir `ejercicio` a 2027 en el código y reinstalar sobre la app | Aparece la tarjeta ofreciendo usar los oficiales nuevos |

El caso 55 es el que más gente rompe al implementar esto: si el borrador se guarda solo, la regla 3 se cae y cualquier tecleo a medias contamina el cálculo.


---

## 7. Pruebas

Acá es donde la app pasa de "anda en mi celular" a "puedo publicarla sin vergüenza".

### 7.1 Tests unitarios automatizados

Creá el archivo `app/src/test/java/uy/tunombre/sueldoliquido/dominio/CalculadoraTest.kt`. En la vista **Project** (no Android) es la carpeta `app/src/test`, la que dice "(test)".

```kotlin
package uy.tunombre.sueldoliquido.dominio

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraTest {

    private val delta = 0.01

    @Test
    fun `sueldo bajo, por debajo del umbral FONASA`() {
        val r = Calculadora.calcular(Entrada(nominal = 15_000.0))
        assertEquals(2_250.0, r.jubilatorio, delta)
        assertEquals(0.03, r.tasaFonasa, delta)   // 15.000 <= 17.160
        assertEquals(450.0, r.fonasa, delta)
        assertEquals(15.0, r.frl, delta)
        assertEquals(0.0, r.irpf, delta)          // por debajo del mínimo no imponible
        assertEquals(12_285.0, r.liquido, delta)
    }

    @Test
    fun `sueldo medio sin cargas, el credito anula el IRPF`() {
        val r = Calculadora.calcular(Entrada(nominal = 50_000.0))
        assertEquals(7_500.0, r.jubilatorio, delta)
        assertEquals(0.045, r.tasaFonasa, delta)
        assertEquals(2_250.0, r.fonasa, delta)
        assertEquals(50.0, r.frl, delta)
        // impuesto por franjas = 195,20 ; crédito = 9.800 x 14 % = 1.372 -> IRPF = 0
        assertEquals(0.0, r.irpf, delta)
        assertEquals(40_200.0, r.liquido, delta)
    }

    @Test
    fun `sueldo alto sin cargas, tasa de deduccion del 8 por ciento`() {
        val r = Calculadora.calcular(Entrada(nominal = 120_000.0))
        assertEquals(18_000.0, r.jubilatorio, delta)
        assertEquals(5_400.0, r.fonasa, delta)
        assertEquals(120.0, r.frl, delta)
        assertEquals(11_296.80, r.impuestoPorFranjas, delta)
        assertEquals(0.08, r.tasaDeduccion, delta)
        assertEquals(1_881.60, r.creditoDeducciones, delta)
        assertEquals(9_415.20, r.irpf, delta)
        assertEquals(87_064.80, r.liquido, delta)
    }

    @Test
    fun `tope de aportacion jubilatoria`() {
        val r = Calculadora.calcular(Entrada(nominal = 350_000.0))
        assertEquals(288_836.0 * 0.15, r.jubilatorio, delta)
        // FONASA y FRL sí van sobre el nominal completo
        assertEquals(350_000.0 * 0.045, r.fonasa, delta)
        assertEquals(350.0, r.frl, delta)
    }

    @Test
    fun `tasas FONASA por situacion familiar`() {
        val base = Entrada(nominal = 60_000.0)
        assertEquals(0.045, Calculadora.calcular(base).tasaFonasa, delta)
        assertEquals(0.06, Calculadora.calcular(base.copy(hijos = 1)).tasaFonasa, delta)
        assertEquals(0.065, Calculadora.calcular(base.copy(conyugeACargo = true)).tasaFonasa, delta)
        assertEquals(0.08, Calculadora.calcular(base.copy(conyugeACargo = true, hijos = 2)).tasaFonasa, delta)
    }

    @Test
    fun `atribucion de hijos al 50 por ciento`() {
        val entera = Calculadora.calcular(Entrada(nominal = 90_000.0, hijos = 2))
        val mitad = Calculadora.calcular(Entrada(nominal = 90_000.0, hijos = 2, atribucionHijos = 0.5))
        assertEquals(
            entera.totalDeducciones - mitad.totalDeducciones,
            Parametros.DEDUCCION_HIJO * 2 * 0.5,
            delta
        )
    }

    @Test
    fun `nominal cero no rompe nada`() {
        val r = Calculadora.calcular(Entrada(nominal = 0.0))
        assertEquals(0.0, r.liquido, delta)
        assertEquals(0.0, r.irpf, delta)
    }
}
```

Para ejecutarlos: clic derecho sobre el archivo → **Run 'CalculadoraTest'**. Corren en segundos, sin emulador. Deberían pasar los 7. Si alguno falla, el error te dice exactamente qué número esperaba y cuál obtuvo.

### 7.2 Casos de prueba manuales

Antes de publicar, recorré esto en el celular:

| # | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| 1 | Campo vacío | Abrir la app sin escribir nada | Todo en $ 0, sin crash |
| 2 | Nominal 15.000 | Ingresar 15000 | FONASA 3 %, IRPF $ 0, líquido $ 12.285 |
| 3 | Nominal 50.000 | Ingresar 50000 | FONASA 4,5 %, IRPF $ 0, líquido $ 40.200 |
| 4 | Nominal 120.000 | Ingresar 120000 | IRPF $ 9.415, líquido $ 87.065 |
| 5 | Cónyuge + hijos | 60000, switch cónyuge ON, 2 hijos | FONASA 8 % |
| 6 | Tope jubilatorio | 350000 | Jubilatorio $ 43.325 (no $ 52.500) |
| 7 | Número gigante | 999999999 | No crashea, muestra el resultado |
| 8 | Rotar pantalla | Con datos cargados, girar el celular | *(Ver nota abajo)* |
| 9 | Modo oscuro | Cambiar el tema del sistema | Todo legible, sin texto invisible |
| 10 | Fuente grande | Ajustes → Pantalla → Tamaño de fuente al máximo | Nada se corta ni se superpone |
| 11 | Sin conexión | Modo avión | Funciona igual (la app es 100 % offline) |
| 12 | Segundo plano | Salir y volver | La app no se cierra sola |

> **Nota sobre el caso 8**: con `remember` a secas, al rotar se pierden los datos. Si te molesta, cambiá cada `remember` por `rememberSaveable` (y agregá el import `androidx.compose.runtime.saveable.rememberSaveable`). Es un reemplazo directo.

### 7.3 Matriz de compatibilidad (Android 8 → Android 16)

Soportar un rango de nueve versiones de Android obliga a probar en los extremos, no solo en el equipo que tenés a mano. Esta es la matriz mínima:

| Nivel | Versión | Cómo probarlo | Qué mirar especialmente |
|---|---|---|---|
| **API 26** | Android 8.0 | Emulador `Pixel2_API26` | Que instale y abra. Que el ícono adaptativo se vea bien. Que los `Card` y `Switch` de Material 3 rendericen correctamente. |
| API 30–33 | Android 11–13 | Tu celular, o un emulador intermedio | Modo oscuro, tema dinámico (Material You aparece en API 31+) |
| **API 36** | Android 16 | Emulador `Pixel7_API36` | Edge-to-edge: que el contenido no quede tapado por la barra de estado o la de navegación |

Para cada nivel, corré al menos los casos 1 a 6 de la tabla de arriba y confirmá que los números dan igual. **El cálculo tiene que ser idéntico en todos**: es Kotlin puro, sin nada dependiente de la versión de Android. Si diera distinto, hay un problema de formato de números, no de fiscalidad.

Verificación rápida de que la app efectivamente se instala en el piso del rango:

```bash
# Con el emulador API 26 corriendo
adb devices
adb install app/build/outputs/apk/debug/app-debug.apk
```

Si el APK se rechaza con `INSTALL_FAILED_OLDER_SDK`, tu `minSdk` quedó por encima de 26: revisá `build.gradle.kts`.

**Un detalle que sí cambia entre versiones:** desde Android 15 (API 35) el modo *edge-to-edge* se aplica por defecto a las apps que apuntan a esa API o superior. Como tu `targetSdk` es 36, en equipos modernos el contenido se dibuja debajo de las barras del sistema. El `Scaffold` del código ya lo resuelve pasando su `padding` a la pantalla, pero verificalo visualmente en el emulador API 36: el título no tiene que quedar tapado por el reloj.

### 7.4 Validación contra fuentes oficiales

Este paso no es opcional. Compará al menos cinco sueldos distintos contra:

1. El **simulador de aportes del BPS** (bps.gub.uy → Aportación → Simulador de aportes)
2. La **escala de IRPF publicada por el BPS y la DGI** para el ejercicio en curso
3. **Recibos de sueldo reales** que puedas conseguir (el tuyo, el de alguien que te lo preste)

Si hay diferencias de pocos pesos, suele ser redondeo. Si hay diferencias grandes, revisá en este orden: tasa de FONASA aplicada, tasa de deducción (14 % vs 8 %), y si el recibo incluye partidas no gravadas.

### 7.5 Casos de prueba de la versión 1.1

Solo si hiciste la ampliación de la sección 6B.

**Persistencia**

| # | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| 13 | Guardado básico | Cargar 120000 y 2 hijos, cerrar la app **desde el selector de apps**, reabrir | Los campos vuelven con los mismos valores |
| 14 | Guardado tras muerte del proceso | Cargar datos y correr `adb shell am kill uy.tunombre.sueldoliquido`, reabrir | Los datos siguen ahí |
| 15 | Recordar desactivado | Apagar "Recordar mis datos", cambiar el nominal, cerrar y reabrir | Los campos vuelven al último valor guardado antes de apagar el switch, no al nuevo |
| 16 | Borrar datos | Tocar "Borrar los datos guardados", volver a la calculadora | Campos vacíos; el nivel de anuncios NO se modifica |
| 17 | Escritura con freno | Escribir un número largo de un tirón y observar logcat | Una sola escritura, no una por tecla |
| 18 | Primera instalación | Desinstalar e instalar de nuevo | Arranca vacío, sin anuncios, sin crash |

**Navegación**

| # | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| 19 | Ida y vuelta | Engranaje → flecha atrás | Vuelve a la calculadora con los datos intactos |
| 20 | Botón atrás del sistema | Engranaje → gesto/botón atrás | Igual que el punto anterior |
| 21 | Atrás en la calculadora | Estando en la calculadora, atrás | Cierra la app, no queda en una pantalla en blanco |
| 22 | Rotación en configuración | Girar el teléfono estando en configuración | Sigue en configuración, con el nivel seleccionado |

**Publicidad** (con los identificadores de prueba, verificando que aparezca la etiqueta "Test Ad")

| # | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| 23 | Estado por defecto | App recién instalada | "Nivel 0" seleccionado; ningún anuncio en ningún lado |
| 24 | Nivel 1 | Elegir "Nivel 1" y volver a la calculadora | Banner al pie, separado del contenido, sin tapar nada |
| 25 | Banner y teclado | Con el banner activo, tocar el campo de nominal | El banner **desaparece** mientras el teclado está abierto |
| 26 | Banner y scroll | Scrollear hasta el final del resultado | El banner queda fijo abajo, nunca encima de un botón o campo |
| 27 | Nivel 2, primera sesión | Instalar de cero, elegir "Nivel 2", cerrar y reabrir | **No** aparece anuncio: es la primera sesión |
| 28 | Nivel 2, después | Cerrar y reabrir una segunda vez | Aparece el anuncio de apertura |
| 29 | Freno de 4 horas | Cerrar y reabrir tres veces seguidas | El anuncio aparece **una sola vez**, no tres |
| 30 | Nivel 3 | Elegir "Nivel 3" | Banner al pie **y** anuncio al abrir, con los mismos frenos |
| 31 | Volver de segundo plano | Con nivel 2, minimizar y volver varias veces | No reaparece el anuncio de apertura |
| 32 | Configuración siempre libre | En los cuatro niveles, entrar a configuración | **Nunca** aparece un anuncio al entrar. En ningún nivel |
| 33 | Apagar | Volver a "Nivel 0", cerrar y reabrir | Ningún anuncio, ni banner ni apertura |
| 34 | Persistencia del nivel | Elegir Nivel 1, forzar cierre, reabrir | El nivel sigue en 1 |
| 35 | Sin conexión | Nivel 3 en modo avión | La app funciona con normalidad; ni anuncios ni errores ni huecos raros en el layout |

**Comparador (anuncio recompensado)**

| # | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| 36 | Texto previo | Ir a configuración sin desbloquear | Antes del botón se lee qué se pide (ver el video completo) y qué se obtiene (30 días) |
| 37 | Aceptación explícita | No tocar nada | **Nunca** se muestra un video sin que se toque el botón, en ningún nivel de publicidad |
| 38 | Video completo | Tocar el botón y mirar el video hasta el final | Se desbloquea el comparador y aparece la fecha de vencimiento |
| 39 | Video cerrado antes | Tocar el botón y cerrar el video a mitad | **No** se desbloquea nada; sin mensajes de error ni penalización |
| 40 | Sin inventario | Modo avión, tocar el botón | Aviso de que no hay video disponible; nada se rompe |
| 41 | Persistencia | Desbloquear, forzar cierre, reabrir | Sigue desbloqueado con la misma fecha |
| 42 | Independencia | Estar en Nivel 0 y desbloquear el comparador | Funciona: el recompensado no depende del nivel de publicidad |

**Botón de GitHub**

| # | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| 33 | Enlace correcto | Tocar "Ver el código en GitHub" | Abre el navegador en tu repositorio real, no en `TUUSUARIO` |
| 34 | Política de privacidad | Tocar el enlace | Abre la URL publicada y accesible |

El caso 29 es el más importante de todos. Un usuario en modo avión, o con datos agotados, tiene que poder calcular su sueldo sin enterarse de que existen los anuncios.

---

## 8. Pulido: ícono, nombre, aviso legal

### 8.1 Ícono

1. Clic derecho sobre la carpeta `res` → **New → Image Asset**.
2. Icon Type: **Launcher Icons (Adaptive and Legacy)**.
3. En **Foreground Layer** elegí *Clip Art* (hay íconos de calculadora y de dinero) o subí tu propia imagen PNG.
4. En **Background Layer** elegí un color sólido.
5. Ajustá el zoom para que el dibujo quede dentro de la zona segura. **Next → Finish**.

> **Ventaja concreta de `minSdk = 26`**: los íconos adaptativos se introdujeron exactamente en Android 8.0, así que **todos** tus usuarios los soportan. Podés dejar la opción "Legacy" generada por defecto, pero no dependés de ella: no hay ningún equipo en tu rango que necesite el ícono viejo.

Guardá aparte una versión **PNG de 512 × 512** del ícono: la vas a necesitar para la ficha de Play. Si lo dibujaste en Inkscape, exportalo así:

```bash
inkscape icono.svg --export-type=png --export-width=512 --export-height=512 -o icono-512.png
```

### 8.2 Aviso legal dentro de la app

Ya está en el código (el texto al pie). Verificá que se vea sin tener que scrollear demasiado. Google mira estas cosas en apps de temática financiera.

### 8.3 Capturas de pantalla

Con la app corriendo en el emulador o el celular, sacá **al menos 2** (idealmente 4–6) capturas:
- Pantalla vacía
- Un sueldo bajo calculado
- Un sueldo alto con el desglose de IRPF
- Modo oscuro

Requisitos de Play: PNG o JPEG, cada lado entre 320 y 3.840 px, relación 16:9 o 9:16. Una captura directa del celular cumple sin tocar nada.

Desde Ubuntu podés capturar la pantalla del celular o del emulador sin tocar el equipo:

```bash
adb exec-out screencap -p > captura-1.png
```

Y si tenés varios dispositivos conectados a la vez (por ejemplo el celular y dos emuladores), especificá cuál:

```bash
adb devices                          # ver los IDs
adb -s emulator-5554 exec-out screencap -p > captura-api26.png
```

---

## 9. Preparar el paquete firmado para publicar

### 9.1 Versionado

En `build.gradle.kts`:
- `versionCode` = número entero que **sube de a uno en cada subida** a Play. Play rechaza un versionCode repetido.
- `versionName` = lo que ve el usuario (`"1.0"`, `"1.1"`…).

### 9.2 Crear la clave de firma (keystore)

Toda app en Play va firmada digitalmente. Google va a manejar la clave final (*Play App Signing*), pero vos necesitás una **clave de subida**.

1. Menú **Build → Generate Signed App Bundle / APK…**
2. Elegí **Android App Bundle** → Next.
3. **Create new…**
4. Completá:
   - **Key store path**: una carpeta que **no** sea la del proyecto (para no subirla a GitHub por error). En Ubuntu, creala antes desde la terminal:

     ```bash
     mkdir -p ~/claves && chmod 700 ~/claves
     ```

     y usá la ruta `/home/TU_USUARIO/claves/sueldoliquido.jks`.
   - **Password** del keystore (anotala).
   - **Alias**: `subida`
   - **Password** de la clave (anotala).
   - **Validity**: 25 años o más.
   - Nombre y país (UY). El resto puede ir vacío.
5. Next → **Build Variant: release** → Create.

Después de generarlo, restringí los permisos del archivo:

```bash
chmod 600 ~/claves/sueldoliquido.jks
ls -l ~/claves/          # tiene que decir -rw-------
```

> 🔴 **Esto es lo más importante de toda la guía**: si perdés el archivo `.jks` o sus contraseñas, **no vas a poder publicar actualizaciones de tu app nunca más**. Guardá el archivo y las contraseñas en al menos dos lugares distintos (por ejemplo, un gestor de contraseñas y un pendrive o un drive privado). No lo subas a un repositorio público.

### 9.3 Dónde queda el archivo

Cuando termina, Android Studio muestra una notificación con un enlace *"locate"*. El archivo está en:

```
app/release/app-release.aab
```

Ese `.aab` (Android App Bundle) es lo que vas a subir a Play. **El APK ya no sirve para publicar**, solo para instalar a mano.

### 9.4 Probá el release antes de subirlo

Generá también un APK firmado en modo release (misma ventana, eligiendo APK) e instalalo en tu celular. A veces una app anda en debug y falla en release. Mejor descubrirlo ahora.

---

## 10. Política de privacidad (obligatoria)

Google exige una URL pública de política de privacidad para **todas** las apps, incluso las que no recolectan nada. Tiene que estar accesible sin login y no vencerse.

### 10.1 Texto base

Tu app no recolecta nada (es 100 % offline, sin permisos, sin internet), así que la política es corta. Adaptá esto:

> **Política de Privacidad — Sueldo Líquido UY**
>
> Última actualización: [fecha]
>
> **1. Información que recolectamos.** Esta aplicación no recolecta, almacena, transmite ni comparte ningún dato personal. Todos los cálculos se realizan localmente en el dispositivo. Los valores que ingresás no salen de tu teléfono ni se guardan.
>
> **2. Permisos.** La aplicación no solicita permisos del sistema ni acceso a internet.
>
> **3. Terceros.** No se utilizan servicios de análisis, publicidad ni rastreo de ningún tipo.
>
> **4. Menores.** La aplicación no está dirigida a menores de 13 años y no recolecta información de ellos.
>
> **5. Naturaleza de la información.** Los cálculos son estimativos, se basan en los parámetros fiscales publicados por el Banco de Previsión Social y la Dirección General Impositiva, y no constituyen asesoramiento contable, legal ni fiscal. No sustituyen la liquidación oficial de haberes. Esta aplicación no está afiliada, patrocinada ni avalada por ningún organismo del Estado uruguayo.
>
> **6. Cambios.** Cualquier modificación de esta política se publicará en esta misma dirección.
>
> **7. Contacto.** [tu email]

### 10.2 Segunda versión: si incluís anuncios (sección 6B)

En cuanto integrás AdMob, la política de arriba **deja de ser verdadera** y usarla sería declarar algo falso ante Google. Usá esta en su lugar:

> **Política de Privacidad — Sueldo Líquido UY**
>
> Última actualización: [fecha]
>
> **1. Datos que la aplicación recolecta por sí misma.** Ninguno. Los valores que ingresás (sueldo, cargas familiares) se procesan y se guardan **únicamente en tu dispositivo**, no se transmiten a ningún servidor y no son accesibles para el desarrollador. Podés borrarlos en cualquier momento desde Configuración, o desinstalando la aplicación.
>
> **2. Publicidad.** La aplicación **no muestra anuncios de forma predeterminada**. Si vos los activás desde la pantalla de Configuración, se utiliza el servicio Google AdMob. Cuando los anuncios están activos, Google puede acceder al identificador de publicidad del dispositivo y a datos técnicos (modelo, sistema operativo, país aproximado, idioma) para seleccionar y medir los anuncios. Ese tratamiento se rige por la política de privacidad de Google, disponible en https://policies.google.com/privacy y por https://support.google.com/admob/answer/6128543 . Podés desactivar los anuncios en cualquier momento desde Configuración, y restablecer o eliminar tu identificador de publicidad desde los ajustes de tu dispositivo Android.
>
> **3. Permisos.** La aplicación solicita acceso a internet, que se utiliza exclusivamente para cargar anuncios cuando vos los habilitaste. Con los anuncios desactivados, la aplicación funciona completamente sin conexión.
>
> **4. Consentimiento.** A los usuarios ubicados en el Espacio Económico Europeo, el Reino Unido y Suiza se les presenta un formulario de consentimiento antes de mostrar anuncios personalizados.
>
> **5. Menores.** La aplicación no está dirigida a menores de 13 años y no recolecta información de ellos de forma consciente.
>
> **6. Naturaleza de la información.** Los cálculos son estimativos, se basan en los parámetros fiscales publicados por el Banco de Previsión Social y la Dirección General Impositiva, y no constituyen asesoramiento contable, legal ni fiscal. No sustituyen la liquidación oficial de haberes. Esta aplicación no está afiliada, patrocinada ni avalada por ningún organismo del Estado uruguayo.
>
> **7. Cambios.** Cualquier modificación se publicará en esta misma dirección.
>
> **8. Contacto.** [tu email]

> Poné la fecha real de actualización y mantenela al día. Si más adelante agregás mediación con otras redes publicitarias, cada una tiene que aparecer nombrada acá.

### 10.3 Dónde hospedarla gratis

**Opción A — Google Sites** (la más simple): sites.google.com → nuevo sitio → pegar el texto → Publicar → copiar la URL.

**Opción B — GitHub Pages**: crear un repo público llamado `politica-privacidad`, subir un `index.md` con el texto, y en *Settings → Pages* activar la publicación desde la rama `main`. La URL queda `https://tuusuario.github.io/politica-privacidad/`.

Verificá que la URL abra en una ventana de incógnito antes de pegarla en Play Console.

---

## 11. Cuenta de Google Play Console

### 11.1 Qué tipo de cuenta elegir

| | Personal | Organización |
|---|---|---|
| Requisitos | Documento de identidad a tu nombre | Empresa registrada + **número D-U-N-S** |
| Trámite | Rápido | Semanas (obtener el D-U-N-S lleva tiempo) |
| Prueba cerrada obligatoria (12 testers / 14 días) | **Sí** | No |
| Nombre visible en la ficha | Tu nombre o un nombre de desarrollador | El de la empresa |

Para un proyecto personal: **cuenta Personal**. El costo de esa elección son los 14 días de prueba cerrada. **El tipo de cuenta no se puede cambiar después**, así que pensalo una vez y seguí.

### 11.2 Registro paso a paso

1. Andá a **https://play.google.com/console** e iniciá sesión con la cuenta de Google que vayas a usar a largo plazo (esta cuenta va a ser dueña de la app para siempre).
2. Activá la **verificación en dos pasos** en esa cuenta si todavía no la tenés: es requisito.
3. Elegí **Personal**.
4. Completá el perfil: nombre legal (tiene que coincidir con tu documento), dirección, teléfono, email de contacto.
5. Aceptá el **Acuerdo de Distribución para Desarrolladores de Google Play** y los Términos de Play Console.
6. Pagá los **US$ 25** con tarjeta de crédito o débito a tu nombre. No se aceptan prepagas.
7. **Verificación de identidad**: subí tu documento (cédula o pasaporte). Google puede pedirte además verificar el dispositivo desde la app móvil de Play Console. La revisión suele tardar entre 48 horas y una semana.

> Si el nombre en la tarjeta o el documento no coincide con el del perfil, la solicitud se rechaza y **la tarifa no se devuelve**. Revisá todo dos veces antes de pagar.

> **Dato de contexto**: desde 2026 Google está extendiendo la verificación de identidad de desarrolladores incluso a quienes distribuyen apps fuera de Play Store. Si publicás por Play, ya quedás cubierto con este mismo trámite.

---

## 12. Crear la app en Play Console y completar la ficha

Una vez aprobada la cuenta, en Play Console: **Crear app**.

### 12.1 Datos iniciales

- **Nombre de la app**: hasta 30 caracteres (ej: `Sueldo Líquido UY`)
- **Idioma predeterminado**: Español (Latinoamérica)
- **App o juego**: App
- **Gratuita o de pago**: Gratuita ⚠️ *una app gratuita no se puede pasar a paga después*
- Declaraciones sobre políticas y leyes de exportación de EE. UU.

### 12.2 El panel de tareas

Play Console te muestra una lista de tareas obligatorias. Hay que completarlas todas antes de poder publicar. Van una por una:

**a) Acceso a la app**
- Elegí *"Todas las funciones están disponibles sin restricciones de acceso"* (tu app no tiene login).

**b) Anuncios**
- **Versión sin AdMob**: *"No, mi app no contiene anuncios."*
- **Versión con AdMob (sección 6B)**: *"Sí, mi app contiene anuncios."* Esto es obligatorio **aunque vengan desactivados por defecto**: lo que se declara es la capacidad de mostrarlos, no la configuración inicial. Declarar que no y que Google detecte el SDK es causa de suspensión. Además, en la ficha de la tienda va a aparecer la etiqueta "Contiene anuncios", que no podés evitar.

**c) Clasificación de contenido**
- Cuestionario: categoría **Utilidad / Productividad / Comunicación**. Respondé "No" a todo lo de violencia, sexo, drogas, apuestas, contenido generado por usuarios. La clasificación sale automáticamente.

**d) Público objetivo y contenido**
- Rango de edad: **18 y más** (o 13+). No marques que va dirigida a menores: eso activa un montón de requisitos extra del programa "Diseñada para familias".

**e) Seguridad de los datos** (Data safety)

*Versión sin AdMob:*
- ¿Tu app recolecta o comparte datos de usuario? → **No**
- ¿Los datos están encriptados en tránsito? → no aplica
- ¿Hay forma de solicitar la eliminación de datos? → no aplica
- Pegá la **URL de la política de privacidad**

*Versión con AdMob (sección 6B):*
- ¿Tu app recolecta o comparte datos de usuario? → **Sí**
- Tipo de dato: **ID de dispositivo o de otro tipo → ID de dispositivo o de otro tipo** (es el identificador de publicidad, que recolecta el SDK de Google, no vos)
- ¿Se recolecta o se comparte? → **Compartido** con terceros (Google, para publicidad)
- Finalidad: **Publicidad o marketing**
- ¿Es obligatorio? → **Opcional**, porque el usuario elige activar los anuncios
- ¿Encriptado en tránsito? → **Sí** (el SDK usa HTTPS)
- ¿El usuario puede pedir que se eliminen? → indicá que puede restablecer o borrar su identificador de publicidad desde los ajustes de Android
- Los datos del sueldo que ingresa el usuario **no se declaran**: nunca salen del dispositivo

> El formulario de seguridad de los datos tiene que describir **todo el comportamiento de la app, incluidas las librerías de terceros**. La excusa "eso lo hace el SDK, no mi código" no existe para Google. Si tenés dudas sobre alguna casilla, la propia AdMob publica una guía de qué declarar para el formulario de Play.

**f) Apps de servicios financieros**
- Te van a preguntar si tu app ofrece servicios financieros (préstamos, inversiones, criptomonedas, seguros, pagos). **Respondé que no**: una calculadora informativa no es un servicio financiero. Ser preciso acá te evita que te pidan licencias regulatorias que no tenés.

**g) Apps gubernamentales**
- Declaración de que **no** es una app desarrollada por o en nombre de un gobierno. Importante que sea coherente con no usar marcas de BPS/DGI.

### 12.3 Ficha de Play Store (la parte de marketing)

| Elemento | Requisito | Sugerencia |
|---|---|---|
| Nombre | máx. 30 caracteres | `Sueldo Líquido UY` |
| Descripción breve | máx. 80 caracteres | `Calculá tu sueldo líquido en Uruguay: BPS, FONASA, FRL e IRPF.` |
| Descripción completa | máx. 4.000 caracteres | Ver plantilla abajo |
| Ícono | PNG 512 × 512 | El que exportaste en 8.1 |
| Gráfico destacado | PNG/JPEG **1024 × 500** | Fondo de color + el nombre de la app. Se hace en Canva en 10 minutos |
| Capturas de teléfono | mínimo 2, hasta 8 | Las de 8.3 |
| Categoría | Finanzas o Herramientas | Herramientas es más neutral |
| Email de contacto | obligatorio | Uno que revises |

**Plantilla de descripción completa:**

```
Calculá en segundos cuánto vas a cobrar de sueldo líquido en Uruguay a partir
de tu sueldo nominal.

QUÉ CALCULA
• Aporte jubilatorio (montepío) con el tope de cotización vigente
• FONASA, con la tasa que corresponde a tu situación familiar
• FRL (Fondo de Reconversión Laboral)
• IRPF, con la escala progresional y el crédito por deducciones
• Total de descuentos y líquido a cobrar

CARACTERÍSTICAS
• Desglose completo: ves de dónde sale cada número
• Deducciones por hijos a cargo, con atribución del 100 % o 50 %
• Fondo de Solidaridad y cajas profesionales
• Funciona sin internet
• No pide permisos ni recolecta ningún dato

Parámetros actualizados al ejercicio 2026 (BPC $ 6.864).

AVISO
Los resultados son estimativos y se basan en los parámetros publicados por el
Banco de Previsión Social y la Dirección General Impositiva. No sustituyen la
liquidación de tu empleador ni constituyen asesoramiento contable o fiscal.
Esta aplicación es independiente y no está afiliada a ningún organismo público.
```

### 12.4 Países

En *Producción → Países y regiones*, seleccioná **Uruguay** (y agregá Argentina/Brasil/España si querés que la vean uruguayos en el exterior). No hay razón para limitarla a un solo país, pero tampoco para publicarla en 190.

---

## 13. Prueba cerrada: 12 testers, 14 días

Este es el requisito que sorprende a todo el mundo, así que planificalo.

### 13.1 La regla, textual

Las cuentas **personales creadas después del 13 de noviembre de 2023** deben ejecutar una **prueba cerrada** con **un mínimo de 12 testers** que hayan estado **inscriptos de forma continua durante al menos 14 días**. Recién cumplido eso podés **solicitar acceso a producción**, que es un formulario que Google revisa.

Detalles que hacen fracasar a la gente:

- Los 14 días deben ser **corridos y ser los últimos 14** al momento de aplicar. Si un tester se da de baja el día 12 y quedás en 11, el conteo se rompe.
- Solo cuenta la pista de **prueba cerrada** (*closed testing*). La prueba interna (*internal testing*) **no** cuenta para el requisito.
- Tienen que ser 12 **cuentas de Google distintas y reales**, que se inscriban por el enlace y instalen en dispositivos reales. Emuladores y cuentas duplicadas no sirven.
- Google también evalúa si los testers **realmente usaron** la app. Cumplir el número es necesario, no suficiente: el formulario de solicitud pregunta cómo hiciste la prueba, qué aprendiste y qué cambiaste.

### 13.2 Cómo se hace, en orden

1. En Play Console: **Prueba → Prueba cerrada → Crear una versión**.
2. Activá **Play App Signing** cuando te lo ofrezca (aceptá; es lo recomendado y protege tu clave).
3. Subí el `app-release.aab`.
4. Escribí las notas de la versión (`Primera versión de prueba.`).
5. En la pestaña **Testers**, creá una lista de correos electrónicos y agregá los de tus testers.
6. Guardá y **publicá la versión** en la pista de prueba cerrada. Google la revisa (suele tardar de 1 a 3 días).
7. Copiá el **enlace de participación** (*opt-in URL*) y mandáselo a cada tester.
8. Cada tester tiene que: abrir el enlace **con la cuenta de Google que le pasaste a la lista**, aceptar ser tester, e instalar la app desde Play Store.
9. A partir del momento en que hay 12 inscriptos, empieza el conteo de 14 días.

### 13.3 De dónde saco 12 testers

- Familia, amigos, compañeros de trabajo: 12 personas es menos de lo que parece, pero cada una necesita **cuenta de Google en un Android real**.
- Reclutá **15 a 18**, no 12 exactos. Siempre hay quien se da de baja o cambia de teléfono.
- Comunidades de desarrolladores que hacen intercambio de testers (grupos de Reddit, Telegram, Discord dedicados a esto). Es gratis y funciona.
- Existen servicios pagos de testers. Funcionan, pero antes de gastar probá con tu círculo.

**Empezá a reclutar antes de terminar la app.** Las dos cosas corren en paralelo.

### 13.4 Solicitar acceso a producción

Cumplidos los 14 días, en el **Panel** aparece el botón para solicitar acceso a producción. El formulario te pregunta cosas como:
- cómo reclutaste a los testers,
- qué feedback recibiste,
- qué cambios hiciste a partir de ese feedback,
- por qué considerás que la app está lista.

**Respondé en concreto**, con ejemplos reales ("dos testers reportaron que el teclado tapaba el resultado; agregué scroll"). Las respuestas genéricas son la principal causa de rechazo entre quienes sí cumplieron los 14 días. La revisión demora aproximadamente una semana.

---

## 14. Producción: publicar de verdad

1. **Producción → Crear una versión nueva**.
2. Subí el `.aab` (podés reutilizar el mismo, o subir uno nuevo con `versionCode` incrementado).
3. Notas de la versión.
4. **Revisar versión → Iniciar lanzamiento en producción**.
5. Google revisa la app. Para una app simple, entre 1 y 7 días. La primera revisión de una cuenta nueva suele ser la más lenta.
6. Cuando aprueban, la app aparece en Play Store en unas horas.

Opcional pero recomendable: en vez de lanzar al 100 %, usá un **lanzamiento por etapas** (20 % de usuarios), mirá si hay reportes de fallos en *Calidad de la app → Android vitals*, y después subí al 100 %.

Si te rechazan, Play Console te dice exactamente qué política se incumplió y podés corregir y volver a enviar. Un rechazo no es el fin del mundo, pero acumular varios sí compromete la cuenta.

---

## 15. Mantenimiento anual

Poné dos recordatorios en el calendario. En serio, poné los recordatorios.

**🗓️ Cada enero — actualizar los parámetros fiscales**

1. El Poder Ejecutivo fija la nueva BPC (vigente desde el 1° de enero).
2. El BPS publica el comunicado con los valores y escalas de IRPF del ejercicio.
3. Abrí `ParametrosFiscales.kt`, cambiá `bpc`, `ejercicio` y `topeJubilatorio`. Las franjas de IRPF, los umbrales y las deducciones se recalculan solos porque están expresados en BPC.
4. Revisá si cambiaron: el tope de cotización jubilatoria, las tasas de FONASA, la deducción por hijo (en BPC) o la estructura de la escala. Si cambió algo estructural, ajustalo a mano.
5. Corré los tests, actualizá los valores esperados, subí `versionCode` y `versionName`, y publicá una actualización.

> **Si hiciste la sección 6C, este plazo deja de ser crítico.** El usuario que necesite la BPC nueva antes de que salga tu actualización puede cargarla él mismo, y cuando publiques la versión con los valores oficiales, la app le va a ofrecer adoptarlos. Eso convierte "tengo que publicar en enero sí o sí" en "conviene publicar en enero" — que es una diferencia enorme cuando el proyecto es un hobby.

**🗓️ Cada mitad de año — nivel de API objetivo**

Google sube el `targetSdk` mínimo todos los años, con fecha límite alrededor del 31 de agosto. En 2026 el piso es **Android 16 (API 36)** para apps nuevas y actualizaciones. Si no actualizás, no vas a poder subir nuevas versiones y la app deja de mostrarse a usuarios nuevos con celulares modernos. Suele ser cambiar un número en `build.gradle.kts`, probar y republicar.

---

## 16. Problemas frecuentes

### 16.1 Entorno (Ubuntu)

| Síntoma | Causa probable | Solución |
|---|---|---|
| `studio.sh` no arranca y no dice nada | Lo abriste desde el menú y perdiste el error | Ejecutalo desde terminal: `/opt/android-studio/bin/studio.sh` |
| Fuentes borrosas o escalado raro | XWayland en pantalla HiDPI | Ajustar tamaño de fuente del IDE, o probar `-Dawt.toolkit.name=WLToolkit` en las opciones de VM |
| Aviso de "external file changes monitoring" | Límite de inotify bajo | `fs.inotify.max_user_watches = 524288` en `/etc/sysctl.d/` (sección 4.1) |
| `/dev/kvm permission denied` | Tu usuario no está en el grupo `kvm` | `sudo adduser $USER kvm`, cerrar sesión y volver a entrar |
| `kvm-ok` dice que no se puede usar | Virtualización apagada, o VirtualBox/VMware en conflicto | Activar VT-x/AMD-V en BIOS; `lsmod \| grep -E "(vbox\|vmware)"` |
| El emulador arranca pero va lentísimo | Está corriendo sin KVM, o elegiste una imagen ARM | Verificar `kvm-ok`; usar imágenes **x86_64** |
| El emulador se cierra sin mensaje | Menos de 5 GB libres en disco | `df -h ~` y liberar espacio |
| Pantalla negra en el emulador | Problema de GPU bajo Wayland | `emulator @NOMBRE -gpu swiftshader_indirect` |
| `adb devices` muestra `no permissions` | Faltan reglas udev o el grupo `plugdev` | Sección 4.5, paso 0 |
| `adb devices` muestra `unauthorized` | Falta aceptar el diálogo en el celular | Revocar autorizaciones USB en el celular y reconectar |
| El celular ni aparece en `lsusb` | Cable de solo carga o puerto malo | Cambiar cable / puerto |
| `INSTALL_FAILED_USER_RESTRICTED` en Xiaomi | Falta "Instalar vía USB" | Activarla en Opciones de desarrollador (requiere cuenta Mi) |
| `JAVA_HOME` apunta a un Java que no querés | Tenés un `openjdk` de apt instalado | Dejar que Android Studio use su JBR: `File → Settings → Build Tools → Gradle → Gradle JDK` |

### 16.2 Proyecto y compatibilidad

| Síntoma | Causa probable | Solución |
|---|---|---|
| Gradle sync falla al crear el proyecto | Sin internet o proxy | Reintentar; `File → Invalidate Caches → Invalidate and Restart` |
| "SDK location not found" | Falta el SDK o `local.properties` | `Tools → SDK Manager`; verificar que `local.properties` apunte a `/home/TU_USUARIO/Android/Sdk` |
| Rojo en `SueldoLiquidoUYTheme` | El tema se llama distinto | `Alt+Enter` sobre el import y aceptar la sugerencia |
| `Unresolved reference 'icons'` en los `import androidx.compose.material.icons.*` | Material 3 ya no arrastra `material-icons-core` | Agregar `implementation("androidx.compose.material:material-icons-extended")` y sincronizar |
| `Unresolved reference 'aNumero'` / `'aEntero'` / `'CampoNumerico'` / `'Fila'` / `'moneda'` | Las funciones auxiliares quedaron `private` dentro de `MainActivity.kt` | Crear `Componentes.kt` (6B.5) y sacarles el `private` |
| `Unresolved reference 'anuncios'` o `'GestorAnuncios'` o `'BannerPublicitario'` | El paquete `anuncios` todavía no existe | Crearlo con los dos archivos de 6B.12, o quitar las referencias si aún no vas a poner publicidad |
| `No parameter with name 'X' found` en `Entrada(...)` | El nombre del campo en `Modelos.kt` no coincide exactamente | `Ctrl+clic` sobre `Entrada` y comparar letra por letra |
| `HorizontalDivider` no existe | Versión vieja de Material 3 | Usar `Divider()` en su lugar |
| *"Call requires API level 31 (current min is 26)"* | Usaste una API posterior a Android 8 | Buscar la alternativa de AndroidX, o envolver en `if (Build.VERSION.SDK_INT >= …)`. **No subas el `minSdk`** |
| `INSTALL_FAILED_OLDER_SDK` al instalar en el emulador API 26 | `minSdk` quedó por encima de 26 | Corregir `build.gradle.kts` |
| Los tests no aparecen para ejecutar | Archivo en la carpeta equivocada | Tiene que estar en `app/src/test/java/…`, no en `androidTest` |
| En Android 16 el título queda tapado por el reloj | Edge-to-edge no manejado | Verificar que el `padding` del `Scaffold` llegue a la pantalla (ya está en el código) |

### 16.3 Configuración y anuncios (sección 6B)

| Síntoma | Causa probable | Solución |
|---|---|---|
| La app crashea al arrancar con un error sobre `APPLICATION_ID` | Falta el `meta-data` en el manifiesto, o pusiste un Ad Unit ID donde va el App ID | Revisar que lleve `~` y no `/` |
| "There are multiple DataStores active for the same file" | Pusiste `preferencesDataStore` dentro de una clase | Tiene que ir a nivel de archivo, una sola vez |
| Los datos no se guardan | El switch "Recordar mis datos" está apagado, o cerraste antes de los 600 ms del freno | Verificar el switch; esperar un segundo antes de cerrar |
| Los campos se vacían al rotar | Usaste `remember` en vez de `rememberSaveable` | Cambiar por `rememberSaveable` |
| Nunca aparece ningún anuncio | Error de carga código 3 (sin relleno) | Es normal con IDs nuevos: pueden tardar hasta 24 h. Revisar logcat filtrando por `Ads` |
| El anuncio de apertura se repite al volver de segundo plano | Falta la bandera de evaluación | Usar `rememberSaveable` para `yaSeEvaluoApertura` |
| El anuncio de apertura nunca aparece | Estás en la primera sesión, o no pasaron 4 horas | Es el comportamiento correcto. Para probar: limpiar datos de la app y abrirla dos veces |
| El banner tapa un campo o queda sobre el teclado | No está en el `bottomBar`, o falta el chequeo de `isImeVisible` | Revisar 6B.5. Es riesgo de clic accidental |
| El recompensado no desbloquea nada | El usuario cerró el video antes del final | Es el comportamiento correcto del formato |
| AdMob restringe la app por "Rewards implementation – User choice" | El video se disparó sin aceptación explícita, o falta el texto previo | Revisar que solo se llame desde el botón y que se declare qué se pide y qué se obtiene |
| Aparece "Test Ad" en producción | Quedaron los identificadores de prueba | Cambiar los tres IDs (6B.15) |
| No aparece "Test Ad" durante el desarrollo | Estás usando tus IDs reales sin dispositivo de prueba | **No toques esos anuncios**; volvé a los IDs de prueba |
| `BuildConfig` no existe | Falta habilitarlo | `buildFeatures { buildConfig = true }` en el bloque `android` |
| Gradle falla con un error del plugin de serialización | La versión del plugin no coincide con la de Kotlin | En `libs.versions.toml`, el plugin debe usar `version.ref = "kotlin"` |
| "Serializer for class … not found" | Falta `@Serializable` en la clase, o el plugin no se aplicó al módulo `:app` | Revisar 6C.5 |
| Los valores editados no se aplican al cálculo | La calculadora sigue llamando a `calcular(entrada)` sin parámetros | Pasar `ajustes.parametros` como segundo argumento |
| La app arranca con los valores oficiales aunque el usuario los había cambiado | El JSON guardado no se pudo leer | Es el comportamiento defensivo esperado. Mirar logcat por un error de deserialización |
| El aviso de ejercicio nuevo nunca aparece | Te olvidaste de subir `ejercicio` al actualizar la BPC | Subir los dos juntos, siempre |
| El formulario de consentimiento nunca aparece en Europa | No creaste el mensaje en AdMob | Privacidad y mensajes → Regulaciones europeas → Crear mensaje |

### 16.4 Google Play

| Síntoma | Causa probable | Solución |
|---|---|---|
| "versionCode ya usado" | No incrementaste el número | Subir `versionCode` en 1 y recompilar |
| Rechazo por target API | `targetSdk` viejo | Poner `targetSdk = 36`, recompilar, resubir |
| "Tu app no cumple los requisitos de prueba" | Faltan los 14 días o hay menos de 12 inscriptos | Esperar / sumar testers de refuerzo |
| Rechazo por suplantación de identidad | Usaste "BPS"/"DGI" o logos oficiales | Renombrar, cambiar ícono y capturas, aclarar independencia en la descripción |

---

## 17. Checklist final

**Antes de compilar el release**
- [ ] Los 7 tests unitarios pasan
- [ ] Los 12 casos manuales verificados en un celular real
- [ ] **Probado en el emulador API 26 (Android 8) y en el API 36 (Android 16)**
- [ ] Contrastado contra el simulador del BPS y contra un recibo real
- [ ] `applicationId` definitivo (no `com.example`)
- [ ] `versionCode = 1`, `versionName = "1.0"`
- [ ] `minSdk = 26`, `targetSdk = 36`
- [ ] Ícono propio puesto
- [ ] Aviso legal visible en la app
- [ ] Sin permisos innecesarios en el `AndroidManifest.xml`

**Solo si hiciste la sección 6C (parámetros editables)**
- [ ] Casos 43 a 56 verificados
- [ ] Los 13 tests unitarios pasan (7 originales + 6 nuevos)
- [ ] El campo `ejercicio` de `ParametrosFiscales` coincide con el año de los valores cargados
- [ ] La tarjeta "Valores modificados" aparece **en la pantalla de resultados**, no solo en configuración
- [ ] El aviso legal del pie cambia de texto cuando los parámetros están modificados
- [ ] "Restaurar valores oficiales" devuelve todo a su lugar de un toque
- [ ] Ningún valor se guarda sin pasar la validación
- [ ] Salir de la pantalla sin tocar Guardar **no** cambia el cálculo

**Solo si hiciste la ampliación de la sección 6B**
- [ ] Casos 13 a 42 verificados
- [ ] `TUUSUARIO` reemplazado en las URL de GitHub y de la política de privacidad
- [ ] El repositorio existe, es público y el `.gitignore` excluye `*.jks`
- [ ] **Los identificadores de AdMob son los reales, no los de prueba** (los tres en `GestorAnuncios.kt`, más el App ID del `AndroidManifest.xml`)
- [ ] Ningún `setTestDeviceIds(...)` quedó en el código
- [ ] La app instalada desde cero arranca en **Nivel 0, sin publicidad**
- [ ] **Entrar a configuración no muestra ningún anuncio en ningún nivel**
- [ ] El anuncio de apertura no aparece en la primera sesión ni más de una vez cada 4 horas
- [ ] El banner se esconde con el teclado abierto y no toca ningún control
- [ ] Ningún texto de la app pide, sugiere ni agradece que se vean anuncios
- [ ] El video recompensado solo se dispara con un toque explícito, y el texto dice antes qué se pide y qué se obtiene
- [ ] **El comparador existe y funciona**: no prometas una recompensa que no entregás
- [ ] En modo avión la calculadora funciona igual
- [ ] Política de privacidad reemplazada por la versión con anuncios (10.2)
- [ ] En Play Console: "Contiene anuncios" = **Sí**
- [ ] Seguridad de los datos declara el identificador de publicidad
- [ ] Perfil de pagos e información fiscal completos en AdMob

**Antes de subir a Play**
- [ ] `.aab` firmado generado
- [ ] Keystore y contraseñas respaldados en dos lugares
- [ ] APK release probado en un celular
- [ ] Política de privacidad publicada y abriendo en incógnito
- [ ] Ícono 512×512, gráfico 1024×500, mínimo 2 capturas
- [ ] Descripción breve (≤80) y completa (≤4.000) escritas
- [ ] Nombre e ícono sin marcas oficiales

**Antes de solicitar producción**
- [ ] 12+ testers inscriptos de forma continua
- [ ] 14 días corridos cumplidos
- [ ] Feedback recibido y al menos un cambio hecho a partir de él
- [ ] Todas las tareas del panel de Play Console en verde
- [ ] Respuestas concretas y específicas en el formulario

---

## 18. Enlaces oficiales

**Desarrollo**
- Android Studio: https://developer.android.com/studio
- Curso oficial de Android con Compose (gratis, en español): https://developer.android.com/courses/android-basics-compose/course
- Documentación de Jetpack Compose: https://developer.android.com/develop/ui/compose/documentation

**Publicación**
- Play Console: https://play.google.com/console
- Requisitos de prueba para cuentas personales nuevas: https://support.google.com/googleplay/android-developer/answer/14151465
- Requisitos de nivel de API objetivo: https://support.google.com/googleplay/android-developer/answer/11926878
- Cómo empezar en Play Console: https://support.google.com/googleplay/android-developer/answer/6112435
- Políticas del programa para desarrolladores: https://play.google.com/about/developer-content-policy/

**Anuncios y persistencia (sección 6B)**
- AdMob: https://admob.google.com
- Guía de inicio de AdMob para Android: https://developers.google.com/admob/android/quick-start
- Banners adaptativos: https://developers.google.com/admob/android/banner/anchored-adaptive
- Anuncios recompensados: https://developers.google.com/admob/android/rewarded
- Políticas de anuncios con recompensa: https://support.google.com/admob/answer/7313578
- Anuncios de apertura de la app: https://developers.google.com/admob/android/app-open
- Habilitar anuncios de prueba: https://developers.google.com/admob/android/test-ads
- Implementaciones de intersticiales no permitidas: https://support.google.com/admob/answer/6201362
- Consentimiento en Europa (UMP): https://support.google.com/admob/answer/13554116
- DataStore: https://developer.android.com/topic/libraries/architecture/datastore
- Navigation Compose: https://developer.android.com/develop/ui/compose/navigation
- kotlinx.serialization: https://github.com/Kotlin/kotlinx.serialization

**Datos fiscales (revisar cada enero)**
- BPS – Tasas Fonasa: https://www.bps.gub.uy/10314/tasas-fonasa.html
- BPS – Topes de cotización: https://www.bps.gub.uy/10306/topes-de-cotizacion.html
- BPS – Simulador de aportes: https://www.bps.gub.uy/11084/simulador-de-aportes.html
- BPS – Comunicado con valores y escalas de IRPF del ejercicio: buscar "Comunicado valores escalas IRPF" en bps.gub.uy
- DGI: https://www.gub.uy/direccion-general-impositiva/

---

## Y ahora, el primer paso

No intentes hacer todo de una. El orden que menos frustración genera es:

1. **Hoy**: instalar Android Studio y crear el proyecto vacío. Que corra el "Hello Android".
2. **Después**: pegar los archivos de `dominio/` y hacer pasar los tests. Sin UI todavía. Cuando los 7 tests están en verde, ya tenés la parte difícil resuelta.
3. **Después**: la pantalla.
4. **En paralelo, desde ya**: crear la cuenta de Play Console y empezar a juntar testers.
