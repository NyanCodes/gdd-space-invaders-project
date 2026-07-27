"""Derive the tier-2 through tier-5 gun pickups from the existing tier-1 flower.

Same 16-frame spin, hue-rotated body, and the baked-in "2X" label repainted to
"4X" / "6X" / "8X" / "10X" so the pickup says what it grants.
"""
from PIL import Image, ImageDraw, ImageFont
import colorsys, os

SRC = 'shots'
FONT = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 15)
TIERS = {
    'bolt': (28.0 / 360.0, '4X'),
    'charged': (285.0 / 360.0, '6X'),
    'eight': (140.0 / 360.0, '8X'),
    'ten': (190.0 / 360.0, '10X'),
}


def glyph_mask(im):
    """The white "2X" label and its dark outline.

    The frames sit on an opaque black backdrop, so "dark" alone would select
    the whole background. The backdrop is whatever dark pixels are reachable
    from the border; dark pixels enclosed by the flower are the label outline.
    """
    px = im.load(); w, h = im.size

    def dark(x, y):
        r, g, b, a = px[x, y]
        return a == 0 or max(r, g, b) < 70

    # Flood the backdrop inward from the border.
    backdrop = set()
    stack = [(x, y) for x in range(w) for y in (0, h - 1) if dark(x, y)]
    stack += [(x, y) for y in range(h) for x in (0, w - 1) if dark(x, y)]
    while stack:
        x, y = stack.pop()
        if (x, y) in backdrop or not (0 <= x < w and 0 <= y < h) or not dark(x, y):
            continue
        backdrop.add((x, y))
        stack += [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]

    mask = set()
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0 or (x, y) in backdrop:
                continue
            mx, mn = max(r, g, b), min(r, g, b)
            sat = 0 if mx == 0 else (mx - mn) / mx
            if (sat < 0.18 and mx > 150) or mx < 70:
                mask.add((x, y))
    return mask, backdrop


def inpaint(im, mask, backdrop):
    """Fill the label away with the nearest surrounding petal colour."""
    px = im.load(); w, h = im.size
    for x, y in sorted(mask):
        best = None
        for rad in range(1, 9):
            ring = []
            for dy in range(-rad, rad + 1):
                for dx in range(-rad, rad + 1):
                    if max(abs(dx), abs(dy)) != rad:
                        continue
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in mask \
                            and (nx, ny) not in backdrop:
                        r, g, b, a = px[nx, ny]
                        if a > 0:
                            ring.append((r, g, b, a))
            if ring:
                best = tuple(sum(c[i] for c in ring) // len(ring) for i in range(4))
                break
        if best:
            px[x, y] = best


def recolour(im, hue, backdrop):
    px = im.load(); w, h = im.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            # These frames have an opaque black backdrop rather than alpha, so
            # only the lit flower body is rotated — recolouring the backdrop
            # would turn its faint hatch into a visible coloured square.
            if a == 0 or (x, y) in backdrop:
                continue
            # Hue only: lightness and saturation carry the original shading.
            _, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            nr, ng, nb = colorsys.hls_to_rgb(hue, l, s)
            px[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)


def label(im, text):
    d = ImageDraw.Draw(im)
    box = d.textbbox((0, 0), text, font=FONT)
    x = (im.width - (box[2] - box[0])) // 2 - box[0]
    y = (im.height - (box[3] - box[1])) // 2 - box[1]
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            if dx or dy:
                d.text((x + dx, y + dy), text, font=FONT, fill=(20, 12, 30, 255))
    d.text((x, y), text, font=FONT, fill=(255, 255, 255, 255))


for name, (hue, text) in TIERS.items():
    os.makedirs(name, exist_ok=True)
    for i in range(1, 17):
        im = Image.open(f'{SRC}/shot{i}.png').convert('RGBA')
        mask, backdrop = glyph_mask(im)
        inpaint(im, mask, backdrop)
        recolour(im, hue, backdrop)
        label(im, text)
        im.save(f'{name}/{name}{i}.png')
    print(name, text, '- 16 frames')
