package fr.astralnexus.astralpack.world;

import fr.astralnexus.astralpack.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Les bâtiments du village du hub : les maisons des joueurs (utilisables : lit, coffre, établi, four, lumière) et les
 * bâtiments du village (auberge, forge, bibliothèque, entrepôt, chapelle, atelier d'alchimie, tour de guet, puits,
 * serre, ferme, jardin). Chaque bâtiment est construit dans un repère local : {@code u} va vers la droite, {@code v}
 * vers l'avant (le côté de la place du Nexus, où se trouve la porte), {@code y} vers le haut, {@code y = 0} étant le sol.
 */
final class NexusVillage {
    /** Types de bâtiments. */
    enum Kind {
        HOME, INN, FORGE, LIBRARY, WAREHOUSE, CHAPEL, ALCHEMIST, TOWER, WELL, GREENHOUSE, FARM, GARDEN
    }

    private NexusVillage() {
    }

    /** Repère local d'une parcelle : centre, hauteur du sol, côté de la porte. */
    static final class Ctx {
        final ServerLevel level;
        final int cx;
        final int cz;
        final int y0;
        final Direction front;
        final Direction back;
        final Direction right;
        final Direction left;

        Ctx(ServerLevel level, int cx, int cz, int y0, Direction front) {
            this.level = level;
            this.cx = cx;
            this.cz = cz;
            this.y0 = y0;
            this.front = front;
            this.back = front.getOpposite();
            this.right = front.getClockWise();
            this.left = this.right.getOpposite();
        }

        BlockPos pos(int u, int v, int y) {
            return new BlockPos(this.cx + this.right.getStepX() * u + this.front.getStepX() * v, this.y0 + y,
                    this.cz + this.right.getStepZ() * u + this.front.getStepZ() * v);
        }

        void put(int u, int v, int y, BlockState state) {
            BlockPos p = this.pos(u, v, y);
            NexusHubBuilder.putSafe(this.level, p.getX(), p.getY(), p.getZ(), state);
        }

        void fill(int u1, int u2, int v1, int v2, int y1, int y2, BlockState state) {
            for (int u = Math.min(u1, u2); u <= Math.max(u1, u2); u++) {
                for (int v = Math.min(v1, v2); v <= Math.max(v1, v2); v++) {
                    for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                        this.put(u, v, y, state);
                    }
                }
            }
        }

