"""Génère les fichiers de données des blocs et ressources du Nexus (états, modèles simples, butins, étiquettes, recettes, textes, minerais).

Usage : python tools/generate_nexus_ressources.py
Les textures (PNG) ne sont PAS générées ici : elles sont faites dans Blockbench (blockbench/block/nexus_ressources.bbmodel).
"""
import json, os

ASTRALITE_COUNT = 1
NEBULA_COUNT = 4
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(REPO, 'src', 'main', 'resources')
ASSETS = os.path.join(RES, 'assets', 'astralpack')
DATA = os.path.join(RES, 'data', 'astralpack')
MC = os.path.join(RES, 'data', 'minecraft')


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


# bloc : (français, anglais, outil, besoin d'un outil de fer)
BLOCKS = {
    'astral_regolith': ('Régolithe astral', 'Astral regolith', 'shovel', False),
    'stellar_stone': ('Pierre stellaire', 'Stellar stone', 'pickaxe', False),
    'reef_crystal': ('Cristal de récif', 'Reef crystal', 'pickaxe', False),
    'astralite_ore': ("Minerai d'astralite", 'Astralite ore', 'pickaxe', True),
    'nebula_ore': ('Minerai de nébuleuse', 'Nebula ore', 'pickaxe', True),
    'astralite_block': ("Bloc d'astralite", 'Block of astralite', 'pickaxe', True),
    'abyssal_stone': ('Pierre abyssale', 'Abyssal stone', 'pickaxe', False),
    'lumen_moss': ('Mousse lumineuse', 'Lumen moss', 'hoe', False),
}
ITEMS = {
    'astralite': ('Astralite', 'Astralite'),
    'nebula_shard': ('Éclat de nébuleuse', 'Nebula shard'),
}

for name in BLOCKS:
    write(os.path.join(ASSETS, 'blockstates', name + '.json'), {"variants": {"": {"model": "astralpack:block/" + name}}})
    write(os.path.join(ASSETS, 'models', 'block', name + '.json'),
          {"parent": "minecraft:block/cube_all", "textures": {"all": "astralpack:block/" + name}})
    write(os.path.join(ASSETS, 'models', 'item', name + '.json'), {"parent": "astralpack:block/" + name})
for name in ITEMS:
    write(os.path.join(ASSETS, 'models', 'item', name + '.json'),
          {"parent": "minecraft:item/generated", "textures": {"layer0": "astralpack:item/" + name}})


def self_drop(name):
    return {"type": "minecraft:block", "pools": [{
        "rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:item", "name": "astralpack:" + name}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}],
        "random_sequence": "astralpack:blocks/" + name}


def ore_drop(name, item, low, high):
    return {"type": "minecraft:block", "pools": [{
        "rolls": 1.0, "bonus_rolls": 0.0,
        "entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": "astralpack:" + name,
             "conditions": [{"condition": "minecraft:match_tool", "predicate": {"enchantments": [
                 {"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}]},
            {"type": "minecraft:item", "name": "astralpack:" + item, "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": low, "max": high}},
                {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                 "formula": "minecraft:ore_drops"},
                {"function": "minecraft:explosion_decay"}]}]}]}],
        "random_sequence": "astralpack:blocks/" + name}


for name in BLOCKS:
    if name == 'astralite_ore':
        loot = ore_drop(name, 'astralite', 1.0, 2.0)
    elif name == 'nebula_ore':
        loot = ore_drop(name, 'nebula_shard', 2.0, 4.0)
    else:
        loot = self_drop(name)
    write(os.path.join(DATA, 'loot_tables', 'blocks', name + '.json'), loot)


def tag(path, new_values):
    existing = []
    if os.path.exists(path):
        with open(path, encoding='utf-8') as f:
            existing = json.load(f).get('values', [])
    values = existing + [v for v in new_values if v not in existing]
    write(path, {"replace": False, "values": values})


tag(os.path.join(MC, 'tags', 'blocks', 'mineable', 'pickaxe.json'),
    ['astralpack:' + n for n, b in BLOCKS.items() if b[2] == 'pickaxe'])
tag(os.path.join(MC, 'tags', 'blocks', 'mineable', 'hoe.json'),
    ['astralpack:' + n for n, b in BLOCKS.items() if b[2] == 'hoe'])
tag(os.path.join(MC, 'tags', 'blocks', 'mineable', 'shovel.json'),
    ['astralpack:' + n for n, b in BLOCKS.items() if b[2] == 'shovel'])
tag(os.path.join(MC, 'tags', 'blocks', 'needs_iron_tool.json'),
    ['astralpack:' + n for n, b in BLOCKS.items() if b[3]])


def shapeless_block_recipes():
    write(os.path.join(DATA, 'recipes', 'astralite_block.json'), {
        "type": "minecraft:crafting_shaped", "category": "building",
        "pattern": ["AAA", "AAA", "AAA"], "key": {"A": {"item": "astralpack:astralite"}},
        "result": {"item": "astralpack:astralite_block", "count": 1}})
    write(os.path.join(DATA, 'recipes', 'astralite_from_block.json'), {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [{"item": "astralpack:astralite_block"}],
        "result": {"item": "astralpack:astralite", "count": 9}})


shapeless_block_recipes()


def lang(code, index):
    path = os.path.join(ASSETS, 'lang', code + '.json')
    with open(path, encoding='utf-8') as f:
        data = json.load(f)
    for n, v in BLOCKS.items():
        data['block.astralpack.' + n] = v[index]
    for n, v in ITEMS.items():
        data['item.astralpack.' + n] = v[index]
    write(path, data)


lang('fr_fr', 0)
lang('en_us', 1)

# ---- Minerais : uniquement sur les îles satellites et au récif, jamais sur l'île centrale ----
TARGETS = [{"target": {"predicate_type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"}}]


def ore_feature(name, block, size, count, discard):
    targets = [{"target": {"predicate_type": "minecraft:block_match", "block": "astralpack:" + rock},
                "state": {"Name": "astralpack:" + block}} for rock in ('abyssal_stone', 'stellar_stone')]
    write(os.path.join(DATA, 'worldgen', 'configured_feature', name + '.json'), {
        "type": "minecraft:ore",
        "config": {"discard_chance_on_air_exposure": discard, "size": size, "targets": targets}})
    write(os.path.join(DATA, 'worldgen', 'placed_feature', name + '.json'), {
        "feature": "astralpack:" + name,
        "placement": [
            {"type": "minecraft:count", "count": count},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {
                "type": "minecraft:uniform", "min_inclusive": {"absolute": 0}, "max_inclusive": {"absolute": 100}}},
            {"type": "minecraft:biome"}]})


# Rareté visée : astralite comme le diamant, nébuleuse comme la redstone (réglée sur la roche réellement générée).
ore_feature('astralite_ore', 'astralite_ore', 6, ASTRALITE_COUNT, 0.5)
ore_feature('nebula_ore', 'nebula_ore', 8, NEBULA_COUNT, 0.0)
