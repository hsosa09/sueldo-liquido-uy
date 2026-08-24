#!/usr/bin/env python3
"""Genera el ícono de 512 × 512 que pide Google Play.

Play no acepta un vector: quiere un PNG de 512 × 512 sin transparencia. El
ícono del teléfono, en cambio, es el vector de `app/src/main/res/drawable/`.
Son dos archivos distintos que tienen que verse igual, y no hay nada que los
mantenga sincronizados salvo este script: acá está el mismo dibujo, en las
mismas coordenadas, y al final una comprobación que falla si el XML dejó de
coincidir.

Uso:

    python3 tienda/generar-icono.py

Escribe `tienda/icono-play-512.png`. Depende de Pillow (`pip install Pillow`).
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

from PIL import Image, ImageDraw

RAIZ = Path(__file__).resolve().parent.parent
XML_FIGURA = RAIZ / "app/src/main/res/drawable/ic_launcher_foreground.xml"
XML_FONDO = RAIZ / "app/src/main/res/drawable/ic_launcher_background.xml"
SALIDA = Path(__file__).resolve().parent / "icono-play-512.png"

# --- La geometría, en el mismo espacio de 108 unidades que el vector ---

VERDE = "#0F766E"
BLANCO = "#FFFFFF"

# Recuadro que contiene al peso: (izq, arr, der, ab), radio, ancho de trazo.
RECUADRO = (29.0, 26.0, 79.0, 56.0)
RECUADRO_RADIO = 7.0
RECUADRO_TRAZO = 5.0

# El signo $ se dibuja en un espacio propio de 24 × 24 y después se transforma,
# igual que el <group> del XML.
GLIFO_ESCALA = 0.82
GLIFO_DX = 44.16
GLIFO_DY = 31.16
GLIFO_TRAZO = 3.0

# La S, como seis cubicas encadenadas. Cada tupla es (c1, c2, fin); el punto de
# partida es el final de la anterior.
S_INICIO = (17.0, 7.5)
S_CURVAS = [
    ((17.0, 5.3), (14.8, 4.0), (12.0, 4.0)),
    ((9.2, 4.0), (7.0, 5.6), (7.0, 8.0)),
    ((7.0, 10.4), (9.2, 11.6), (12.0, 12.0)),
    ((14.8, 12.4), (17.0, 13.6), (17.0, 16.0)),
    ((17.0, 18.4), (14.8, 20.0), (12.0, 20.0)),
    ((9.2, 20.0), (7.0, 18.7), (7.0, 16.5)),
]
S_BARRA = [(12.0, 1.5), (12.0, 22.5)]

# Flecha hacia abajo, ya en coordenadas del lienzo de 108.
FLECHA_TRAZO = 6.0
FLECHA_ASTA = [(54.0, 62.0), (54.0, 81.0)]
FLECHA_PUNTA = [(45.0, 71.0), (54.0, 81.0), (63.0, 71.0)]

# Cuánto del lienzo de 108 entra en el PNG. El lanzador solo muestra los 72
# centrales y el resto lo recorta; Play, en cambio, redondea apenas las
# esquinas. Con 84 el dibujo ocupa en el cuadrado de Play más o menos la misma
# proporción que ocupa en el círculo del lanzador.
LIENZO_VISIBLE = 84.0

LADO = 512
SUPERMUESTREO = 4


def cubica(p0, p1, p2, p3, pasos=48):
    """Puntos de una Bézier cúbica. Sin la primera, para no repetir el empalme."""
    puntos = []
    for i in range(1, pasos + 1):
        t = i / pasos
        u = 1.0 - t
        x = u**3 * p0[0] + 3 * u**2 * t * p1[0] + 3 * u * t**2 * p2[0] + t**3 * p3[0]
        y = u**3 * p0[1] + 3 * u**2 * t * p1[1] + 3 * u * t**2 * p2[1] + t**3 * p3[1]
        puntos.append((x, y))
    return puntos


def densificar(puntos, paso=0.25):
    """Rellena los tramos rectos, para que el trazo salga parejo."""
    salida = [puntos[0]]
    for (x0, y0), (x1, y1) in zip(puntos, puntos[1:]):
        largo = ((x1 - x0) ** 2 + (y1 - y0) ** 2) ** 0.5
        n = max(1, int(largo / paso))
        for i in range(1, n + 1):
            t = i / n
            salida.append((x0 + (x1 - x0) * t, y0 + (y1 - y0) * t))
    return salida


class Lienzo:
    """Dibuja en coordenadas de 108 unidades sobre una imagen supermuestreada.

    El trazo se estampa como una sucesión de discos en vez de usar `line()` con
    ancho: así las puntas y los codos salen redondeados sin casos especiales,
    que es exactamente lo que hace `strokeLineCap="round"` en el vector.
    """

    def __init__(self, lado, supermuestreo, visible):
        self.escala = lado * supermuestreo / visible
        self.desfase = (108.0 - visible) / 2.0
        self.imagen = Image.new("RGB", (lado * supermuestreo,) * 2, VERDE)
        self.pincel = ImageDraw.Draw(self.imagen)

    def punto(self, p):
        return ((p[0] - self.desfase) * self.escala, (p[1] - self.desfase) * self.escala)

    def trazo(self, puntos, ancho, color=BLANCO):
        radio = ancho * self.escala / 2.0
        for p in densificar(puntos):
            x, y = self.punto(p)
            self.pincel.ellipse((x - radio, y - radio, x + radio, y + radio), fill=color)

    def recuadro(self, caja, radio, ancho, color=BLANCO):
        izq, arr, der, ab = caja
        x0, y0 = self.punto((izq, arr))
        x1, y1 = self.punto((der, ab))
        self.pincel.rounded_rectangle(
            (x0, y0, x1, y1),
            radius=radio * self.escala,
            outline=color,
            width=max(1, round(ancho * self.escala)),
        )

    def terminar(self, lado):
        return self.imagen.resize((lado, lado), Image.LANCZOS)


def glifo_peso():
    """Los dos trazos del $, ya llevados al lienzo de 108."""
    s = [S_INICIO]
    for c1, c2, fin in S_CURVAS:
        s.extend(cubica(s[-1], c1, c2, fin))

    def llevar(puntos):
        return [(GLIFO_DX + GLIFO_ESCALA * x, GLIFO_DY + GLIFO_ESCALA * y) for x, y in puntos]

    return llevar(s), llevar(S_BARRA)


def comprobar_xml():
    """Falla si el vector del teléfono dejó de coincidir con este dibujo.

    No compara el dibujo entero: alcanza con los números que se cambian a mano
    —la transformación del $, el color del fondo, los anchos de trazo— para que
    un retoque en el XML no pase inadvertido y los dos íconos se separen.
    """
    figura = XML_FIGURA.read_text(encoding="utf-8")
    fondo = XML_FONDO.read_text(encoding="utf-8")
    problemas = []

    def exigir(texto, patron, que):
        if not re.search(patron, texto):
            problemas.append(que)

    exigir(fondo, rf'fillColor="{VERDE}"', f"el fondo del XML ya no es {VERDE}")
    exigir(figura, rf'translateX="{GLIFO_DX}"', f"translateX del $ ya no es {GLIFO_DX}")
    exigir(figura, rf'translateY="{GLIFO_DY}"', f"translateY del $ ya no es {GLIFO_DY}")
    exigir(figura, rf'scaleX="{GLIFO_ESCALA}"', f"scaleX del $ ya no es {GLIFO_ESCALA}")
    exigir(figura, rf'strokeWidth="{RECUADRO_TRAZO:g}"', "el trazo del recuadro cambió")
    exigir(figura, rf'strokeWidth="{FLECHA_TRAZO:g}"', "el trazo de la flecha cambió")
    exigir(figura, rf'strokeWidth="{GLIFO_TRAZO:g}"', "el trazo del $ cambió")

    if problemas:
        print("El vector del lanzador y este script se separaron:", file=sys.stderr)
        for p in problemas:
            print(f"  · {p}", file=sys.stderr)
        print(
            "\nActualizá tienda/generar-icono.py para que vuelvan a coincidir, o el\n"
            "ícono de Play va a mostrar un dibujo distinto al del teléfono.",
            file=sys.stderr,
        )
        sys.exit(1)


def dibujar(lado, visible=LIENZO_VISIBLE):
    """El ícono completo, en RGB y sin recortar."""
    lienzo = Lienzo(lado, SUPERMUESTREO, visible)
    lienzo.recuadro(RECUADRO, RECUADRO_RADIO, RECUADRO_TRAZO)

    s, barra = glifo_peso()
    lienzo.trazo(s, GLIFO_TRAZO)
    lienzo.trazo(barra, GLIFO_TRAZO)

    lienzo.trazo(FLECHA_ASTA, FLECHA_TRAZO)
    lienzo.trazo(FLECHA_PUNTA, FLECHA_TRAZO)

    return lienzo.terminar(lado)


def recortar(imagen, forma):
    """Devuelve la imagen con todo lo que queda fuera de `forma` transparente."""
    lado = imagen.size[0]
    grande = lado * SUPERMUESTREO
    mascara = Image.new("L", (grande, grande), 0)
    pincel = ImageDraw.Draw(mascara)
    if forma == "circulo":
        pincel.ellipse((0, 0, grande - 1, grande - 1), fill=255)
    else:
        pincel.rounded_rectangle((0, 0, grande - 1, grande - 1), radius=grande * 0.20, fill=255)

    salida = imagen.convert("RGBA")
    salida.putalpha(mascara.resize((lado, lado), Image.LANCZOS))
    return salida


def generar_mipmaps():
    """Rehace los `ic_launcher*.webp` de `res/mipmap-*dpi/`.

    En un `minSdk` 26 estos archivos no se usan nunca: `mipmap-anydpi` tiene
    prioridad sobre cualquier calificador de densidad, así que el sistema
    siempre arma el ícono con el vector adaptativo. Se regeneran igual para que
    no quede el robot verde de Android Studio dentro del APK ni en el
    repositorio, esperando a que alguien lo encuentre en una captura.
    """
    densidades = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
    for densidad, lado in densidades.items():
        carpeta = RAIZ / "app/src/main/res" / f"mipmap-{densidad}"
        carpeta.mkdir(exist_ok=True)
        base = dibujar(lado)
        recortar(base, "cuadrado").save(carpeta / "ic_launcher.webp", "WEBP", lossless=True)
        recortar(base, "circulo").save(carpeta / "ic_launcher_round.webp", "WEBP", lossless=True)
    print(f"mipmap-*dpi · {len(densidades) * 2} archivos regenerados")


def main():
    comprobar_xml()

    imagen = dibujar(LADO)
    imagen.save(SALIDA, "PNG", optimize=True)
    print(f"{SALIDA.relative_to(RAIZ)} · {imagen.size[0]} × {imagen.size[1]} · sin transparencia")

    generar_mipmaps()


if __name__ == "__main__":
    main()
