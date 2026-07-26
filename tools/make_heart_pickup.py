"""Draw the 16-frame spinning heart pickup.

Same shape as the other power-ups on screen: 32x32, an opaque black backdrop,
and sixteen frames of one full rotation. The heart turns about its vertical
axis, so the frames squash horizontally to a sliver edge-on and the reverse
face is shaded a little darker.

Run from the project root:  python3 tools/make_heart_pickup.py
"""
import math
import os
from PIL import Image

OUT = 'src/images/powerUps/heart'
SIZE = 32
FRAMES = 16
SS = 8               # supersample factor while drawing
FILL = (228, 26, 58)
LIGHT = (255, 122, 138)
DARK = (122, 8, 30)
EDGE = (58, 2, 14)
BANDS = 4            # flat shading steps across the highlight


def heart_mask(w, h):
    """Points inside the classic implicit heart, as a set of (x, y) pixels."""
    inside = set()
    for py in range(h):
        for px in range(w):
            # Map into [-1.4, 1.4], y up.
            x = (px - w / 2 + 0.5) / (w / 2) * 1.35
            y = -(py - h / 2 + 0.5) / (h / 2) * 1.35 + 0.25
            t = x * x + y * y - 1
            if t * t * t - x * x * y * y * y <= 0:
                inside.add((px, py))
    return inside


def draw_face(w, h, back):
    """One flat face of the heart, drawn big before it gets squashed."""
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    px = img.load()
    inside = heart_mask(w, h)
    for x, y in inside:
        # Edge if any 4-neighbour is outside.
        edge = any((x + dx, y + dy) not in inside
                   for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            col = EDGE
        else:
            # Highlight up and to the left, mirrored on the reverse face and
            # posterized into a few flat bands so it reads as pixel art rather
            # than an airbrushed gradient like the rest of the pickups.
            hx = (w - x) if back else x
            lit = 1.0 - min(1.0, math.hypot(hx - w * 0.34, y - h * 0.30) / (w * 0.45))
            band = round(max(0.0, lit) ** 1.4 * (BANDS - 1)) / (BANDS - 1)
            col = tuple(int(FILL[i] + (LIGHT[i] - FILL[i]) * band) for i in range(3))
            if back:
                col = tuple(int(DARK[i] + (c - DARK[i]) * 0.55) for i, c in enumerate(col))
        px[x, y] = col + (255,)
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    big = SIZE * SS
    front = draw_face(big, big, back=False)
    back = draw_face(big, big, back=True)

    for i in range(FRAMES):
        angle = 2 * math.pi * i / FRAMES
        squash = abs(math.cos(angle))
        face = front if math.cos(angle) >= 0 else back

        frame = Image.new('RGBA', (big, big), (0, 0, 0, 255))
        w = max(SS, int(round(big * squash)))
        if w > 0:
            scaled = face.resize((w, big), Image.BOX)
            frame.alpha_composite(scaled, ((big - w) // 2, 0))
        # Edge-on: a dark sliver so the spin reads instead of blinking out.
        # BOX keeps the flat bands flat; LANCZOS would ring and blur them.
        frame = frame.resize((SIZE, SIZE), Image.BOX).convert('RGBA')
        frame.save(f'{OUT}/heart{i + 1}.png')
    print(f'wrote {FRAMES} frames to {OUT}')


if __name__ == '__main__':
    main()
