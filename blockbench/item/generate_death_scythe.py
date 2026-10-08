"""Génère la géométrie GeckoLib et la texture de la Death Scythe (première version, avant reprise dans Blockbench).

Usage, depuis la racine du dépôt : python blockbench/item/generate_death_scythe.py
Écrit geo/death_scythe.geo.json et textures/item/death_scythe.png dans src/main/resources/assets/astralpack.
Repère : 1 unité = 1 pixel (1/16 de bloc), Y vers le haut, la lame part vers +X, l'épaisseur est sur Z.
L'origine est la main (le milieu du manche) : le rendu pivote autour d'elle.
"""
import json
import math
import random
import struct
import zlib

RES = 'src/main/resources/assets/astralpack/'
TEX_W = TEX_H = 128

UV = {'dark': (0, 0), 'edge': (64, 0), 'white': (96, 0), 'violet': (64, 32)}

bones = {}


def bone(name, parent, pivot):
    bones[name] = {'name': name, 'pivot': list(pivot), 'cubes': []}
    if parent:
        bones[name]['parent'] = parent


def cube(b, frm, to, mat='dark', angle=0.0, pivot=None):
    c = {'origin': [round(v, 3) for v in frm],
         'size': [round(to[i] - frm[i], 3) for i in range(3)],
         'uv': list(UV[mat])}
    if angle:
        p = pivot if pivot else frm
        c['pivot'] = [round(v, 3) for v in p]
        c['rotation'] = [0, 0, round(angle, 3)]
    bones[b]['cubes'].append(c)


def chain(b, sx, sy, angles, lengths, widths, depth, mat='dark'):
    """Segments mis bout à bout ; l'angle est la rotation en Z (0 = vers le haut, positif = vers la gauche)."""
    for ang, length, w in zip(angles, lengths, widths):
        cube(b, (sx - w / 2, sy, -depth / 2), (sx + w / 2, sy + length, depth / 2), mat, ang, (sx, sy, 0))
        rad = math.radians(ang)
        sx -= length * math.sin(rad)
        sy += length * math.cos(rad)
    return sx, sy


def ring(b, cx, cy, half, depth=1.6):
    cube(b, (cx - half, cy - half, -depth / 2), (cx + half, cy + half, depth / 2))
    cube(b, (cx - half, cy - half, -depth / 2), (cx + half, cy + half, depth / 2), angle=45, pivot=(cx, cy, 0))
    inner = half * 0.66
    cube(b, (cx - inner, cy - inner, -depth / 2 - 0.15), (cx + inner, cy + inner, depth / 2 + 0.15), 'white')
    cube(b, (cx - inner, cy - inner, -depth / 2 - 0.15), (cx + inner, cy + inner, depth / 2 + 0.15), 'white',
         angle=45, pivot=(cx, cy, 0))
    arm = inner * 1.1
    cube(b, (cx - 0.45, cy - arm, -depth / 2 - 0.3), (cx + 0.45, cy + arm, depth / 2 + 0.3))
    cube(b, (cx - arm, cy - 0.45, -depth / 2 - 0.3), (cx + arm, cy + 0.45, depth / 2 + 0.3))


bone('weapon', None, (0, 0, 0))

# Manche rainuré et pointe recourbée
bone('handle', 'weapon', (0, 0, 0))
cube('handle', (-1, -15, -1), (1, 15, 1))
for i in range(8):
    y = -14 + i * 4
    cube('handle', (-1.25, y, -1.25), (1.25, y + 1, 1.25), 'violet' if i % 2 == 0 else 'dark')
cube('handle', (-1.5, -16, -1.5), (1.5, -14.8, 1.5))
cube('handle', (-1.5, 14.2, -1.5), (1.5, 15.4, 1.5), 'violet')
bone('hook', 'weapon', (0, -15, 0))
chain('hook', 0, -15, [180, 200, 225, 250], [2.5, 2.5, 2.5, 2.2], [1.9, 1.6, 1.3, 1.0], 1.9)

# Anneau du milieu, trois pointes vers l'avant
MID_Y = 19
bone('mid_ring', 'weapon', (0, MID_Y, 0))
ring('mid_ring', 0, MID_Y, 3.0)
bone('mid_spikes', 'weapon', (3, MID_Y, 0))
for off in (14, 0, -14):
    chain('mid_spikes', 3.0, MID_Y + off * 0.06, [-90 + off, -90 + off - 6], [2.2, 2.0], [1.0, 0.45], 0.9)

# Haut du manche, articulé, avec épines
bone('shaft', 'weapon', (0, MID_Y + 3, 0))
tx, ty = chain('shaft', 0, MID_Y + 3, [6, 14, 8, -6, -16, -20], [3.2, 3.2, 3.2, 3.2, 3.2, 3.0],
               [1.8, 1.6, 1.8, 1.6, 1.8, 1.6], 1.6)
sx, sy = 0.0, MID_Y + 3.0
for ang, length in zip([6, 14, 8, -6, -16, -20], [3.2, 3.2, 3.2, 3.2, 3.2, 3.0]):
    rad = math.radians(ang)
    sx -= length * math.sin(rad)
    sy += length * math.cos(rad)
    cube('shaft', (sx - 1.4, sy - 0.4, -0.5), (sx + 1.4, sy + 0.4, 0.5), angle=ang, pivot=(sx, sy, 0))
    cube('shaft', (sx + 0.7, sy - 0.2, -0.3), (sx + 2.0, sy + 0.2, 0.3), angle=ang - 30, pivot=(sx + 0.7, sy, 0))

