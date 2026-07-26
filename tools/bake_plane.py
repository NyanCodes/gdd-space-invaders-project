"""Bake a MagicaVoxel .obj export into the game's top-down plane sprite.

Matches the existing plane1/2/3.png: orthographic view straight down the
model's +Y axis, one pixel per voxel, each face flat-shaded by its
axis-aligned normal, nose pointing left.
"""
import sys
from PIL import Image

# Brightness per axis-aligned face normal. Top faces full, sides stepped down,
# which is what gives the voxel models their readable chunky relief.
SHADE = {(0, 1, 0): 1.00, (0, -1, 0): 0.45,
         (1, 0, 0): 0.80, (-1, 0, 0): 0.62,
         (0, 0, 1): 0.88, (0, 0, -1): 0.70}


def load_obj(path, palette_path):
    verts, norms, texs, faces = [], [], [], []
    for line in open(path):
        p = line.split()
        if not p:
            continue
        if p[0] == 'v':
            verts.append(tuple(float(v) for v in p[1:4]))
        elif p[0] == 'vn':
            norms.append(tuple(float(v) for v in p[1:4]))
        elif p[0] == 'vt':
            texs.append(tuple(float(v) for v in p[1:3]))
        elif p[0] == 'f':
            idx = []
            for tok in p[1:]:
                bits = (tok.split('/') + ['', ''])[:3]
                idx.append(tuple(int(b) - 1 if b else None for b in bits))
            faces.append(idx)
    pal = Image.open(palette_path).convert('RGBA')
    return verts, texs, norms, faces, pal


def bake(obj, palette_path, out, pitch=None):
    verts, texs, norms, faces, pal = load_obj(obj, palette_path)
    xs = [v[0] for v in verts]
    zs = [v[2] for v in verts]

    # One pixel per voxel: the voxel pitch is the smallest gap between
    # distinct coordinates along an axis.
    if pitch is None:
        uniq = sorted(set(round(x, 5) for x in xs))
        pitch = min(b - a for a, b in zip(uniq, uniq[1:]))

    x0, x1 = min(xs), max(xs)
    z0, z1 = min(zs), max(zs)
    W = int(round((x1 - x0) / pitch))
    H = int(round((z1 - z0) / pitch))

    img = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    px = img.load()
    depth = [[None] * W for _ in range(H)]

    for face in faces:
        vi = [f[0] for f in face]
        ti = [f[1] for f in face]
        ni = [f[2] for f in face]
        n = norms[ni[0]] if ni[0] is not None else (0, 1, 0)
        key = tuple(int(round(c)) for c in n)
        shade = SHADE.get(key, 0.8)

        # Palette colour: MagicaVoxel indexes a 256x1 texture by u.
        u = texs[ti[0]][0] if ti[0] is not None else 0
        r, g, b, a = pal.getpixel((min(255, max(0, int(u * 256))), 0))
        if a == 0:
            continue
        col = (int(r * shade), int(g * shade), int(b * shade), 255)

        fv = [verts[i] for i in vi]
        # Axis-aligned quads only, so a bounding box fill is exact.
        fx0 = min(v[0] for v in fv); fx1 = max(v[0] for v in fv)
        fz0 = min(v[2] for v in fv); fz1 = max(v[2] for v in fv)
        fy = max(v[1] for v in fv)  # depth: higher y wins (we look down +Y)

        ix0 = int(round((fx0 - x0) / pitch)); ix1 = int(round((fx1 - x0) / pitch))
        iz0 = int(round((fz0 - z0) / pitch)); iz1 = int(round((fz1 - z0) / pitch))
        # A face edge-on to the camera still covers one pixel row/column.
        if ix1 == ix0: ix1 = ix0 + 1
        if iz1 == iz0: iz1 = iz0 + 1

        for iz in range(max(0, iz0), min(H, iz1)):
            for ix in range(max(0, ix0), min(W, ix1)):
                if depth[iz][ix] is None or fy > depth[iz][ix]:
                    depth[iz][ix] = fy
                    px[ix, iz] = col

    # The models are modelled nose-right; the game draws them nose-left.
    img = img.transpose(Image.FLIP_LEFT_RIGHT)
    img.save(out)
    return img.size


if __name__ == '__main__':
    print(bake(*sys.argv[1:4]))
