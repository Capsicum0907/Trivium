#!/usr/bin/env python3
"""Draw every paxel item texture.

This script is the source of the sprites; the PNG files under
src/main/resources are its output and are not edited by hand. Adding a material
is adding one row to PALETTES, and changing the shape changes all of them at
once, which is the only way six sprites stay a set rather than six drawings.

Shape is described once, as regions of a 16x16 grid. Shading is derived rather
than drawn: a pixel with nothing above-left of it catches the light, a pixel
with nothing below-right of it falls into shadow, and everything else is body
colour. That rule is what keeps the six consistent no matter what the shape
becomes.

No third-party libraries: the PNG is assembled from zlib and struct, both of
which ship with Python.

    python tools/make_textures.py
"""

from __future__ import annotations

import pathlib
import struct
import zlib

SIZE = 16

OUT_DIR = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources/assets/trivium/textures/item"

# --- shape -----------------------------------------------------------------
# Coordinates are (x, y) with the origin at the top left, as the PNG is stored.

def _handle() -> set[tuple[int, int]]:
    """A two-pixel-wide shaft running from the bottom left up to the head."""
    return {(2 + k + d, 14 - k) for k in range(8) for d in (0, 1)}


def _spike() -> set[tuple[int, int]]:
    """The pick end: a tapering arm reaching up and to the left off the shaft.

    Three pixels across rather than one. A single-pixel diagonal at this size
    reads as a scratch on the handle instead of a separate prong.
    """
    rows = {
        2: range(4, 6),
        3: range(4, 7),
        4: range(5, 8),
        5: range(6, 9),
        6: range(7, 10),
    }
    return {(x, y) for y, xs in rows.items() for x in xs}


def _blade() -> set[tuple[int, int]]:
    """The axe end: a rounded wedge on the right of the shaft."""
    rows = {
        2: range(11, 13),
        3: range(11, 14),
        4: range(10, 15),
        5: range(10, 15),
        6: range(10, 14),
    }
    return {(x, y) for y, xs in rows.items() for x in xs}


def _scoop() -> set[tuple[int, int]]:
    """The shovel end: a flat-bottomed slab hanging below the blade."""
    rows = {
        7: range(10, 14),
        8: range(11, 14),
        9: range(11, 13),
    }
    return {(x, y) for y, xs in rows.items() for x in xs}


HEAD = _spike() | _blade() | _scoop()
HANDLE = _handle() - HEAD

# --- colour ----------------------------------------------------------------
# Each entry is (light, body, shadow) for the head. The handle is a stick in
# every case, so its three tones are shared.

HANDLE_TONES = ("#9C7A4E", "#7A5C3A", "#5A4128")

PALETTES = {
    "wooden": ("#B08C55", "#93713D", "#6B4E28"),
    "stone": ("#A8A8A8", "#8A8A8A", "#666666"),
    "iron": ("#E8E8E8", "#C8C8C8", "#9A9A9A"),
    "golden": ("#FDF06A", "#F0C41B", "#B58800"),
    "diamond": ("#8FF2E6", "#4AEDD9", "#2C9E92"),
    "netherite": ("#6E6260", "#4A4143", "#2C2427"),
}


def _rgba(colour: str) -> tuple[int, int, int, int]:
    value = colour.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), 255)


def _tone(pixel: tuple[int, int], region: set[tuple[int, int]], tones: tuple[str, str, str]) -> str:
    """Light where the region ends going up-left, shadow where it ends down-right."""
    x, y = pixel
    light, body, shadow = tones
    if (x - 1, y - 1) not in region:
        return light
    if (x + 1, y + 1) not in region:
        return shadow
    return body


# --- output ----------------------------------------------------------------

def _png(pixels: dict[tuple[int, int], tuple[int, int, int, int]]) -> bytes:
    raw = bytearray()
    for y in range(SIZE):
        raw.append(0)  # filter type 0 for the row
        for x in range(SIZE):
            raw.extend(pixels.get((x, y), (0, 0, 0, 0)))

    def chunk(kind: bytes, data: bytes) -> bytes:
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body))

    header = struct.pack(">IIBBBBB", SIZE, SIZE, 8, 6, 0, 0, 0)  # 8-bit RGBA
    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", header)
        + chunk(b"IDAT", zlib.compress(bytes(raw), 9))
        + chunk(b"IEND", b"")
    )


def draw(head_tones: tuple[str, str, str]) -> bytes:
    pixels: dict[tuple[int, int], tuple[int, int, int, int]] = {}
    for pixel in HANDLE:
        pixels[pixel] = _rgba(_tone(pixel, HANDLE, HANDLE_TONES))
    for pixel in HEAD:
        pixels[pixel] = _rgba(_tone(pixel, HEAD, head_tones))
    return _png(pixels)


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for prefix, tones in PALETTES.items():
        path = OUT_DIR / f"{prefix}_paxel.png"
        path.write_bytes(draw(tones))
        print(f"wrote {path.relative_to(OUT_DIR.parents[5])}")


if __name__ == "__main__":
    main()
