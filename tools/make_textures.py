#!/usr/bin/env python3
"""Draw every paxel item texture.

This script is the source of the sprites; the PNG files under
src/main/resources are its output and are not edited by hand. Adding a material
is adding one row to PALETTES, and changing the shape changes all of them at
once, which is the only way six sprites stay a set rather than six drawings.

Shape is described once, as regions of a 16x16 grid, and shading is derived
from it rather than painted. The rule is read off the vanilla tools, whose
pixels were counted for this: every edge is dark, the edge that turns away
below-right is darkest of all, and the light sits just inside the lit edge
rather than on it. Five tones for the head, four for the shaft. That rule is
what keeps the six consistent no matter what the shape becomes.

The ramps are the vanilla ones, sampled from each material's own pickaxe, axe
and shovel. A paxel is seen in a row of slots beside those tools, so borrowing
their exact tones is what makes it look like it belongs there; inventing a
ramp would only make it look like it came from somewhere else.

The silhouette follows the one every other paxel uses - a blade above, an arm
hooking down its right side, a thin handle on the diagonal. Five of the six
mods surveyed draw that same skeleton, which makes it the genre rather than
anyone's design, and an item that does not look like its genre is harder to
recognise than one that does. What is ours is the squared foot at the end of
the arm and the drawn-out left point on the blade; the pixels are this
script's, not anyone else's.

No third-party libraries: the PNG is assembled from zlib and struct, both of
which ship with Python.

    python tools/make_textures.py
"""

from __future__ import annotations

import pathlib
import struct
import zlib
from typing import NamedTuple

SIZE = 16

OUT_DIR = pathlib.Path(__file__).resolve().parents[1] / "src/main/resources/assets/trivium/textures/item"

# --- shape -----------------------------------------------------------------
# Coordinates are (x, y) with the origin at the top left, as the PNG is stored.

HANDLE_LENGTH = 7


def _handle_anchors() -> list[tuple[int, int]]:
    """The left pixel of the shaft on each of its rows, bottom left up to the head.

    Three across rather than two, which is what every vanilla handle is: a dark
    side, a grained core and the side that turns away.
    """
    return [(2 + k, 14 - k) for k in range(HANDLE_LENGTH)]


def _blade() -> set[tuple[int, int]]:
    """The upper mass: a leaf with its tip at the top, widening as it comes down.

    Its left corner is drawn out to a point and its right corner runs into the arm
    below, so the one body reads as two ends rather than as a lump.
    """
    rows = {
        1: range(7, 9),
        2: range(6, 10),
        3: range(4, 11),
        4: range(3, 12),
        5: range(5, 13),
    }
    return {(x, y) for y, xs in rows.items() for x in xs}


def _arm() -> set[tuple[int, int]]:
    """The hook: an arm leaving the blade's right corner and curving down past the
    handle, ending in a squared foot rather than a taper."""
    rows = {
        6: range(9, 13),
        7: range(10, 14),
        8: range(11, 14),
        9: range(11, 15),
        10: range(11, 15),
        11: range(10, 15),
    }
    return {(x, y) for y, xs in rows.items() for x in xs}


HEAD = _blade() | _arm()

# --- colour ----------------------------------------------------------------
# Every ramp below is vanilla's own, read out of that material's pickaxe, axe and
# shovel. Nothing here is chosen by eye.


class Ramp(NamedTuple):
    """Head tones, lightest to darkest."""

    highlight: str
    light: str
    body: str
    dark: str
    outline: str


class Grain(NamedTuple):
    """Shaft tones. One fewer, because a stick has no specular."""

    light: str
    body: str
    dark: str
    outline: str


# The stick, which is the shaft of every vanilla tool and so of every paxel.
HANDLE_TONES = Grain("#896727", "#684E1E", "#493615", "#281E0B")

PALETTES = {
    "wooden": Ramp("#886626", "#755821", "#6B511F", "#372910", "#20180A"),
    "stone": Ramp("#9A9A9A", "#898989", "#7F7F7F", "#494949", "#181818"),
    "iron": Ramp("#FFFFFF", "#D8D8D8", "#C1C1C1", "#444444", "#181818"),
    "golden": Ramp("#FDFF76", "#EAEE57", "#E9B115", "#825D16", "#3F2E0E"),
    "diamond": Ramp("#33EBCB", "#2BC7AC", "#27B29A", "#0E3F36", "#082520"),
    # Netherite's own tools are speckled with two hues; these are the grey-brown
    # ones. The purple that also appears there belongs to the ingot, not the tool.
    "netherite": Ramp("#867B86", "#706770", "#5D565D", "#3B393B", "#231012"),
}


def _rgba(colour: str) -> tuple[int, int, int, int]:
    value = colour.lstrip("#")
    return (int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16), 255)


def _tone(pixel: tuple[int, int], region: set[tuple[int, int]], ramp: Ramp) -> str:
    """Where a pixel sits relative to the edges of its own region.

    The order is the whole of it. The outline claims a pixel before the rim does,
    so a corner that faces both ways reads as turning away; and the light is two
    steps in from the lit edge rather than on it, which is what stops the shape
    looking like a sticker.
    """
    x, y = pixel
    if (x + 1, y) not in region or (x, y + 1) not in region:
        return ramp.outline
    if (x - 1, y) not in region or (x, y - 1) not in region:
        return ramp.dark
    if (x - 1, y - 1) not in region:
        return ramp.highlight
    if (x - 2, y) not in region or (x, y - 2) not in region:
        return ramp.light
    return ramp.body


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


def draw(ramp: Ramp) -> bytes:
    pixels: dict[tuple[int, int], tuple[int, int, int, int]] = {}
    for row, (x, y) in enumerate(_handle_anchors()):
        # The core alternates by row. That is the grain: a shaft in one flat colour
        # is the thing that reads as plastic no matter how good the head is.
        core = HANDLE_TONES.light if row % 2 == 0 else HANDLE_TONES.body
        pixels[(x, y)] = _rgba(HANDLE_TONES.dark)
        pixels[(x + 1, y)] = _rgba(core)
        pixels[(x + 2, y)] = _rgba(HANDLE_TONES.outline)
    # The head is laid over the shaft, as the metal of a real one is.
    for pixel in HEAD:
        pixels[pixel] = _rgba(_tone(pixel, HEAD, ramp))
    return _png(pixels)


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for prefix, ramp in PALETTES.items():
        path = OUT_DIR / f"{prefix}_paxel.png"
        path.write_bytes(draw(ramp))
        print(f"wrote {path.relative_to(OUT_DIR.parents[5])}")


if __name__ == "__main__":
    main()
