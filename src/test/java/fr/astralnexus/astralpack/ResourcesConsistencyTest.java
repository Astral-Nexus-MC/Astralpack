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
    void spawnBiomeModifiersAreDeclared() throws IOException {
        Path dir = Path.of("src/main/resources/data/astralpack/forge/biome_modifier");
        assertTrue(read(dir.resolve("fennec_spawns.json")).contains("#minecraft:is_badlands"));
        assertTrue(read(dir.resolve("fennec_spawns_desert.json")).contains("minecraft:desert"));
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
