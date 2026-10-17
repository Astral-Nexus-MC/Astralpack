package fr.astralnexus.astralpack.world;

import com.mojang.logging.LogUtils;
import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
 * jusqu'à vingt-quatre parcelles de base libres (huit sur un anneau intérieur, seize sur l'extérieur, celles dont le sol est trop incomplet sont ignorées), place du marché au nord et deux embarcadères. Elle se fait en étapes (une par tick, tant qu'un
 * joueur est dans le Nexus) sur le terrain déjà généré, et ne touche qu'aux blocs naturels du Nexus : rien de ce que
 * les joueurs ont construit n'est remplacé. La progression est enregistrée dans {@link NexusData}.
 */
public final class NexusHubBuilder {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Génération du terrain (celle de {@code LegacyWorldMigration}) et version du plan : un changement refait la construction. */
    public static final int HUB_KEY = 4 * 100 + 1;

    private static final int PLAZA_RADIUS = 20;
    private static final int AVENUE_START = 21;
    private static final int RING_ROAD_RADIUS = 72;
    private static final int PARCEL_SIZE = 18;
    /** Nombre de maisons utilisables réservées aux joueurs. */
    private static final int HOMES = 10;
    private static final int STAGE_PLAZA = 0;
    private static final int STAGE_AVENUE_FIRST = 1;
    private static final int STAGE_RING_ROAD = 5;
    private static final int STAGE_MARKET = 6;
    private static final int STAGE_JETTY_FIRST = 7;
    private static final int JETTIES = 2;
    private static final int STAGE_PARCEL_FIRST = STAGE_JETTY_FIRST + JETTIES;

    /** Emplacements de parcelles : centre et anneau (0 intérieur, 1 milieu, 2 extérieur). */
    private static final List<int[]> PARCEL_SLOTS = parcelSlots();
    private static final int STAGES = STAGE_PARCEL_FIRST + PARCEL_SLOTS.size();

    /** Directions des avenues : est, ouest, nord, sud (le nord est vers les z négatifs). */
    private static final int[][] AVENUES = {{1, 0}, {-1, 0}, {0, -1}, {0, 1}};

    /** Bâtiments du village, dans l'ordre où on les attribue aux parcelles ; la fin du tableau se répète. */
    private static final NexusVillage.Kind[] VILLAGE = {
            NexusVillage.Kind.INN, NexusVillage.Kind.FORGE, NexusVillage.Kind.LIBRARY, NexusVillage.Kind.WAREHOUSE,
            NexusVillage.Kind.CHAPEL, NexusVillage.Kind.ALCHEMIST, NexusVillage.Kind.TOWER, NexusVillage.Kind.WELL,
            NexusVillage.Kind.GREENHOUSE, NexusVillage.Kind.FARM, NexusVillage.Kind.GARDEN, NexusVillage.Kind.FARM};
    private static final int VILLAGE_REPEAT_FROM = 8;

    private static List<int[]> parcelSlots() {
        List<int[]> slots = new ArrayList<>();
        double[][] rings = {{38, 8, 22.5D, 45.0D}, {56, 12, 15.0D, 30.0D}, {92, 16, 11.25D, 22.5D}};
        for (int ring = 0; ring < rings.length; ring++) {
            for (int k = 0; k < (int) rings[ring][1]; k++) {
                double angle = Math.toRadians(rings[ring][2] + rings[ring][3] * k);
                slots.add(new int[]{(int) Math.round(rings[ring][0] * Math.cos(angle)),
                        (int) Math.round(rings[ring][0] * Math.sin(angle)), ring});
            }
        }
        return slots;
    }

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

    /** Commande d'administration : termine toutes les étapes restantes tout de suite. Renvoie {étapes faites, étapes ignorées}. */
    public static int[] runAll(ServerLevel level, boolean restart) {
        NexusData data = NexusData.get(level.getServer());
        data.syncHubKey(HUB_KEY);
        if (restart) {
            data.setHubStage(0);
        }
        int done = 0;
        int skipped = 0;
        while (data.getHubStage() < STAGES) {
            if (!runStage(level, data)) {
                skipped++;
            }
            done++;
        }
        return new int[]{done, skipped};
    }

    /** La place du centre doit exister avant d'y faire arriver un joueur. */
    public static void ensurePlaza(ServerLevel level) {
        NexusData data = NexusData.get(level.getServer());
        data.syncHubKey(HUB_KEY);
        if (data.getHubStage() == STAGE_PLAZA) {
            runStage(level, data);
        }
    }

