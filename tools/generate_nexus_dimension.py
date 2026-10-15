"""Génère les fichiers de données de la dimension Astral Nexus (relief, biomes, types de dimension).

Usage : python tools/generate_nexus_dimension.py [xz_scale ampl offset haut_iles erosion_hub]
Valeurs retenues en 0.4.0 : 4.0 2.5 -1.0 70 -0.15. Nécessite le jar client-extra de Minecraft 1.20.1 (cache Gradle).
"""
import json, os, zipfile, sys

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
D = os.path.join(REPO, 'src', 'main', 'resources', 'data', 'astralpack')
JAR = os.path.expanduser(r'~\.gradle\caches\forge_gradle\minecraft_repo\versions\1.20.1\client-extra.jar')

# Paramètres réglables du relief
SAT_XZ_SCALE = float(sys.argv[1]) if len(sys.argv) > 1 else 4.0
SAT_AMPL = float(sys.argv[2]) if len(sys.argv) > 2 else 2.5
SAT_OFFSET = float(sys.argv[3]) if len(sys.argv) > 3 else -1.0
SAT_TOP = int(sys.argv[4]) if len(sys.argv) > 4 else 70
HUB_EROSION = float(sys.argv[5]) if len(sys.argv) > 5 else -0.15


def write(rel, obj):
    p = os.path.join(D, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


z = zipfile.ZipFile(JAR)
end = json.loads(z.read('data/minecraft/worldgen/noise_settings/end.json'))
over = json.loads(z.read('data/minecraft/worldgen/noise_settings/overworld.json'))
router = dict(end['noise_router'])
router['temperature'] = {"type": "minecraft:cache_2d", "argument": over['noise_router']['temperature']}
router['vegetation'] = {"type": "minecraft:cache_2d", "argument": over['noise_router']['vegetation']}

sat = {
    "type": "minecraft:interpolated",
    "argument": {
        "type": "minecraft:add",
        "argument1": {
            "type": "minecraft:add",
            "argument1": {
                "type": "minecraft:add",
                "argument1": SAT_OFFSET,
                "argument2": {
                    "type": "minecraft:mul",
                    "argument1": SAT_AMPL,
                    "argument2": {"type": "minecraft:noise", "noise": "minecraft:continentalness",
                                  "xz_scale": SAT_XZ_SCALE, "y_scale": 0.0},
                },
            },
            "argument2": {"type": "minecraft:y_clamped_gradient", "from_y": 30, "to_y": SAT_TOP,
                          "from_value": -1.5, "to_value": 0.0},
        },
        "argument2": {"type": "minecraft:y_clamped_gradient", "from_y": SAT_TOP, "to_y": SAT_TOP + 6,
                      "from_value": 0.0, "to_value": -4.0},
    },
}
router['final_density'] = {"type": "minecraft:max", "argument1": end['noise_router']['final_density'], "argument2": sat}


def floor_rule(top, below):
    return {"type": "minecraft:sequence", "sequence": [
        {"type": "minecraft:condition",
         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                     "add_surface_depth": False, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": top}}},
        {"type": "minecraft:condition",
         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                     "add_surface_depth": True, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": below}}},
    ]}


def biome_rule(biome, top, below, patch=None):
    run = floor_rule(top, below)
    if patch:
        run = {"type": "minecraft:sequence", "sequence": [
            {"type": "minecraft:condition",
             "if_true": {"type": "minecraft:noise_threshold", "noise": "minecraft:surface",
                         "min_threshold": patch[1], "max_threshold": 10.0},
             "then_run": floor_rule(patch[0], below)},
            run]}
    return {"type": "minecraft:condition",
            "if_true": {"type": "minecraft:biome", "biome_is": [biome]},
            "then_run": run}


noise_settings = {
    "sea_level": 0,
    "disable_mob_generation": True,
    "aquifers_enabled": False,
    "ore_veins_enabled": False,
    "legacy_random_source": True,
    "default_block": {"Name": "astralpack:abyssal_stone"},
    "default_fluid": {"Name": "minecraft:air"},
    "noise": {"min_y": 0, "height": 128, "size_horizontal": 2, "size_vertical": 1},
    "noise_router": router,
    "spawn_target": [],
    "surface_rule": {"type": "minecraft:sequence", "sequence": [
        biome_rule("astralpack:nexus_hub", "astralpack:astral_regolith", "astralpack:stellar_stone", ("astralpack:lumen_moss", 0.3)),
        biome_rule("astralpack:astral_isles", "astralpack:astral_regolith", "astralpack:stellar_stone", ("astralpack:lumen_moss", 0.45)),
        biome_rule("astralpack:crystal_reef", "astralpack:reef_crystal", "astralpack:abyssal_stone"),
    ]},
}
write('worldgen/noise_settings/nexus.json', noise_settings)


def rgb(h):
    return int(h, 16)


def biome(sky, fog, water, water_fog, particle, particle_p, spawners=None, temperature=0.5, ores=()):
    effects = {
        "sky_color": rgb(sky), "fog_color": rgb(fog),
        "water_color": rgb(water), "water_fog_color": rgb(water_fog),
        "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000,
                       "block_search_extent": 8, "offset": 2.0},
    }
    if particle:
        effects["particle"] = {"probability": particle_p, "options": {"type": particle}}
    return {
        "carvers": {}, "downfall": 0.0, "has_precipitation": False, "temperature": temperature,
        "effects": effects, "features": [[]] * 6 + [list(ores)] + [[]] * 4, "spawn_costs": {},
        "spawners": spawners or {"monster": [], "creature": [], "ambient": [], "axolotls": [], "misc": [],
                                  "underground_water_creature": [], "water_ambient": [], "water_creature": []},
    }


def with_monsters(mobs):
    s = {"monster": mobs, "creature": [], "ambient": [], "axolotls": [], "misc": [],
         "underground_water_creature": [], "water_ambient": [], "water_creature": []}
    return s


write('worldgen/biome/nexus_hub.json', biome('1a1448', '2a1c68', '5b3fd6', '1a1448', 'minecraft:end_rod', 0.004))
write('worldgen/biome/astral_isles.json', biome('160f3a', '241a5c', '3f5fd6', '140f3a', 'minecraft:end_rod', 0.003,
      with_monsters([{"type": "minecraft:enderman", "weight": 10, "minCount": 1, "maxCount": 2}]),
      ores=("astralpack:nebula_ore", "astralpack:astralite_ore")))
write('worldgen/biome/crystal_reef.json', biome('1c1050', '33206e', '7a4be0', '1c1050', 'minecraft:reverse_portal', 0.006,
      with_monsters([{"type": "minecraft:enderman", "weight": 6, "minCount": 1, "maxCount": 1}]),
      ores=("astralpack:astralite_ore",)))

FULL = [-2.0, 2.0]


def entry(b, **kw):
    p = {"temperature": FULL, "humidity": FULL, "continentalness": FULL, "erosion": FULL, "weirdness": FULL,
         "depth": FULL, "offset": 0.0}
    p.update(kw)
    return {"biome": b, "parameters": p}


biome_source = {"type": "minecraft:multi_noise", "biomes": [
    entry("astralpack:nexus_hub", erosion=[HUB_EROSION, 2.0]),
    entry("astralpack:astral_isles", erosion=[-2.0, HUB_EROSION], temperature=[-2.0, 0.25]),
    entry("astralpack:crystal_reef", erosion=[-2.0, HUB_EROSION], temperature=[0.25, 2.0]),
]}


def dim_type(effects):
    return {
        "ambient_light": 0.3, "bed_works": True, "coordinate_scale": 1.0, "effects": effects,
        "fixed_time": 18000, "has_ceiling": False, "has_raids": False, "has_skylight": True,
        "height": 256, "infiniburn": "#minecraft:infiniburn_overworld", "logical_height": 256, "min_y": 0,
        "monster_spawn_block_light_limit": 0,
        "monster_spawn_light_level": {"type": "minecraft:uniform", "value": {"min_inclusive": 0, "max_inclusive": 7}},
        "natural": False, "piglin_safe": False, "respawn_anchor_works": False, "ultrawarm": False,
    }


write('dimension_type/nexus.json', dim_type('minecraft:overworld'))
write('dimension/nexus.json', {
    "type": "astralpack:nexus",
    "generator": {"type": "minecraft:noise", "settings": "astralpack:nexus", "biome_source": biome_source},
})

# Attention : ne jamais supprimer une dimension déjà publiée sans avoir nettoyé les mondes (voir tools/retirer_nexus_b.py).
print('ok', SAT_XZ_SCALE, SAT_AMPL, SAT_OFFSET, SAT_TOP, HUB_EROSION)