        /** Pilier posé sur le sol de la parcelle : poteau sombre et tête lumineuse. */
        void post(int u, int v, int height, BlockState cap) {
            BlockPos p = this.pos(u, v, 0);
            NexusHubBuilder.pillar(this.level, p.getX(), p.getZ(), this.y0, height, cap);
        }
    }

    // ---- Blocs ----

    private static BlockState wall() {
        return ModBlocks.ABYSSAL_STONE.get().defaultBlockState();
    }

    private static BlockState stone() {
        return ModBlocks.STELLAR_STONE.get().defaultBlockState();
    }

    private static BlockState crystal() {
        return ModBlocks.REEF_CRYSTAL.get().defaultBlockState();
    }

    private static BlockState moss() {
        return ModBlocks.LUMEN_MOSS.get().defaultBlockState();
    }

    private static BlockState regolith() {
        return ModBlocks.ASTRAL_REGOLITH.get().defaultBlockState();
    }

    private static BlockState beacon() {
        return ModBlocks.ASTRALITE_BLOCK.get().defaultBlockState();
    }

    private static BlockState glass() {
        return Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
    }

    private static BlockState lantern() {
        return Blocks.SEA_LANTERN.defaultBlockState();
    }

    private static BlockState carpet() {
        return Blocks.PURPLE_CARPET.defaultBlockState();
    }

    private static BlockState tiles() {
        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }

    private static BlockState air() {
        return Blocks.AIR.defaultBlockState();
    }

    private static BlockState facing(Block block, Direction direction) {
        return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
    }

    private static BlockState stairs(Direction ascendsTowards, boolean top) {
        return Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, ascendsTowards)
                .setValue(BlockStateProperties.HALF, top ? Half.TOP : Half.BOTTOM);
    }

    private static BlockState slab() {
        return Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState();
    }

    // ---- Éléments communs ----

    /** Altitude du toit à la rangée {@code v} : un bloc de plus par rangée vers le faîtage. */
    private static int roofY(int h, int hv, int v) {
        return h + 1 + (hv + 1 - Math.abs(v));
    }

    private static void door(Ctx c, int u, int v) {
        BlockState lower = facing(Blocks.WARPED_DOOR, c.back).setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,
                DoubleBlockHalf.LOWER);
        BlockState upper = lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        c.put(u, v, 1, lower);
        c.put(u, v, 2, upper);
    }

    private static void bed(Ctx c, int u, int v, Direction towardsHead) {
        BlockPos foot = c.pos(u, v, 1);
        BlockState state = facing(Blocks.PURPLE_BED, towardsHead);
        NexusHubBuilder.putSafe(c.level, foot.getX(), foot.getY(), foot.getZ(),
                state.setValue(BlockStateProperties.BED_PART, BedPart.FOOT));
        BlockPos head = foot.relative(towardsHead);
        NexusHubBuilder.putSafe(c.level, head.getX(), head.getY(), head.getZ(),
                state.setValue(BlockStateProperties.BED_PART, BedPart.HEAD));
    }

    private static void waterCauldron(Ctx c, int u, int v) {
        c.put(u, v, 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
    }

    /**
     * Salle à toit en bâtière : sol, murs (coins de cristal), plafond, toit, porte au milieu du mur avant, fenêtres,
     * chemin et lampadaires devant. {@code hu} et {@code hv} sont les demi-largeur et demi-profondeur des murs.
     */
    private static void hall(Ctx c, int hu, int hv, int h) {
        c.fill(-hu, hu, -hv, hv, 0, 0, stone());
        for (int y = 1; y <= h; y++) {
            c.fill(-hu, hu, -hv, -hv, y, y, wall());
            c.fill(-hu, hu, hv, hv, y, y, wall());
            c.fill(-hu, -hu, -hv, hv, y, y, wall());
            c.fill(hu, hu, -hv, hv, y, y, wall());
        }
        for (int[] corner : new int[][]{{-hu, -hv}, {hu, -hv}, {-hu, hv}, {hu, hv}}) {
            c.fill(corner[0], corner[0], corner[1], corner[1], 1, h, crystal());
        }
        // Toit : prisme plein sous un revêtement d'escaliers, faîtage de tuiles, débord d'une rangée.
        for (int v = -hv; v <= hv; v++) {
            c.fill(-hu, hu, v, v, h + 1, roofY(h, hv, v) - 1, wall());
        }
        for (int v = -hv - 1; v <= hv + 1; v++) {
            int y = roofY(h, hv, v);
            if (v == 0) {
                c.fill(-hu - 1, hu + 1, 0, 0, y, y, tiles());
            } else {
                c.fill(-hu - 1, hu + 1, v, v, y, y, stairs(v > 0 ? c.back : c.front, false));
            }
        }
        door(c, 0, hv);
        c.fill(-1, 1, hv + 1, 8, 0, 0, stone());
        c.post(-2, hv + 3, 3, lantern());
        c.post(2, hv + 3, 3, lantern());
        // Fenêtres : deux sur la façade, une au fond, une sur chaque côté.
        int window = Math.max(2, hu - 2);
        for (int sign = -1; sign <= 1; sign += 2) {
            c.fill(sign * window, sign * window, hv, hv, 2, 3, glass());
            c.fill(sign * hu, sign * hu, 0, 0, 2, 3, glass());
        }
        c.fill(0, 0, -hv, -hv, 2, 3, glass());
        c.put(0, 0, h + 1, lantern());
    }

    /** Jardinet devant la maison : mousse lumineuse et petits cristaux. */
    private static void frontGarden(Ctx c, int hv) {
        for (int sign = -1; sign <= 1; sign += 2) {
            c.fill(sign * 4, sign * 6, hv + 2, hv + 4, 0, 0, moss());
            c.put(sign * 5, hv + 3, 1, crystal());
            c.put(sign * 5, hv + 3, 2, crystal());
        }
    }

    // ---- Bâtiments ----

    static void build(Ctx c, Kind kind) {
        switch (kind) {
            case HOME -> home(c);
            case INN -> inn(c);
            case FORGE -> forge(c);
            case LIBRARY -> library(c);
            case WAREHOUSE -> warehouse(c);
            case CHAPEL -> chapel(c);
            case ALCHEMIST -> alchemist(c);
            case TOWER -> tower(c);
            case WELL -> well(c);
            case GREENHOUSE -> greenhouse(c);
            case FARM -> farm(c);
            case GARDEN -> garden(c);
        }
    }

    /** Maison de joueur : une pièce, un lit, un coffre, un établi, un four, un tapis et de la lumière. */
    private static void home(Ctx c) {
        hall(c, 4, 4, 4);
        frontGarden(c, 4);
        bed(c, 2, -2, c.back);
        c.put(-3, -3, 1, facing(Blocks.CHEST, c.right));
        c.put(-3, -2, 1, Blocks.CRAFTING_TABLE.defaultBlockState());
        c.put(-3, -1, 1, facing(Blocks.FURNACE, c.right));
        c.fill(-1, 1, -1, 1, 1, 1, carpet());
        c.put(3, 2, 1, Blocks.BOOKSHELF.defaultBlockState());
        c.put(3, 2, 2, Blocks.BOOKSHELF.defaultBlockState());
    }

    private static void inn(Ctx c) {
        hall(c, 6, 5, 4);
        frontGarden(c, 5);
        c.fill(-4, 4, -3, -3, 1, 1, stone());
        for (int u = -4; u <= 4; u += 2) {
            c.put(u, -4, 1, Blocks.BARREL.defaultBlockState());
        }
        for (int[] t : new int[][]{{-3, 0}, {3, 0}, {-3, 2}, {3, 2}}) {
            c.put(t[0], t[1], 1, stone());
            c.put(t[0], t[1], 2, Blocks.DARK_OAK_PRESSURE_PLATE.defaultBlockState());
            c.put(t[0] + (t[0] < 0 ? 1 : -1), t[1], 1, slab());
        }
        bed(c, -5, 3, c.front);
        bed(c, 5, 3, c.front);
        c.fill(-1, 1, 0, 3, 1, 1, carpet());
        c.put(-3, 0, 5, lantern());
        c.put(3, 0, 5, lantern());
    }

    private static void forge(Ctx c) {
        hall(c, 5, 4, 4);
        frontGarden(c, 4);
        for (int u = -3; u <= 3; u += 3) {
            c.put(u, -3, 1, facing(Blocks.BLAST_FURNACE, c.front));
        }
        c.put(-4, 1, 1, facing(Blocks.ANVIL, c.front));
        c.put(4, 0, 1, Blocks.GRINDSTONE.defaultBlockState());
        c.put(4, -1, 1, Blocks.SMITHING_TABLE.defaultBlockState());
        waterCauldron(c, 4, -3);
        c.put(-4, -2, 1, facing(Blocks.CHEST, c.right));
        c.put(-4, -1, 1, facing(Blocks.CHEST, c.right));
        // Cheminée.
        c.fill(3, 3, -4, -4, 5, 13, wall());
        c.put(3, -4, 14, crystal());
    }

    private static void library(Ctx c) {
        hall(c, 5, 5, 5);
        frontGarden(c, 5);
        for (int v = -4; v <= 3; v++) {
            for (int y = 1; y <= 3; y++) {
                c.put(-4, v, y, Blocks.BOOKSHELF.defaultBlockState());
                c.put(4, v, y, Blocks.BOOKSHELF.defaultBlockState());
            }
        }
        for (int u = -3; u <= 3; u++) {
            c.put(u, -4, 1, Blocks.BOOKSHELF.defaultBlockState());
            c.put(u, -4, 2, Blocks.BOOKSHELF.defaultBlockState());
        }
        c.put(0, 0, 1, Blocks.ENCHANTING_TABLE.defaultBlockState());
        for (int v = -1; v <= 1; v++) {
            c.put(-2, v, 1, Blocks.BOOKSHELF.defaultBlockState());
            c.put(-2, v, 2, Blocks.BOOKSHELF.defaultBlockState());
            c.put(2, v, 1, Blocks.BOOKSHELF.defaultBlockState());
            c.put(2, v, 2, Blocks.BOOKSHELF.defaultBlockState());
        }
        c.put(0, 3, 1, facing(Blocks.LECTERN, c.back));
        c.fill(-1, 1, 1, 4, 1, 1, carpet());
    }

    private static void warehouse(Ctx c) {
        hall(c, 6, 4, 4);
        frontGarden(c, 4);
        for (int u = -5; u <= 5; u++) {
            c.put(u, -3, 1, facing(Blocks.CHEST, c.front));
            c.put(u, -3, 2, u % 2 == 0 ? Blocks.BARREL.defaultBlockState() : facing(Blocks.CHEST, c.front));
        }
        for (int v = -2; v <= 2; v++) {
            c.put(-5, v, 1, Blocks.BARREL.defaultBlockState());
            c.put(5, v, 1, Blocks.BARREL.defaultBlockState());
        }
        c.put(-3, 0, 1, stone());
        c.put(3, 0, 1, stone());
    }

    private static void chapel(Ctx c) {
        hall(c, 4, 6, 5);
        frontGarden(c, 6);
        c.fill(-2, 2, -5, -5, 1, 1, stone());
        c.put(0, -5, 2, beacon());
        c.put(-2, -5, 2, crystal());
        c.put(2, -5, 2, crystal());
        c.put(-2, -5, 3, crystal());
        c.put(2, -5, 3, crystal());
        c.fill(-1, 1, -3, 5, 1, 1, carpet());
        for (int v : new int[]{-1, 1, 3}) {
            c.put(-3, v, 1, slab());
            c.put(-2, v, 1, slab());
            c.put(2, v, 1, slab());
            c.put(3, v, 1, slab());
        }
        for (int v : new int[]{-3, 0, 3}) {
            c.fill(-4, -4, v, v, 2, 4, glass());
            c.fill(4, 4, v, v, 2, 4, glass());
            c.put(0, v, 6, lantern());
        }
    }

    private static void alchemist(Ctx c) {
        hall(c, 4, 4, 4);
        frontGarden(c, 4);
        c.put(-3, -3, 1, Blocks.BREWING_STAND.defaultBlockState());
        c.put(3, -3, 1, Blocks.BREWING_STAND.defaultBlockState());
        waterCauldron(c, -3, -1);
        waterCauldron(c, 3, -1);
        c.put(0, -3, 1, stone());
        c.put(0, -3, 2, Blocks.BREWING_STAND.defaultBlockState());
        c.put(-3, 1, 1, Blocks.BARREL.defaultBlockState());
        c.put(3, 1, 1, Blocks.BARREL.defaultBlockState());
        c.put(-3, 2, 1, facing(Blocks.CHEST, c.right));
        c.fill(-1, 1, 0, 2, 1, 1, carpet());
    }

    private static void tower(Ctx c) {
        int top = 12;
        c.fill(-2, 2, -2, 2, 0, 0, stone());
        for (int y = 1; y <= top - 1; y++) {
            c.fill(-2, 2, -2, -2, y, y, wall());
            c.fill(-2, 2, 2, 2, y, y, wall());
            c.fill(-2, -2, -2, 2, y, y, wall());
            c.fill(2, 2, -2, 2, y, y, wall());
        }
        for (int[] corner : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            c.fill(corner[0], corner[0], corner[1], corner[1], 1, top, crystal());
        }
        c.fill(-2, 2, -2, 2, top, top, stone());
        c.put(0, -1, top, air());
        for (int y = 1; y <= top; y++) {
            c.put(0, -1, y, facing(Blocks.LADDER, c.front));
        }
        for (int u = -2; u <= 2; u++) {
            for (int v : new int[]{-2, 2}) {
                c.put(u, v, top + 1, wall());
            }
        }
        for (int v = -1; v <= 1; v++) {
            c.put(-2, v, top + 1, wall());
            c.put(2, v, top + 1, wall());
        }
        c.put(0, 0, top + 1, beacon());
        c.put(0, 0, top + 2, beacon());
        door(c, 0, 2);
        for (int y : new int[]{4, 8}) {
            c.fill(-2, -2, 0, 0, y, y + 1, glass());
            c.fill(2, 2, 0, 0, y, y + 1, glass());
            c.fill(0, 0, -2, -2, y, y + 1, glass());
        }
        c.put(0, 0, 4, lantern());
        c.fill(-1, 1, 3, 8, 0, 0, stone());
        c.post(-2, 5, 3, lantern());
        c.post(2, 5, 3, lantern());
    }

    private static void well(Ctx c) {
        c.fill(-3, 3, -3, 3, 0, 0, stone());
        for (int u = -1; u <= 1; u++) {
            for (int v = -1; v <= 1; v++) {
                if (u == 0 && v == 0) {
                    c.put(0, 0, 0, Blocks.WATER.defaultBlockState());
                    c.put(0, 0, -1, Blocks.WATER.defaultBlockState());
                } else {
                    c.put(u, v, 1, wall());
                }
            }
        }
        for (int[] p : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            c.fill(p[0], p[0], p[1], p[1], 1, 4, wall());
            c.put(p[0], p[1], 5, crystal());
        }
        c.fill(-2, 2, -2, 2, 5, 5, tiles());
        c.put(0, 0, 4, lantern());
        c.fill(-1, 1, 4, 8, 0, 0, stone());
    }

    private static void greenhouse(Ctx c) {
        int hu = 5;
        int hv = 4;
        c.fill(-hu, hu, -hv, hv, 0, 0, stone());
        for (int y = 1; y <= 3; y++) {
            c.fill(-hu, hu, -hv, -hv, y, y, y == 1 ? wall() : glass());
            c.fill(-hu, hu, hv, hv, y, y, y == 1 ? wall() : glass());
            c.fill(-hu, -hu, -hv, hv, y, y, y == 1 ? wall() : glass());
            c.fill(hu, hu, -hv, hv, y, y, y == 1 ? wall() : glass());
        }
        c.fill(-hu, hu, -hv, hv, 4, 4, glass());
        c.fill(-hu + 2, hu - 2, -hv + 2, hv - 2, 5, 5, glass());
        for (int[] corner : new int[][]{{-hu, -hv}, {hu, -hv}, {-hu, hv}, {hu, hv}}) {
            c.fill(corner[0], corner[0], corner[1], corner[1], 1, 4, crystal());
        }
        for (int u = -4; u <= 4; u++) {
            for (int v = -3; v <= 2; v++) {
                if (Math.abs(u) == 2) {
                    c.put(u, v, 0, Blocks.WATER.defaultBlockState());
                } else {
                    c.put(u, v, 0, Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
                    c.put(u, v, 1, (u + v) % 2 == 0 ? Blocks.WHEAT.defaultBlockState().setValue(BlockStateProperties.AGE_7, 7)
                            : Blocks.CARROTS.defaultBlockState().setValue(BlockStateProperties.AGE_7, 7));
                }
            }
        }
        c.put(0, 0, 4, lantern());
        c.put(-3, 0, 4, lantern());
        c.put(3, 0, 4, lantern());
        door(c, 0, hv);
        c.put(0, hv - 1, 0, stone());
        c.fill(-1, 1, hv + 1, 8, 0, 0, stone());
    }

    private static void farm(Ctx c) {
        for (int strip = -6; strip <= 6; strip += 4) {
            c.fill(strip - 1, strip + 1, -5, 4, 0, 0,
                    Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
            c.fill(strip, strip, -5, 4, 0, 0, Blocks.WATER.defaultBlockState());
            for (int v = -5; v <= 4; v++) {
                for (int du : new int[]{-1, 1}) {
                    c.put(strip + du, v, 1, (v % 2 == 0 ? Blocks.WHEAT : Blocks.POTATOES).defaultBlockState()
                            .setValue(BlockStateProperties.AGE_7, 7));
                }
            }
            c.fill(strip + 2, strip + 2, -5, 4, 0, 0, stone());
        }
        for (int[] p : new int[][]{{-8, -6}, {8, -6}, {-8, 5}, {8, 5}}) {
            c.post(p[0], p[1], 3, lantern());
        }
        c.fill(-1, 1, 5, 8, 0, 0, stone());
    }

    private static void garden(Ctx c) {
        c.fill(-7, 7, -7, 7, 0, 0, moss());
        c.fill(-2, 2, -2, 2, 0, 0, stone());
        c.fill(-1, 1, -1, 1, 0, 0, Blocks.WATER.defaultBlockState());
        for (int u = -2; u <= 2; u++) {
            for (int v = -2; v <= 2; v++) {
                if (Math.abs(u) == 2 || Math.abs(v) == 2) {
                    c.put(u, v, 1, crystal());
                }
            }
        }
        for (int[] p : new int[][]{{-5, -5}, {5, -5}, {-5, 4}, {5, 4}, {0, -6}}) {
            int height = 3 + Math.abs(p[0] + p[1]) % 3;
            for (int y = 1; y <= height; y++) {
                c.put(p[0], p[1], y, crystal());
            }
            c.put(p[0], p[1], height + 1, beacon());
        }
        for (int u : new int[]{-4, 4}) {
            c.put(u, 0, 1, stairs(u < 0 ? c.right : c.left, false));
        }
        c.fill(-1, 1, 3, 8, 0, 0, stone());
    }
}