    /** Fait l'étape en cours ; renvoie faux si elle a été ignorée (terrain inadapté ou erreur). */
    private static boolean runStage(ServerLevel level, NexusData data) {
        int stage = data.getHubStage();
        boolean built = false;
        try {
            if (stage == STAGE_PLAZA) {
                marketCache = null;
                jettyCache = null;
                built = plaza(level, data);
            } else if (stage < STAGE_RING_ROAD) {
                built = avenue(level, AVENUES[stage - STAGE_AVENUE_FIRST]);
            } else if (stage == STAGE_RING_ROAD) {
                built = ringRoad(level);
            } else if (stage == STAGE_MARKET) {
                built = market(level);
            } else if (stage < STAGE_PARCEL_FIRST) {
                built = jetty(level, stage - STAGE_JETTY_FIRST);
            } else {
                built = parcel(level, data, stage - STAGE_PARCEL_FIRST);
            }
        } catch (RuntimeException e) {
            LOGGER.error("[Nexus] Étape {} de la construction du hub impossible, on passe à la suivante", stage, e);
        }
        if (!built) {
            LOGGER.warn("[Nexus] Étape {} du hub ignorée (terrain inadapté ou erreur).", stage);
        }
        data.setHubStage(stage + 1);
        if (stage + 1 >= STAGES) {
            LOGGER.info("[Nexus] Construction du hub terminée : {} maison(s) pour les joueurs.", data.getHomes());
        }
        return built;
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
    static boolean replaceable(BlockState state) {
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

    /** Pose un bloc seulement là où il n'y a rien ou du terrain naturel : jamais sur la construction d'un joueur. */
    static void putSafe(ServerLevel level, int x, int y, int z, BlockState state) {
        if (replaceable(level.getBlockState(new BlockPos(x, y, z)))) {
            set(level, x, y, z, state);
        }
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

    static void pillar(ServerLevel level, int x, int z, int groundY, int height, BlockState cap) {
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

    private static boolean plaza(ServerLevel level, NexusData data) {
        int center = terrainTop(level, 0, 0);
        if (center <= level.getMinBuildHeight() + 2) {
            return false;
        }
        data.setHubY(center);
        int target = center;
        dismantleReturnGate(level, data);
        for (int x = -PLAZA_RADIUS; x <= PLAZA_RADIUS; x++) {
            for (int z = -PLAZA_RADIUS; z <= PLAZA_RADIUS; z++) {
                double r = Math.sqrt(x * x + z * z);
                if (r > PLAZA_RADIUS) {
                    continue;
                }
                flatten(level, x, z, target, plazaBlock(x, z, r));
            }
        }
        NexusDimension.ensureReturnGate(level);
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
        return true;
    }

    /**
     * Le portail de retour, généré à la première arrivée, peut se trouver sur un monticule : on le démonte (anneau,
     * surface et dalle de base, rien d'autre) pour le reconstruire à la hauteur de la place, une fois celle-ci aplanie.
     */
    private static void dismantleReturnGate(ServerLevel level, NexusData data) {
        BlockPos center = data.getReturnGate();
        if (center == null) {
            return;
        }
        BlockState air = Blocks.AIR.defaultBlockState();
        int r = RingGeometry.RADIUS;
        for (int[] c : RingGeometry.ring()) {
            BlockPos pos = NexusRing.cell(center, net.minecraft.core.Direction.Axis.X, c[0], c[1]);
            if (level.getBlockState(pos).is(ModBlocks.ASTRAL_RING_BLOCK.get())) {
                level.setBlock(pos, air, 2);
            }
        }
        for (int[] c : RingGeometry.interior()) {
            BlockPos pos = NexusRing.cell(center, net.minecraft.core.Direction.Axis.X, c[0], c[1]);
            if (level.getBlockState(pos).is(ModBlocks.NEXUS_PORTAL.get())) {
                level.setBlock(pos, air, 2);
            }
        }
        for (int x = -r + 2; x <= r - 2; x++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.offset(x, -r - 1, dz);
                if (level.getBlockState(pos).is(Blocks.DEEPSLATE_TILES)) {
                    level.setBlock(pos, air, 2);
                }
            }
        }
        data.clearReturnGate();
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

    private static boolean avenue(ServerLevel level, int[] d) {
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
        return true;
    }

    private static boolean ringRoad(ServerLevel level) {
        int bound = RING_ROAD_RADIUS + 3;
        for (int x = -bound; x <= bound; x++) {
            for (int z = -bound; z <= bound; z++) {
                double off = Math.abs(Math.sqrt(x * x + z * z) - RING_ROAD_RADIUS);
                if (off <= 1.5D) {
                    pave(level, x, z, off <= 0.6D ? stellar() : crystal());
                }
            }
        }
        return true;
    }

    // ---- Parcelles : maisons des joueurs et village ----

    /** Direction de la porte : du côté de la place du Nexus. */
    private static Direction towardsPlaza(int cx, int cz) {
        if (Math.abs(cx) >= Math.abs(cz)) {
            return cx > 0 ? Direction.WEST : Direction.EAST;
        }
        return cz > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    private static NexusVillage.Kind chooseKind(NexusData data, int ring) {
        if (ring > 0 && data.getHomes() < HOMES) {
            data.addHome();
            return NexusVillage.Kind.HOME;
        }
        int i = data.nextRole();
        if (i >= VILLAGE.length) {
            i = VILLAGE_REPEAT_FROM + (i - VILLAGE_REPEAT_FROM) % (VILLAGE.length - VILLAGE_REPEAT_FROM);
        }
        return VILLAGE[i];
    }

    private static boolean parcel(ServerLevel level, NexusData data, int index) {
        int[] slot = PARCEL_SLOTS.get(index);
        int cx = slot[0];
        int cz = slot[1];
        MarketPlan plan = marketPlan(level);
        if (plan != null && Math.abs(cx - plan.x()) < 30 && Math.abs(cz - plan.z()) < 30) {
            return false;
        }
        int half = PARCEL_SIZE / 2;
        List<Integer> heights = new ArrayList<>();
        for (int x = cx - half; x < cx + half; x++) {
            for (int z = cz - half; z < cz + half; z++) {
                if (hasTerrain(level, x, z)) {
                    heights.add(terrainTop(level, x, z));
                }
            }
        }
        if (heights.size() < PARCEL_SIZE * PARCEL_SIZE * 80 / 100) {
            return false;
        }
        Collections.sort(heights);
        int target = heights.get(heights.size() / 2);
        if (heights.get(heights.size() - 1) - heights.get(0) > 10) {
            return false;
        }
        NexusVillage.Kind kind = chooseKind(data, slot[2]);
        for (int x = cx - half; x < cx + half; x++) {
            for (int z = cz - half; z < cz + half; z++) {
                boolean border = x == cx - half || x == cx + half - 1 || z == cz - half || z == cz + half - 1;
                flatten(level, x, z, target, border ? crystal() : regolith());
            }
        }
        NexusVillage.build(new NexusVillage.Ctx(level, cx, cz, target, towardsPlaza(cx, cz)), kind);
        return true;
    }

    // ---- Place du marché ----

    /** Demi-côté de la place du marché : un carré de 36 x 36 blocs. */
    private static final int MARKET_HALF = 18;

    /** Où se trouve la place du marché : centre (à la distance donnée du centre du hub, sur un axe). */
    private record MarketPlan(Direction outward, int distance) {
        int x() {
            return this.outward.getStepX() * this.distance;
        }

        int z() {
            return this.outward.getStepZ() * this.distance;
        }
    }

    private static MarketPlan marketCache;
    private static List<int[]> jettyCache;

    /** Cherche la place du marché : vers le nord de préférence, sinon le sud, l'est ou l'ouest ; la plus loin possible. */
    private static MarketPlan marketPlan(ServerLevel level) {
        if (marketCache != null) {
            return marketCache;
        }
        for (Direction outward : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            for (int distance = 84; distance >= 44; distance -= 4) {
                int mx = outward.getStepX() * distance;
                int mz = outward.getStepZ() * distance;
                int valid = 0;
                int total = 0;
                int low = Integer.MAX_VALUE;
                int high = Integer.MIN_VALUE;
                for (int x = mx - MARKET_HALF; x < mx + MARKET_HALF; x += 3) {
                    for (int z = mz - MARKET_HALF; z < mz + MARKET_HALF; z += 3) {
                        total++;
                        if (hasTerrain(level, x, z)) {
                            valid++;
                            int y = terrainTop(level, x, z);
                            low = Math.min(low, y);
                            high = Math.max(high, y);
                        }
                    }
                }
                if (valid * 100 >= total * 70 && high - low <= 12) {
                    marketCache = new MarketPlan(outward, distance);
                    return marketCache;
                }
            }
        }
        return null;
    }

    private static boolean market(ServerLevel level) {
        MarketPlan plan = marketPlan(level);
        if (plan == null) {
            return false;
        }
        int mx = plan.x();
        int mz = plan.z();
        List<Integer> heights = new ArrayList<>();
        for (int x = mx - MARKET_HALF; x < mx + MARKET_HALF; x++) {
            for (int z = mz - MARKET_HALF; z < mz + MARKET_HALF; z++) {
                if (hasTerrain(level, x, z)) {
                    heights.add(terrainTop(level, x, z));
                }
            }
        }
        Collections.sort(heights);
        int target = heights.get(heights.size() / 2);
        for (int x = mx - MARKET_HALF; x < mx + MARKET_HALF; x++) {
            for (int z = mz - MARKET_HALF; z < mz + MARKET_HALF; z++) {
                boolean border = x == mx - MARKET_HALF || x == mx + MARKET_HALF - 1 || z == mz - MARKET_HALF
                        || z == mz + MARKET_HALF - 1;
                double r = Math.sqrt((x - mx) * (x - mx) + (z - mz) * (z - mz));
                flatten(level, x, z, target, border || (r >= 5.5D && r < 6.7D) ? crystal() : stellar());
            }
        }
        NexusVillage.Ctx c = new NexusVillage.Ctx(level, mx, mz, target, plan.outward().getOpposite());
        for (int u : new int[]{-MARKET_HALF, MARKET_HALF - 1}) {
            for (int v : new int[]{-MARKET_HALF, MARKET_HALF - 1}) {
                c.post(u, v, 6, beacon());
            }
        }
        monument(c);
        for (int u : new int[]{-12, -4, 4, 12}) {
            stall(c, u, -13);
        }
        for (int u : new int[]{-12, -6, 6, 12}) {
            stall(c, u, 13);
        }
        return true;
    }

    /** Fontaine d'astralite au centre de la place : socle, colonne et balise, entourés de quatre cristaux. */
    private static void monument(NexusVillage.Ctx c) {
        for (int u = -2; u <= 2; u++) {
            for (int v = -2; v <= 2; v++) {
                c.put(u, v, 1, Math.abs(u) == 2 || Math.abs(v) == 2 ? abyssal() : stellar());
            }
        }
        c.fill(-1, 1, -1, 1, 2, 2, stellar());
        c.fill(0, 0, 0, 0, 3, 8, abyssal());
        c.put(0, 0, 9, beacon());
        for (int[] p : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            c.put(p[0], p[1], 2, crystal());
            c.put(p[0], p[1], 3, crystal());
        }
    }

    /** Étal : comptoir de trois blocs, quatre poteaux, toit et lanterne de cristal. */
    private static void stall(NexusVillage.Ctx c, int u, int v) {
        c.fill(u - 1, u + 1, v, v, 1, 1, stellar());
        for (int[] post : new int[][]{{u - 2, v - 1}, {u + 2, v - 1}, {u - 2, v + 1}, {u + 2, v + 1}}) {
            c.fill(post[0], post[0], post[1], post[1], 1, 3, abyssal());
        }
        c.fill(u - 2, u + 2, v - 1, v + 1, 4, 4, abyssal());
        c.put(u, v, 5, crystal());
    }

    // ---- Embarcadères ----

    /** Les deux embarcadères : les deux axes (hors celui du marché) où l'île s'étend le plus loin, à 30 blocs au moins. */
    private static List<int[]> jettyPlan(ServerLevel level) {
        if (jettyCache != null) {
            return jettyCache;
        }
        MarketPlan market = marketPlan(level);
        List<int[]> candidates = new ArrayList<>();
        for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            if (market != null && market.outward().getStepX() == d[0] && market.outward().getStepZ() == d[1]) {
                continue;
            }
            int e = edge(level, d[0], d[1]);
            if (e >= 30) {
                candidates.add(new int[]{d[0], d[1], e});
            }
        }
        candidates.sort((p, q) -> Integer.compare(q[2], p[2]));
        jettyCache = new ArrayList<>(candidates.subList(0, Math.min(JETTIES, candidates.size())));
        return jettyCache;
    }

    private static boolean jetty(ServerLevel level, int index) {
        List<int[]> plan = jettyPlan(level);
        if (index >= plan.size()) {
            return false;
        }
        int[] d = plan.get(index);
        Direction outward = Direction.EAST;
        for (Direction candidate : Direction.Plane.HORIZONTAL) {
            if (candidate.getStepX() == d[0] && candidate.getStepZ() == d[1]) {
                outward = candidate;
            }
        }
        int platform = d[2] - 5;
        int px = d[0] * platform;
        int pz = d[1] * platform;
        int y = terrainTop(level, px, pz);
        if (y <= level.getMinBuildHeight() + 2) {
            return false;
        }
        NexusVillage.Ctx c = new NexusVillage.Ctx(level, px, pz, y, outward);
        for (int u = -4; u <= 4; u++) {
            for (int v = -4; v <= 4; v++) {
                BlockPos p = c.pos(u, v, 0);
                boolean border = Math.abs(u) == 4 || Math.abs(v) == 4;
                flatten(level, p.getX(), p.getZ(), y, border ? crystal() : stellar());
            }
        }
        for (int v = 5; v <= 18; v++) {
            for (int u = -1; u <= 1; u++) {
                c.put(u, v, 0, Math.abs(u) == 1 ? crystal() : stellar());
            }
            if (v % 4 == 1) {
                c.post(-2, v, 3, beacon());
                c.post(2, v, 3, beacon());
            }
        }
        c.post(0, 19, 6, beacon());
        return true;
    }
}