# Anneau du haut, épines recourbées
TOP_X, TOP_Y = tx - 1.5, ty + 4.5
bone('top_ring', 'weapon', (TOP_X, TOP_Y, 0))
ring('top_ring', TOP_X, TOP_Y, 4.6, depth=1.8)
bone('top_spikes', 'weapon', (TOP_X, TOP_Y, 0))
for ang, start in ((8, (-1.2, 4.0)), (48, (-3.6, 3.4)), (92, (-4.5, 0.6)), (132, (-3.6, -2.6))):
    chain('top_spikes', TOP_X + start[0], TOP_Y + start[1], [ang, ang + 14, ang + 30], [3.4, 3.0, 2.6],
          [1.7, 1.2, 0.6], 1.0)

# Lame : arête dentelée près de l'anneau puis grande courbe vers le bas
bone('blade', 'weapon', (TOP_X + 4.5, TOP_Y + 1, 0))
sx, sy = TOP_X + 4.2, TOP_Y + 0.6
headings = [8, 2, -6, -14, -22, -30, -40, -50, -60, -70]
lengths = [4.2, 4.2, 4.2, 4.0, 4.0, 3.8, 3.6, 3.4, 3.0, 2.6]
up_w = [4.6, 5.2, 4.4, 3.8, 3.2, 2.7, 2.2, 1.7, 1.1, 0.5]
low_w = [3.4, 3.1, 2.5, 1.8, 1.2, 0.8, 0.6, 0.5, 0.45, 0.4]
for i, (h, length, uw) in enumerate(zip(headings, lengths, up_w)):
    lw = low_w[i]
    cube('blade', (sx, sy - lw, -0.55), (sx + length, sy + uw, 0.55), angle=h, pivot=(sx, sy, 0))
    cube('blade', (sx + 0.2, sy - lw - 0.7, -0.45), (sx + length - 0.1, sy - lw + 0.05, 0.45), 'edge', angle=h, pivot=(sx, sy, 0))
    if i in (0, 1, 2, 3, 4):
        cube('blade', (sx + 1.0, sy + uw - 0.4, -0.4), (sx + 2.4, sy + uw + 2.2 - i * 0.5, 0.4), angle=h - 12,
             pivot=(sx + 1.0, sy + uw - 0.4, 0))
    rad = math.radians(h)
    sx += length * math.cos(rad)
    sy += length * math.sin(rad)

# Décalage pour que l'origine soit la main : tout le modèle est déjà centré sur le manche (y = 0 au milieu)

geo = {
    'format_version': '1.12.0',
    'minecraft:geometry': [{
        'description': {
            'identifier': 'geometry.death_scythe',
            'texture_width': TEX_W,
            'texture_height': TEX_H,
            'visible_bounds_width': 8,
            'visible_bounds_height': 8,
            'visible_bounds_offset': [0, 1.5, 0]
        },
        'bones': [dict(bones[n]) for n in bones]
    }]
}
for b in geo['minecraft:geometry'][0]['bones']:
    if not b['cubes']:
        del b['cubes']
open(RES + 'geo/death_scythe.geo.json', 'w', encoding='utf-8', newline='\n').write(
    json.dumps(geo, indent=1) + '\n')


def write_png(path, pixels):
    h, w = len(pixels), len(pixels[0])
    raw = b''.join(b'\x00' + b''.join(bytes(px) for px in row) for row in pixels)

    def chunk(tag, data):
        c = struct.pack('>I', len(data)) + tag + data
        return c + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)

    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) \
        + chunk(b'IDAT', zlib.compress(raw, 9)) + chunk(b'IEND', b'')
    open(path, 'wb').write(png)


def clamp(v):
    return max(0, min(255, int(round(v))))


rnd = random.Random(7)
REGIONS = (((64, 0, 96, 32), (176, 150, 240), 14, True),
           ((96, 0, 128, 32), (240, 234, 255), 8, True),
           ((64, 32, 96, 64), (112, 64, 200), 12, True))
pixels = []
glow = []
for y in range(TEX_H):
    row = []
    grow = []
    for x in range(TEX_W):
        base, spread, emissive = (24, 24, 34), 7, False
        for (x0, y0, x1, y1), col, spr, em in REGIONS:
            if x0 <= x < x1 and y0 <= y < y1:
                base, spread, emissive = col, spr, em
        if not emissive and (x * 7 + y * 3) % 11 == 0:
            base = (46, 44, 62)
        k = rnd.uniform(-spread, spread)
        c = [clamp(base[0] + k), clamp(base[1] + k), clamp(base[2] + k)]
        row.append(c + [255])
        grow.append(c + [255] if emissive else [0, 0, 0, 0])
    pixels.append(row)
    glow.append(grow)
write_png(RES + 'textures/item/death_scythe.png', pixels)
write_png(RES + 'textures/item/death_scythe_glowmask.png', glow)
print('death_scythe: %d os, %d cubes' % (len(bones), sum(len(b['cubes']) for b in bones.values())))
