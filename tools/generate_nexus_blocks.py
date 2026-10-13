"""Génère les ressources des blocs du portail du Nexus : textures, modèles, états de blocs, recettes, butin, tags.

Usage : python tools/generate_nexus_blocks.py   (nécessite Pillow)
"""
import json
import math
import os
import random

from PIL import Image

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'astralpack')
DATA = os.path.join(REPO, 'src', 'main', 'resources', 'data')


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


def save_png(img, rel):
    path = os.path.join(ASSETS, 'textures', 'block', rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


rng = random.Random(7)

# ---- Bloc d'anneau : pierre sombre violacée, bordure de dalle, rune lumineuse ----
ring = Image.new('RGBA', (16, 16))
px = ring.load()
for x in range(16):
    for y in range(16):
        base = 34 + rng.randint(-4, 4)
        px[x, y] = (base, base - 2, base + 22, 255)
        if x in (0, 15) or y in (0, 15):
            px[x, y] = (22, 20, 44, 255)
        elif x in (1, 14) or y in (1, 14):
            px[x, y] = (52, 48, 92, 255)
for dx, dy, col in [(0, 0, (102, 224, 255)), (1, 0, (102, 224, 255)), (-1, 0, (102, 224, 255)),
                    (0, 1, (102, 224, 255)), (0, -1, (102, 224, 255)), (0, 0, (220, 250, 255))]:
    px[7 + dx, 7 + dy] = col + (255,)
    px[8 + dx, 8 + dy] = col + (255,)
for x, y in [(3, 3), (12, 3), (3, 12), (12, 12), (5, 8), (10, 7)]:
    px[x, y] = (180, 140, 255, 255)
save_png(ring, 'astral_ring_block.png')

# ---- Pupitre : atlas 16 x 16 (pierre sombre, pierre à rune, panneau violet, métal argenté) ----
con = Image.new('RGBA', (16, 16))
px = con.load()
for x in range(16):
    for y in range(16):
        if y < 8 and x < 8:
            b = 30 + rng.randint(-3, 3)
            px[x, y] = (b, b - 2, b + 20, 255)
        elif y < 8:
            b = 46 + rng.randint(-3, 3)
            px[x, y] = (b, b - 2, b + 26, 255)
        elif y < 12:
            t = (x + y) % 4
            px[x, y] = [(110, 70, 220, 255), (140, 100, 245, 255), (170, 130, 255, 255), (140, 100, 245, 255)][t]
        else:
            b = 170 + rng.randint(-8, 8) - (y - 12) * 10
            px[x, y] = (b, b, b + 18, 255)
for x in range(8, 16):
    px[x, 3] = (102, 224, 255, 255)
    px[x, 4] = (102, 224, 255, 255)
save_png(con, 'nexus_console.png')

# ---- Surface du portail : tourbillon animé (8 images de 16 x 16, translucide) ----
frames = 8
portal = Image.new('RGBA', (16, 16 * frames))
px = portal.load()
for f in range(frames):
    phase = 2 * math.pi * f / frames
    for x in range(16):
        for y in range(16):
            dx, dy = x - 7.5, y - 7.5
            dist = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            v = math.sin(dist * 0.9 - phase + ang * 2.0) * 0.5 + 0.5
            w = math.sin((x + y) * 0.55 + phase * 2) * 0.5 + 0.5
            r = int(60 + 110 * v + 40 * w)
            g = int(70 + 90 * w)
            b = int(200 + 55 * v)
            a = int(130 + 80 * v)
            px[x, f * 16 + y] = (min(r, 255), min(g, 255), min(b, 255), min(a, 235))
save_png(portal, 'nexus_portal.png')
write_json(os.path.join(ASSETS, 'textures', 'block', 'nexus_portal.png.mcmeta'),
           {"animation": {"frametime": 3, "interpolate": True}})

# ---- Modèles ----
REG = {'dark': [0, 0, 8, 8], 'rune': [8, 0, 16, 8], 'glow': [0, 8, 16, 12], 'metal': [0, 12, 16, 16]}


def box(frm, to, region, rotation=None):
    faces = {d: {"uv": REG[region], "texture": "#0"} for d in ("north", "south", "east", "west", "up", "down")}
    element = {"from": frm, "to": to, "faces": faces}
    if rotation:
        element["rotation"] = rotation
    return element


console_base = [
    box([1, 0, 1], [15, 3, 15], 'dark'),
    box([2, 3, 2], [14, 4, 14], 'rune'),
    box([4, 4, 4], [12, 9, 12], 'dark'),
    box([3, 9, 3], [13, 11, 13], 'rune', {"angle": -22.5, "axis": "x", "origin": [8, 10, 8]}),
    box([7, 11, 7], [9, 11.4, 9], 'glow'),
]
planted_extra = [
    box([7.4, 11, 7.4], [8.6, 27, 8.6], 'metal'),
    box([3.5, 24.5, 7.4], [8.6, 27, 8.6], 'dark'),
    box([1, 20.5, 7.4], [4.5, 24.5, 8.6], 'dark'),
    box([1, 20, 7.5], [3, 21, 8.5], 'glow'),
]
tex = {"0": "astralpack:block/nexus_console", "particle": "astralpack:block/nexus_console"}
write_json(os.path.join(ASSETS, 'models', 'block', 'nexus_console.json'),
           {"textures": tex, "elements": console_base})
write_json(os.path.join(ASSETS, 'models', 'block', 'nexus_console_planted.json'),
           {"textures": tex, "elements": console_base + planted_extra})
write_json(os.path.join(ASSETS, 'models', 'item', 'nexus_console.json'),
           {"parent": "astralpack:block/nexus_console_planted" if False else "astralpack:block/nexus_console"})

write_json(os.path.join(ASSETS, 'models', 'block', 'astral_ring_block.json'),
           {"parent": "minecraft:block/cube_all", "textures": {"all": "astralpack:block/astral_ring_block"}})
write_json(os.path.join(ASSETS, 'models', 'item', 'astral_ring_block.json'),
           {"parent": "astralpack:block/astral_ring_block"})

write_json(os.path.join(ASSETS, 'models', 'block', 'nexus_portal.json'), {
    "render_type": "minecraft:translucent",
    "textures": {"0": "astralpack:block/nexus_portal", "particle": "astralpack:block/nexus_portal"},
    "elements": [{"from": [0, 0, 6], "to": [16, 16, 10], "faces": {
        "north": {"uv": [0, 0, 16, 16], "texture": "#0"},
        "south": {"uv": [0, 0, 16, 16], "texture": "#0"}}}],
})

# ---- États de blocs ----
write_json(os.path.join(ASSETS, 'blockstates', 'astral_ring_block.json'),
           {"variants": {"": {"model": "astralpack:block/astral_ring_block"}}})
variants = {}
for facing, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    for planted, model in (("false", "nexus_console"), ("true", "nexus_console_planted")):
        v = {"model": f"astralpack:block/{model}"}
        if rot:
            v["y"] = rot
        variants[f"facing={facing},planted={planted}"] = v
write_json(os.path.join(ASSETS, 'blockstates', 'nexus_console.json'), {"variants": variants})
write_json(os.path.join(ASSETS, 'blockstates', 'nexus_portal.json'), {"variants": {
    "axis=x": {"model": "astralpack:block/nexus_portal"},
    "axis=z": {"model": "astralpack:block/nexus_portal", "y": 90}}})

# ---- Butin, tags, recettes ----
for name in ('astral_ring_block', 'nexus_console'):
    write_json(os.path.join(DATA, 'astralpack', 'loot_tables', 'blocks', f'{name}.json'), {
        "type": "minecraft:block",
        "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
                   "entries": [{"type": "minecraft:item", "name": f"astralpack:{name}"}],
                   "conditions": [{"condition": "minecraft:survives_explosion"}]}],
        "random_sequence": f"astralpack:blocks/{name}",
    })
