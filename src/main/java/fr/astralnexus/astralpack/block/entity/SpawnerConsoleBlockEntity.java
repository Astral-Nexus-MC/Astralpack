package fr.astralnexus.astralpack.block.entity;

import fr.astralnexus.astralpack.network.Notify;
import fr.astralnexus.astralpack.registry.ModBlockEntities;
import fr.astralnexus.astralpack.spawner.SpawnerControl;
import fr.astralnexus.astralpack.spawner.SpawnerStat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Console de contrôle des spawners : garde un niveau (de -5 à +5) pour chacun des cinq réglages et l'applique aux
 * spawners du chunk où elle se trouve, et à eux seuls. Les spawners posés plus tard dans le chunk sont pris en compte
 * au passage suivant ; ceux qui quittent le chunk, ou toute la console si on la casse, retrouvent leurs réglages d'origine.
 */
public class SpawnerConsoleBlockEntity extends BlockEntity {
    /** Intervalle, en ticks, entre deux passages sur les spawners du chunk. */
    private static final int PASS_INTERVAL = 40;

    private final int[] levels = new int[SpawnerStat.values().length];
    private int countdown;

    public SpawnerConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPAWNER_CONSOLE.get(), pos, state);
    }

    public int level(SpawnerStat stat) {
        return this.levels[stat.ordinal()];
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpawnerConsoleBlockEntity console) {
        if (++console.countdown >= PASS_INTERVAL) {
            console.countdown = 0;
            console.applyAll();
        }
    }

    /** Applique les niveaux à tous les spawners du chunk ; renvoie leur nombre. */
    public int applyAll() {
        if (this.level == null || this.level.isClientSide) {
            return 0;
        }
        List<SpawnerBlockEntity> spawners = SpawnerControl.spawnersInChunk(this.level, this.worldPosition);
        for (SpawnerBlockEntity spawner : spawners) {
            SpawnerControl.apply(spawner, this.levels);
        }
        return spawners.size();
    }

    /** Remet tous les spawners du chunk comme ils étaient (console cassée). */
    public void restoreAll() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        for (SpawnerBlockEntity spawner : SpawnerControl.spawnersInChunk(this.level, this.worldPosition)) {
            SpawnerControl.restore(spawner);
        }
    }

    /** Clic droit avec un objet : renvoie vrai s'il s'agissait d'un objet de la console. */
    public boolean tryUpgrade(Player player, ItemStack held) {
        boolean[] bonus = new boolean[1];
        SpawnerStat stat = SpawnerStat.forItem(held, bonus);
        if (stat == null) {
            return false;
        }
        int current = this.levels[stat.ordinal()];
        int next = current + (bonus[0] ? 1 : -1);
        if (Math.abs(next) > SpawnerStat.MAX_LEVEL) {
            Notify.send(player, Component.translatable("message.astralpack.spawner_console.limit",
                    Component.translatable(stat.translationKey()), signed(current)), Notify.Kind.WARNING);
            return true;
        }
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        this.levels[stat.ordinal()] = next;
        this.setChanged();
        int count = this.applyAll();
        if (this.level instanceof ServerLevel server) {
            server.playSound(null, this.worldPosition, bonus[0] ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.ANVIL_LAND,
                    SoundSource.BLOCKS, 1.0F, bonus[0] ? 1.2F : 0.6F);
            for (SpawnerBlockEntity spawner : SpawnerControl.spawnersInChunk(server, this.worldPosition)) {
                BlockPos at = spawner.getBlockPos();
                server.sendParticles(bonus[0] ? ParticleTypes.END_ROD : ParticleTypes.SMOKE, at.getX() + 0.5D,
                        at.getY() + 1.0D, at.getZ() + 0.5D, 12, 0.3D, 0.3D, 0.3D, 0.02D);
            }
        }
        Notify.send(player, Component.translatable("message.astralpack.spawner_console.changed",
                Component.translatable(stat.translationKey()), signed(next), count),
                bonus[0] ? Notify.Kind.SUCCESS : Notify.Kind.INFO);
        return true;
    }

    /** Résumé dans le chat : niveaux, objets demandés, nombre de spawners sous contrôle. */
    public void sendStatus(Player player) {
        int count = this.level == null ? 0 : SpawnerControl.spawnersInChunk(this.level, this.worldPosition).size();
        player.sendSystemMessage(Component.translatable("message.astralpack.spawner_console.status", count));
        for (SpawnerStat stat : SpawnerStat.values()) {
            player.sendSystemMessage(Component.translatable("message.astralpack.spawner_console.line",
                    Component.translatable(stat.translationKey()), signed(this.level(stat)),
                    stat.bonusName(), stat.malusName()));
        }
    }

    private static String signed(int level) {
        return level > 0 ? "+" + level : Integer.toString(level);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putIntArray("Levels", this.levels);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int[] saved = tag.getIntArray("Levels");
        for (int i = 0; i < this.levels.length && i < saved.length; i++) {
            this.levels[i] = saved[i];
        }
    }
}
