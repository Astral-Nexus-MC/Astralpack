package fr.astralnexus.astralpack;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Vérifie que le code Java et les exports Blockbench (GeckoLib) restent cohérents. */
class ResourcesConsistencyTest {
    private static final Path RES = Path.of("src/main/resources/assets/astralpack");

    private static String read(Path p) throws IOException {
        return Files.readString(p, StandardCharsets.UTF_8);
    }

    @Test
    void animationsUsedByEntityExist() throws IOException {
        String anims = read(RES.resolve("animations/fennec.animation.json"));
        for (String n : List.of("idle", "walk", "sit", "shake", "attack", "belly_scratch", "bite", "sleep", "dig")) {
            assertTrue(anims.contains("\"animation.fennec." + n + "\""), "animation manquante : " + n);
        }
    }

    @Test
    void geoDeclaresRequiredBones() throws IOException {
        String geo = read(RES.resolve("geo/fennec.geo.json"));
        assertTrue(geo.contains("\"geometry.fennec\""));
        for (String b : List.of("body", "head", "leg_front_left", "leg_front_right", "leg_back_left",
                "leg_back_right", "tail", "jaw", "mouth_item", "fur_torso", "fur_head", "fur_tail", "fur_leg_front_left", "armor_helmet_iron", "armor_chest_netherite")) {
            assertTrue(geo.contains("\"name\": \"" + b + "\""), "bone manquant : " + b);
        }
    }

    @Test
    void soundsAndTexturesArePresent() throws IOException {
        String sounds = read(RES.resolve("sounds.json"));
        for (String s : List.of("ambient1", "ambient2", "ambient3", "hurt1", "hurt2", "death")) {
            assertTrue(Files.exists(RES.resolve("sounds/mob/fennec/" + s + ".ogg")), "son manquant : " + s);
        }
        assertTrue(sounds.contains("mob/fennec"));
        assertTrue(Files.exists(RES.resolve("textures/entity/fennec.png")));
    }

    @Test
    void deathScytheResourcesArePresent() throws IOException {
        assertTrue(read(RES.resolve("geo/death_scythe.geo.json")).contains("\"geometry.death_scythe\""));
        assertTrue(Files.exists(RES.resolve("textures/item/death_scythe.png")));
        assertTrue(Files.exists(RES.resolve("textures/item/death_scythe_glowmask.png")));
        assertTrue(Files.exists(RES.resolve("animations/death_scythe.animation.json")));
        assertTrue(read(RES.resolve("models/item/death_scythe.json")).contains("builtin/entity"));
        assertTrue(Files.exists(Path.of("src/main/resources/data/astralpack/recipes/death_scythe.json")));
        for (String lang : List.of("en_us", "fr_fr")) {
            String json = read(RES.resolve("lang/" + lang + ".json"));
            for (String key : List.of("item.astralpack.death_scythe", "item.astralpack.death_scythe.two_handed",
                    "item.astralpack.death_scythe.wither")) {
                assertTrue(json.contains("\"" + key + "\""), "clé manquante (" + lang + ") : " + key);
            }
        }
    }

    @Test
    void nexusDimensionFilesArePresent() throws IOException {
        Path data = Path.of("src/main/resources/data/astralpack");
        for (String f : List.of("dimension/nexus.json", "dimension_type/nexus.json", "worldgen/noise_settings/nexus.json",
                "worldgen/biome/nexus_hub.json", "worldgen/biome/astral_isles.json",
                "worldgen/biome/crystal_reef.json")) {
            assertTrue(Files.exists(data.resolve(f)), "fichier manquant : " + f);
        }
        String dimension = read(data.resolve("dimension/nexus.json"));
        for (String biome : List.of("nexus_hub", "astral_isles", "crystal_reef")) {
            assertTrue(dimension.contains("astralpack:" + biome), "biome absent de la dimension : " + biome);
        }
        for (String lang : List.of("en_us", "fr_fr")) {
            String json = read(RES.resolve("lang/" + lang + ".json"));
            for (String key : List.of("commands.astralpack.nexus.entered", "commands.astralpack.nexus.left",
                    "commands.astralpack.nexus.missing", "message.astralpack.nexus.rescued")) {
                assertTrue(json.contains("\"" + key + "\""), "clé manquante (" + lang + ") : " + key);
            }
        }
    }

