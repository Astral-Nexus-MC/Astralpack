package fr.astralnexus.astralpack.world;

import fr.astralnexus.astralpack.Astralpack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Dimension hub « Nexus Astral » : clés, point d'apparition sûr et téléportation. */
public final class NexusDimension {
    public static final ResourceKey<Level> NEXUS = key("nexus");
    /** Variante de comparaison (ciel de l'End), à retirer une fois la lumière choisie. */
    public static final ResourceKey<Level> NEXUS_B = key("nexus_b");

    /** Hauteur de la plateforme de secours si le centre est vide. */
    private static final int PLATFORM_Y = 64;

    private NexusDimension() {
    }

    private static ResourceKey<Level> key(String name) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(Astralpack.MOD_ID, name));
    }

    public static boolean isNexus(Level level) {
        return level.dimension().equals(NEXUS) || level.dimension().equals(NEXUS_B);
    }

    /** Point d'apparition du hub : au-dessus de l'île centrale, sinon sur une petite plateforme créée à la volée. */
    public static Vec3 hubSpawn(ServerLevel level) {
        level.getChunk(ChunkPos.ZERO.x, ChunkPos.ZERO.z);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
        if (y <= level.getMinBuildHeight() + 1) {
            BlockPos center = new BlockPos(0, PLATFORM_Y - 1, 0);
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-3, 0, -3), center.offset(3, 0, 3))) {
                level.setBlockAndUpdate(pos, Blocks.DEEPSLATE_TILES.defaultBlockState());
            }
            y = PLATFORM_Y;
        }
        return new Vec3(0.5, y, 0.5);
    }

    public static void teleportToHub(ServerPlayer player, ServerLevel level) {
        Vec3 spawn = hubSpawn(level);
        player.teleportTo(level, spawn.x, spawn.y, spawn.z, player.getYRot(), 0.0F);
        player.fallDistance = 0.0F;
    }
}
