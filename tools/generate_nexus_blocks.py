"""Génère les ressources « logiques » des blocs du portail du Nexus : états de blocs, modèles d'objets, recettes, butin, tags.

Les modèles 3D et les textures des blocs ne sont PAS générés ici : ils sont créés et polis dans Blockbench
(blockbench/block/astral_ring_block.bbmodel, nexus_console.bbmodel, nexus_portal.bbmodel) puis exportés dans
src/main/resources/assets/astralpack/models/block/ et textures/block/. Ce script ne doit jamais les écraser.

Usage : python tools/generate_nexus_blocks.py
"""
import json
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'astralpack')
DATA = os.path.join(REPO, 'src', 'main', 'resources', 'data')


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


# ---- Modèles d'objets (les modèles de blocs viennent de Blockbench) ----
write_json(os.path.join(ASSETS, 'models', 'item', 'nexus_console.json'), {"parent": "astralpack:block/nexus_console"})
write_json(os.path.join(ASSETS, 'models', 'item', 'astral_ring_block.json'),
           {"parent": "astralpack:block/astral_ring_block"})

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
