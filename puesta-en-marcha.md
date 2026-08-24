# Puesta en marcha en una máquina nueva

Cómo dejar el proyecto compilando en otra computadora, y qué cosas **no** viajan
en el repositorio.

El estado del proyecto y lo que falta están en la sección 2 de `CLAUDE.md`. Esto
es solo el arranque.

---

## 1. Clonar

```bash
git clone https://github.com/hsosa09/sueldo-liquido-uy.git SueldoLiquidoUY
cd SueldoLiquidoUY
```

Hay **dos repositorios remotos**, y la rama `play` vive en el segundo:

```bash
git remote add play https://github.com/hsosa09/sueldo-liquido-uy-play.git
git fetch play
git checkout -b play play/main
git checkout main
```

| Remoto | Rama | Qué es |
|---|---|---|
| `origin` | `main` | La 1.0, sin publicidad. Es lo que se sube primero a Play. |
| `play` | `main` (local: `play`) | La 1.1, con AdMob. Cambia cuatro archivos respecto de `main`. |

**`play` está atrasada.** El ícono, la paleta y los documentos de tienda entraron
solo en `main`. Antes de tocar la 1.1 hay que traerlos, o saldría con el robot
verde de Android Studio y el tema morado:

```bash
git checkout play
git merge main
```

No debería haber conflictos: los cuatro archivos en que las ramas difieren
—`GestorAnuncios.kt`, `BannerPublicitario.kt`, el manifiesto y
`app/build.gradle.kts`— no los tocó ninguno de estos cambios.

---

## 2. Lo que no está en el repositorio

Cuatro cosas, todas por buenas razones:

**`local.properties`** — está en `.gitignore` porque guarda la ruta del SDK, que
es distinta en cada máquina. Android Studio lo genera solo la primera vez que
abre el proyecto. Si se compila solo desde la terminal, hay que escribirlo:

```bash
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

**El keystore de firma** (`*.jks`) — nunca puede entrar al repositorio: si se
sube, queda en el historial para siempre y cualquiera puede firmar
actualizaciones falsas. Todavía no existe; se crea al preparar la subida a Play.
Cuando exista, **hay que respaldarlo aparte**: si se pierde, no se puede
actualizar más la app en Play, y no hay forma de recuperarlo.

**Los emuladores.** Ver el punto 4.

**Pillow**, que necesita `tienda/generar-icono.py`. Ver el punto 5.

---

## 3. Compilar

El proyecto necesita un JDK para *arrancar* Gradle, pero **no** para compilar:
`gradle.properties` tiene `org.gradle.java.installations.auto-detect=false` y
`app/build.gradle.kts` declara `jvmToolchain(17)`, así que Gradle se
aprovisiona su propio JDK 17 en `~/.gradle/jdks` y compila siempre con ese, sin
importar con cuál se lo haya lanzado. Eso es lo que hace que compilar desde la
terminal y desde Android Studio dé el mismo resultado.

Entonces alcanza con que exista *algún* JDK en el `PATH`:

```bash
sudo apt install openjdk-17-jdk    # o el que prefieras: solo lanza Gradle
./gradlew testDebugUnitTest
```

Si en la máquina nueva tampoco hay `java` —porque Android Studio está instalado
como flatpak, que es el caso de la máquina anterior— hay que apuntar `JAVA_HOME`
al JDK que Studio trae adentro:

```bash
export JAVA_HOME=/var/lib/flatpak/app/com.google.AndroidStudio/x86_64/stable/active/files/extra/jbr
```

Usar `active` y no el hash de la versión: el hash cambia con cada actualización
del flatpak, el symlink no.

Si aparece `JDK home directory does not exist: /app/extra/jbr`, el caché de
configuración de Gradle quedó con una ruta de adentro del sandbox del flatpak:

```bash
./gradlew --stop && rm -rf .gradle/configuration-cache .kotlin
```

Verificación de que quedó bien: **14 tests, todos en verde**.

```bash
./gradlew testDebugUnitTest assembleDebug
```

---

## 4. Emuladores

Ninguno viaja con el repositorio. Hacen falta dos, y el cálculo tiene que dar
idéntico en los dos: **API 26** (el `minSdk`) y **API 36** (el `targetSdk`).

```bash
~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager \
    "system-images;android-36;google_apis_playstore;x86_64"
~/Android/Sdk/cmdline-tools/latest/bin/avdmanager create avd \
    -n sueldo_capturas -k "system-images;android-36;google_apis_playstore;x86_64" -d pixel_7
```

Para las capturas de la ficha hay que forzar 1080 × 1920 —9:16 exacto, que es lo
que pide Play—; el procedimiento entero está en `tienda/ficha-play.md`.

**Las capturas que están en el repositorio hay que rehacerlas.** Se sacaron con
`android-37.2-beta3`, la única imagen que estaba descargada en la máquina
anterior. Sirven para revisar, pero no para subir a Play: pueden mostrar
elementos de sistema de una versión que todavía no salió.

---

## 5. El ícono

`tienda/generar-icono.py` rehace el PNG de 512 × 512 de Play y los
`mipmap-*dpi/*.webp` a partir del mismo dibujo que el ícono del lanzador.
Necesita Pillow:

```bash
pip install Pillow
python3 tienda/generar-icono.py
```

No hace falta correrlo salvo que se toque el ícono. Si alguien edita
`res/drawable/ic_launcher_foreground.xml` a mano y el script deja de coincidir,
el script **falla a propósito** en vez de generar un ícono distinto al del
teléfono.

---

## 6. Un detalle molesto de `.idea/`

`.idea/compiler.xml` y `.idea/misc.xml` están versionados y Android Studio los
reescribe solo, alternando entre JDK 17 y 21 según con qué se haya compilado
último. Aparecen como modificados sin que uno haya tocado nada.

No afectan al build —el toolchain manda, ver el punto 3—, así que lo más simple
es ignorar ese ruido y no commitearlo. Si molesta demasiado:

```bash
git rm --cached .idea/compiler.xml .idea/misc.xml
echo "/.idea/compiler.xml" >> .gitignore
echo "/.idea/misc.xml" >> .gitignore
```

---

## 7. Antes de tocar nada

Leer `CLAUDE.md`. Es la fuente de verdad del proyecto: los valores fiscales de
2026, los casos de prueba verificados a mano, y sobre todo la sección 7, que
tiene las reglas de publicidad a las que se llegó corrigiendo un plan que
violaba tres políticas de AdMob.

`pruebas-manuales.md` tiene lo que los tests no pueden cubrir: persistencia,
navegación, rotación, parámetros y publicidad.
