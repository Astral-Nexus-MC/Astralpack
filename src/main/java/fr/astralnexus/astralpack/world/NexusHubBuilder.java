package fr.astralnexus.astralpack.world;

import com.mojang.logging.LogUtils;
import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Construction du hub (plan validé par le responsable) : place du Nexus au centre, quatre avenues, route circulaire,
 * jusqu'à vingt-quatre parcelles de base libres (huit sur un anneau intérieur, seize sur l'extérieur, celles dont le sol est trop incomplet sont ignorées), marché au nord et deux embarcadères. Elle se fait en étapes (une par tick, tant qu'un
 * joueur est dans le Nexus) sur le terrain déjà généré, et ne touche qu'aux blocs naturels du Nexus : rien de ce que
 * les joueurs ont construit n'est remplacé. La progression est enregistrée dans {@link NexusData}.
 */
public final class NexusHubBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Génération du terrain (celle de {@code LegacyWorldMigration}) et version du plan : un changement refait la construction. */
    public static final int HUB_KEY = 3 * 100 + 1;

    private static final int PLAZA_RADIUS = 20;
    private static final int AVENUE_START = 21;
    private static final int RING_ROAD_RADIUS = 64;
    private static final int PARCEL_RING = 84;
    private static final int PARCEL_INNER_RING = 46;
    private static final int OUTER_PARCELS = 16;
    private static final int INNER_PARCELS = 8;
    private static final int PARCEL_SIZE = 22;
    private static final int PARCELS = OUTER_PARCELS + INNER_PARCELS;
    private static final int STAGE_PLAZA = 0;
    private static final int STAGE_AVENUE_FIRST = 1;
    private static final int STAGE_RING_ROAD = 5;
    private static final int STAGE_PARCEL_FIRST = 6;
    private static final int STAGE_MARKET = STAGE_PARCEL_FIRST + PARCELS;
    private static final int STAGE_JETTY_EAST = STAGE_MARKET + 1;
    private static final int STAGE_JETTY_WEST = STAGE_MARKET + 2;
    private static final int STAGES = STAGE_MARKET + 3;

    /** Directions des avenues : est, ouest, nord, sud (le nord est vers les z négatifs). */
    private static final int[][] AVENUES = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}};

    /** Une étape toutes les 20 ticks (une seconde) : chaque étape peut générer des chunks, on évite les à-coups. */
    private static final int STAGE_INTERVAL = 20;
    private static int countdown;

    private NexusHubBuilder() {
    }

    /** À appeler à chaque tick du serveur : fait avancer la construction d'une étape si un joueur est dans le Nexus. */
    public static void tick(ServerLevel level) {
        if (level.players().isEmpty() || ++countdown < STAGE_INTERVAL) {
            return;
        }
        countdown = 0;
        NexusData data = NexusData.get(level.getServer());
        data.syncHubKey(HUB_KEY);
        if (data.getHubStage() >= STAGES) {
            return;
        }
        runStage(level, data);
    }

    /** Commande d'administration : termine toutes les étapes restantes tout de suite. Renvoie le nombre d'étapes faites. */
    public static int runAll(ServerLevel level, boolean restart) {
        NexusData data = NexusData.get(level.getServer());
        data.syncHubKey(HUB_KEY);
        if (restart) {
            data.setHubStage(0);
        }
        int done = 0;
        while (data.getHubStage() < STAGES) {
            runStage(level, data);
            done++;
        }
        return done;
    }

    /** La place du centre doit exister avant d'y faire arriver un joueur. */
    public static void ensurePlaza(ServerLevel level) {
        NexusData data = NexusData.get(level.getServer());
        data.syncHubKey(HUB_KEY);
        if (data.getHubStage() == STAGE_PLAZA) {
            runStage(level, data);
        }
    }

    private static void runStage(ServerLevel level, NexusData data) {
        int stage = data.getHubStage();
        try {
            if (stage == STAGE_PLAZA) {
                plaza(level, data);
            } else if (stage < STAGE_RING_ROAD) {
                avenue(level, AVENUES[stage - STAGE_AVENUE_FIRST]);
            } else if (stage == STAGE_RING_ROAD) {
                ringRoad(level);
            } else if (stage < STAGE_MARKET) {
                parcel(level, stage - STAGE_PARCEL_FIRST);
            } else if (stage == STAGE_MARKET) {
                market(level);
            } else if (stage == STAGE_JETTY_EAST) {
                jetty(level, 1);
            } else {
                jetty(level, -1);
            }
        } catch (RuntimeException e) {
            LOGGER.error("[Nexus] Étape {} de la construction du hub impossible, on passe à la suivante", stage, e);
        }
        data.setHubStage(stage + 1);
        if (stage + 1 >= STAGES) {
            LOGGER.info("[Nexus] Construction du hub terminée.");
        }
    }

    // ---- Outils ----

    private static BlockState stellar() {
        return ModBlocks.STELLAR_STONE.get().defaultBlockState();
    }

    private static BlockState abyssal() {
        return ModBlocks.ABYSSAL_STONE.get().defaultBlockState();
    }

    private static BlockState regolith() {
        return ModBlocks.ASTRAL_REGOLITH.get().defaultBlockState();
    }

    private static BlockState crystal() {
        return ModBlocks.REEF_CRYSTAL.get().defaultBlockState();
    }

    private static BlockState beacon() {
        return ModBlocks.ASTRALITE_BLOCK.get().defaultBlockState();
    }

    /** Blocs naturels ou de décor du Nexus : les seuls que la construction a le droit de remplacer. */
    private static boolean replaceable(BlockState state) {
        return state.isAir() || state.is(ModBlocks.ASTRAL_REGOLITH.get()) || state.is(ModBlocks.STELLAR_STONE.get())
                || state.is(ModBlocks.ABYSSAL_STONE.get()) || state.is(ModBlocks.LUMEN_MOSS.get())
                || state.is(ModBlocks.REEF_CRYSTAL.get());
    }

    private static boolean natural(BlockState state) {
        return !state.isAir() && replaceable(state);
    }

    /** Hauteur du dernier bloc plein du relief d'origine (sans le décor), ou une valeur trop basse s'il n'y en a pas. */
    private static int terrainTop(ServerLevel level, int x, int z) {
        return level.getChunkSource().getGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level,
                level.getChunkSource().randomState()) - 1;
    }

    private static boolean hasTerrain(ServerLevel level, int x, int z) {
        return terrainTop(level, x, z) > level.getMinBuildHeight() + 2;
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }

    /** Enlève le décor naturel au-dessus d'un point, sans toucher à autre chose. */
    private static void clearAbove(ServerLevel level, int x, int fromY, int toY, int z) {
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = fromY; y <= toY; y++) {
            BlockState state = level.getBlockState(new BlockPos(x, y, z));
            if (natural(state)) {
                set(level, x, y, z, air);
            }
        }
    }

    /** Remplace la surface d'une colonne, en suivant le relief ; ne fait rien dans le vide ou sur une construction. */
    private static boolean pave(ServerLevel level, int x, int z, BlockState top) {
        int y = terrainTop(level, x, z);
        if (y <= level.getMinBuildHeight() + 2 || !natural(level.getBlockState(new BlockPos(x, y, z)))) {
            return false;
        }
        clearAbove(level, x, y + 1, y + 6, z);
        set(level, x, y, z, top);
        return true;
    }

    /** Aplanit une colonne à la hauteur donnée : creuse ou comble, puis pose le revêtement. */
    private static boolean flatten(ServerLevel level, int x, int z, int target, BlockState top) {
        int y = terrainTop(level, x, z);
        if (y <= level.getMinBuildHeight() + 2 || !natural(level.getBlockState(new BlockPos(x, y, z)))) {
            return false;
        }
        if (y > target) {
            clearAbove(level, x, target + 1, y + 14, z);
        } else {
            clearAbove(level, x, y + 1, target + 14, z);
            for (int fill = y + 1; fill < target; fill++) {
                BlockState state = level.getBlockState(new BlockPos(x, fill, z));
                if (state.isAir()) {
                    set(level, x, fill, z, stellar());
                }
            }
        }
        set(level, x, target, z, top);
        return true;
    }

    private static void pillar(ServerLevel level, int x, int z, int groundY, int height, BlockState cap) {
        for (int i = 1; i <= height; i++) {
            BlockPos pos = new BlockPos(x, groundY + i, z);
            if (!replaceable(level.getBlockState(pos))) {
                return;
            }
            set(level, x, groundY + i, z, i == height ? cap : abyssal());
        }
    }

    private static int groundAt(ServerLevel level, int x, int z) {
        return terrainTop(level, x, z);
    }

    /** Distance, en blocs, du centre au bord de l'île dans cette direction (dernier bloc de relief continu). */
    private static int edge(ServerLevel level, int dx, int dz) {
        int last = 0;
        int gap = 0;
        for (int t = AVENUE_START; t <= 150; t++) {
            if (hasTerrain(level, dx * t, dz * t)) {
                last = t;
                gap = 0;
            } else if (++gap > 6) {
                break;
            }
        }
        return last;
    }

    // ---- Étapes ----

    private static void plaza(ServerLevel level, NexusData data) {
        int center = terrainTop(level, 0, 0);
        if (center <= level.getMinBuildHeight() + 2) {
            return;
        }
        data.setHubY(center);
        int target = center;
        for (int x = -PLAZA_RADIUS; x <= PLAZA_RADIUS; x++) {
            for (int z = -PLAZA_RADIUS; z <= PLAZA_RADIUS; z++) {
                double r = Math.sqrt(x * x + z * z);
                if (r > PLAZA_RADIUS || insideReturnGate(x, z)) {
                    continue;
                }
                flatten(level, x, z, target, plazaBlock(x, z, r));
            }
        }
        for (int k = 0; k < 8; k++) {
            double angle = Math.toRadians(22.5D + 45.0D * k);
            int x = (int) Math.round(16.5D * Math.cos(angle));
            int z = (int) Math.round(16.5D * Math.sin(angle));
            if (!insideReturnGate(x, z)) {
                pillar(level, x, z, target, 5, beacon());
            }
        }
        for (int[] d : AVENUES) {
            for (int side = -1; side <= 1; side += 2) {
                int px = d[0] * (PLAZA_RADIUS + 1) + (d[0] == 0 ? side * 4 : 0);
                int pz = d[1] * (PLAZA_RADIUS + 1) + (d[1] == 0 ? side * 4 : 0);
                if (!insideReturnGate(px, pz)) {
                    pillar(level, px, pz, target, 6, beacon());
                }
            }
        }
    }

    /** Le portail de retour est à 14 blocs au sud de l'arrivée : la place ne le touche pas. */
    private static boolean insideReturnGate(int x, int z) {
        return Math.abs(x) <= 8 && z >= 11 && z <= 17;
    }

    private static BlockState plazaBlock(int x, int z, double r) {
        if (r < 2.5D) {
            return beacon();
        }
        if ((r >= 8.0D && r < 9.2D) || r >= 18.8D) {
            return crystal();
        }
        if (r >= 13.0D && r < 14.2D) {
            return abyssal();
        }
        double angle = Math.toDegrees(Math.atan2(z, x));
        double spoke = Math.abs(((angle % 45.0D) + 45.0D) % 45.0D);
        if ((spoke < 2.2D || spoke > 42.8D) && r > 3.0D) {
            return crystal();
        }
        return stellar();
    }

    private static void avenue(ServerLevel level, int[] d) {
        int end = edge(level, d[0], d[1]);
        for (int t = AVENUE_START; t <= end; t++) {
            for (int w = -2; w <= 2; w++) {
                int x = d[0] != 0 ? d[0] * t : w;
                int z = d[1] != 0 ? d[1] * t : w;
                pave(level, x, z, Math.abs(w) <= 1 ? stellar() : crystal());
            }
            if (t % 12 == 0) {
                for (int side = -1; side <= 1; side += 2) {
                    int x = d[0] != 0 ? d[0] * t : side * 3;
                    int z = d[1] != 0 ? d[1] * t : side * 3;
                    if (hasTerrain(level, x, z)) {
                        pillar(level, x, z, groundAt(level, x, z), 4, beacon());
                    }
                }
            }
        }
    }

    private static void ringRoad(ServerLevel level) {
        int bound = RING_ROAD_RADIUS + 3;
        for (int x = -bound; x <= bound; x++) {
            for (int z = -bound; z <= bound; z++) {
                double off = Math.abs(Math.sqrt(x * x + z * z) - RING_ROAD_RADIUS);
                if (off <= 1.5D) {
                    pave(level, x, z, off <= 0.6D ? stellar() : crystal());
                }
            }
        }
    }

    private static void parcel(ServerLevel level, int index) {
        boolean inner = index >= OUTER_PARCELS;
        double angle = inner ? Math.toRadians(22.5D + 45.0D * (index - OUTER_PARCELS))
                : Math.toRadians(11.25D + 22.5D * index);
        int ring = inner ? PARCEL_INNER_RING : PARCEL_RING;
        int cx = (int) Math.round(ring * Math.cos(angle));
        int cz = (int) Math.round(ring * Math.sin(angle));
        int half = PARCEL_SIZE / 2;
        List<Integer> heights = new ArrayList<>();
        for (int x = cx - half; x < cx + half; x++) {
            for (int z = cz - half; z < cz + half; z++) {
                if (hasTerrain(level, x, z)) {
                    heights.add(terrainTop(level, x, z));
                }
            }
        }
        if (heights.size() < PARCEL_SIZE * PARCEL_SIZE * 85 / 100) {
            return;
        }
        Collections.sort(heights);
        int target = heights.get(heights.size() / 2);
        if (heights.get(heights.size() - 1) - heights.get(0) > 10) {
            return;
        }
        for (int x = cx - half; x < cx + half; x++) {
            for (int z = cz - half; z < cz + half; z++) {
                boolean border = x == cx - half || x == cx + half - 1 || z == cz - half || z == cz + half - 1;
                flatten(level, x, z, target, border ? crystal() : regolith());
            }
        }
        int[][] corners = {{cx - half, cz - half}, {cx + half - 1, cz - half}, {cx - half, cz + half - 1},
                {cx + half - 1, cz + half - 1}};
        for (int[] c : corners) {
            pillar(level, c[0], c[1], target, 5, beacon());
        }
        int markX = cx;
        int markZ = cz;
        if (Math.abs(cx) >= Math.abs(cz)) {
            markX = cx + (cx > 0 ? -half : half - 1);
        } else {
            markZ = cz + (cz > 0 ? -half : half - 1);
        }
        set(level, markX, target, markZ, ModBlocks.ASTRAL_RING_BLOCK.get().defaultBlockState());
    }

    private static void market(ServerLevel level) {
        int north = edge(level, 0, -1);
        if (north < 40) {
            return;
        }
        int distance = Math.min(86, north - 14);
        int cz = -distance;
        List<Integer> heights = new ArrayList<>();
        for (int x = -17; x <= 16; x++) {
            for (int z = cz - 8; z <= cz + 7; z++) {
                if (hasTerrain(level, x, z)) {
                    heights.add(terrainTop(level, x, z));
                }
            }
        }
        if (heights.size() < 34 * 16 * 80 / 100) {
            return;
        }
        Collections.sort(heights);
        int target = heights.get(heights.size() / 2);
        for (int x = -17; x <= 16; x++) {
            for (int z = cz - 8; z <= cz + 7; z++) {
                boolean border = x == -17 || x == 16 || z == cz - 8 || z == cz + 7;
                flatten(level, x, z, target, border ? crystal() : stellar());
            }
        }
        for (int row = 0; row < 2; row++) {
            int rowZ = row == 0 ? cz - 5 : cz + 4;
            for (int s = -1; s <= 1; s++) {
                int sx = s * 9 - 1;
                for (int dx = 0; dx < 3; dx++) {
                    set(level, sx + dx, target + 1, rowZ, stellar());
                }
                for (int[] post : new int[][]{{sx - 1, rowZ - 1}, {sx + 3, rowZ - 1}, {sx - 1, rowZ + 1}, {sx + 3, rowZ + 1}}) {
                    for (int h = 1; h <= 3; h++) {
                        set(level, post[0], target + h, post[1], abyssal());
                    }
                }
                for (int dx = -1; dx <= 3; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        set(level, sx + dx, target + 4, rowZ + dz, abyssal());
                    }
                }
                set(level, sx + 1, target + 5, rowZ, crystal());
            }
        }
    }

    private static void jetty(ServerLevel level, int side) {
        int edge = edge(level, side, 0);
        if (edge < 40) {
            return;
        }
        int end = edge - 1;
        int cx = side * (end - 4);
        int y = terrainTop(level, cx, 0);
        if (y <= level.getMinBuildHeight() + 2) {
            return;
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean border = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                flatten(level, cx + dx, dz, y, border ? crystal() : stellar());
            }
        }
        for (int t = 1; t <= 14; t++) {
            int x = side * (end + t);
            for (int w = -1; w <= 1; w++) {
                set(level, x, y, w, Math.abs(w) == 1 ? crystal() : stellar());
            }
            if (t % 4 == 0) {
                pillar(level, x, -2, y, 3, beacon());
                pillar(level, x, 2, y, 3, beacon());
            }
        }
        int tip = side * (end + 15);
        for (int w = -1; w <= 1; w++) {
            set(level, tip, y, w, abyssal());
        }
        pillar(level, tip, 0, y, 6, beacon());
    }
}