values = ["astralpack:astral_ring_block", "astralpack:nexus_console"]


def merge_tag(rel):
    path = os.path.join(DATA, 'minecraft', 'tags', 'blocks', rel)
    existing = []
    if os.path.exists(path):
        with open(path, encoding='utf-8') as f:
            existing = json.load(f).get('values', [])
    merged = existing + [v for v in values if v not in existing]
    write_json(path, {"replace": False, "values": merged})


merge_tag('mineable/pickaxe.json')
merge_tag('needs_iron_tool.json')

write_json(os.path.join(DATA, 'astralpack', 'recipes', 'astral_ring_block.json'), {
    "type": "minecraft:crafting_shaped", "category": "building",
    "pattern": ["OAO", "AEA", "OAO"],
    "key": {"O": {"item": "minecraft:obsidian"}, "A": {"item": "minecraft:amethyst_shard"},
            "E": {"item": "minecraft:ender_pearl"}},
    "result": {"item": "astralpack:astral_ring_block", "count": 4},
})
write_json(os.path.join(DATA, 'astralpack', 'recipes', 'nexus_console.json'), {
    "type": "minecraft:crafting_shaped", "category": "misc",
    "pattern": ["RER", "DND", "DDD"],
    "key": {"R": {"item": "astralpack:astral_ring_block"}, "E": {"item": "minecraft:ender_eye"},
            "D": {"item": "minecraft:deepslate_tiles"}, "N": {"item": "minecraft:netherite_ingot"}},
    "result": {"item": "astralpack:nexus_console", "count": 1},
})

# La recette de la Death Scythe ne fonctionne qu'une fois par serveur.
scythe_path = os.path.join(DATA, 'astralpack', 'recipes', 'death_scythe.json')
with open(scythe_path, encoding='utf-8') as f:
    scythe = json.load(f)
scythe['type'] = 'astralpack:unique_shaped'
write_json(scythe_path, scythe)
print('ok')
