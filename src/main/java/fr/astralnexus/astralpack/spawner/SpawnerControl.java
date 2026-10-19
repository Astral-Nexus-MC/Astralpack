package fr.astralnexus.astralpack.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Applique les niveaux d'une console aux spawners de son chunk, sans jamais perdre les réglages d'origine : la
 * première fois qu'une console touche un spawner, ses valeurs d'origine sont gardées dans ses données persistantes
 * ({@code AstralBase}) ; chaque application part de ces valeurs. Les restaurer (console cassée, spawner décroché)
 * remet le spawner exactement comme il était.
 */
public final class SpawnerControl {
    private static final String BASE = "AstralBase";
    private static final String APPLIED = "AstralApplied";

    private SpawnerControl() {
    }

    /** Spawners du chunk qui contient cette position. */
    public static List<SpawnerBlockEntity> spawnersInChunk(Level level, BlockPos pos) {
        List<SpawnerBlockEntity> found = new ArrayList<>();
        LevelChunk chunk = level.getChunkAt(pos);
        for (BlockEntity entity : chunk.getBlockEntities().values()) {
            if (entity instanceof SpawnerBlockEntity spawner && !spawner.isRemoved()) {
                found.add(spawner);
            }
        }
        return found;
    }

    /** Valeur d'origine d'une clé du spawner : celle d'avant la console si elle a déjà touché ce spawner, sinon sa valeur actuelle. */
    public static int baseValue(SpawnerBlockEntity spawner, String key) {
        CompoundTag persistent = spawner.getPersistentData();
        if (persistent.contains(BASE)) {
            return persistent.getCompound(BASE).getInt(key);
        }
        return spawner.saveWithoutMetadata().getInt(key);
    }

    /** Console du chunk qui contient cette position, ou {@code null}. */
    public static fr.astralnexus.astralpack.block.entity.SpawnerConsoleBlockEntity findConsole(Level level, BlockPos pos) {
        for (BlockEntity entity : level.getChunkAt(pos).getBlockEntities().values()) {
            if (entity instanceof fr.astralnexus.astralpack.block.entity.SpawnerConsoleBlockEntity console
                    && !console.isRemoved()) {
                return console;
            }
        }
        return null;
    }

    /** Applique les niveaux ; renvoie vrai si le spawner a changé. */
    public static boolean apply(SpawnerBlockEntity spawner, int[] levels) {
        boolean neutral = true;
        for (int level : levels) {
            neutral &= level == 0;
        }
        if (neutral) {
            return restore(spawner);
        }
        CompoundTag persistent = spawner.getPersistentData();
        if (!persistent.contains(BASE)) {
            CompoundTag current = spawner.saveWithoutMetadata();
            CompoundTag base = new CompoundTag();
            for (SpawnerStat stat : SpawnerStat.values()) {
                for (String key : stat.keys()) {
                    base.putInt(key, current.getInt(key));
                }
            }
            persistent.put(BASE, base);
        }
        String signature = signature(levels);
        if (signature.equals(persistent.getString(APPLIED))) {
            return false;
        }
        persistent.putString(APPLIED, signature);
        write(spawner, persistent.getCompound(BASE), levels);
        return true;
    }

    /** Remet un spawner comme il était avant la console. */
    public static boolean restore(SpawnerBlockEntity spawner) {
        CompoundTag persistent = spawner.getPersistentData();
        if (!persistent.contains(BASE)) {
            return false;
        }
        CompoundTag base = persistent.getCompound(BASE);
        persistent.remove(BASE);
        persistent.remove(APPLIED);
        write(spawner, base, new int[SpawnerStat.values().length]);
        return true;
    }

    private static void write(SpawnerBlockEntity spawner, CompoundTag base, int[] levels) {
        CompoundTag data = spawner.saveWithoutMetadata();
        for (SpawnerStat stat : SpawnerStat.values()) {
            String[] keys = stat.keys();
            for (String key : keys) {
                data.putShort(key, (short) stat.apply(base.getInt(key), levels[stat.ordinal()]));
            }
            if (keys.length == 2 && data.getShort(keys[0]) > data.getShort(keys[1])) {
                data.putShort(keys[0], data.getShort(keys[1]));
            }
        }
        spawner.load(data);
        spawner.setChanged();
        if (spawner.getLevel() != null) {
            spawner.getLevel().sendBlockUpdated(spawner.getBlockPos(), spawner.getBlockState(), spawner.getBlockState(), 3);
        }
    }

    private static String signature(int[] levels) {
        StringBuilder builder = new StringBuilder();
        for (int level : levels) {
            builder.append(level).append(',');
        }
        return builder.toString();
    }
}