    @Test
    void nexusPortalResourcesArePresent() throws IOException {
        for (String f : List.of("blockstates/astral_ring_block.json", "blockstates/nexus_console.json",
                "blockstates/nexus_portal.json", "models/block/astral_ring_block.json",
                "models/block/nexus_console.json", "models/block/nexus_console_planted.json",
                "models/block/nexus_portal.json", "models/item/astral_ring_block.json",
                "models/item/nexus_console.json", "textures/block/astral_ring_block.png",
                "textures/block/astral_ring_rune.png", "textures/block/nexus_console_glow.png",
                "textures/block/nexus_console_scythe.png", "textures/block/nexus_portal.png",
                "textures/block/nexus_portal.png.mcmeta")) {
            assertTrue(Files.exists(RES.resolve(f)), "ressource manquante : " + f);
        }
        Path data = Path.of("src/main/resources/data");
        for (String f : List.of("astralpack/recipes/astral_ring_block.json", "astralpack/recipes/nexus_console.json",
                "astralpack/loot_tables/blocks/astral_ring_block.json",
                "astralpack/loot_tables/blocks/nexus_console.json")) {
            assertTrue(Files.exists(data.resolve(f)), "fichier manquant : " + f);
        }
        assertTrue(read(data.resolve("minecraft/tags/blocks/mineable/pickaxe.json")).contains("astralpack:nexus_console"));
        // La Death Scythe ne se fabrique qu'une fois par serveur : sa recette passe par le type dédié.
        assertTrue(read(data.resolve("astralpack/recipes/death_scythe.json")).contains("astralpack:unique_shaped"));
        for (String lang : List.of("en_us", "fr_fr")) {
            String json = read(RES.resolve("lang/" + lang + ".json"));
            for (String key : List.of("block.astralpack.astral_ring_block", "block.astralpack.nexus_console",
                    "block.astralpack.nexus_portal", "message.astralpack.nexus.console.no_ring",
                    "message.astralpack.nexus.console.opened", "commands.astralpack.nexus.scythe.crafted")) {
                assertTrue(json.contains("\"" + key + "\""), "clé manquante (" + lang + ") : " + key);
            }
        }
    }

    @Test
    void spawnBiomeModifiersAreDeclared() throws IOException {
        Path dir = Path.of("src/main/resources/data/astralpack/forge/biome_modifier");
        assertTrue(read(dir.resolve("fennec_spawns.json")).contains("#astralpack:has_fennec"));
        assertTrue(read(dir.resolve("fennec_spawns_beach.json")).contains("#astralpack:has_fennec_beach"));
        Path tags = Path.of("src/main/resources/data/astralpack/tags/worldgen/biome");
        String main = read(tags.resolve("has_fennec.json"));
        assertTrue(main.contains("#minecraft:is_badlands") && main.contains("minecraft:desert"));
        assertTrue(main.contains("biomesoplenty:dryland") && main.contains("biomeswevegone:mojave_desert"));
        assertTrue(read(tags.resolve("has_fennec_beach.json")).contains("minecraft:beach"));
    }

    @Test
    void burrowBlockAssetsArePresent() throws IOException {
        for (String f : List.of("blockstates/fennec_burrow.json", "models/block/fennec_burrow.json",
                "models/item/fennec_burrow.json", "textures/block/fennec_burrow.png",
                "textures/block/fennec_burrow_floor.png")) {
            assertTrue(Files.exists(RES.resolve(f)), "ressource manquante : " + f);
        }
        Path data = Path.of("src/main/resources/data");
        assertTrue(Files.exists(data.resolve("astralpack/recipes/fennec_burrow.json")));
        assertTrue(Files.exists(data.resolve("astralpack/loot_tables/blocks/fennec_burrow.json")));
        for (String lang : List.of("en_us", "fr_fr")) {
            assertTrue(read(RES.resolve("lang/" + lang + ".json")).contains("\"block.astralpack.fennec_burrow\""),
                    "nom du terrier manquant (" + lang + ")");
        }
    }
}
