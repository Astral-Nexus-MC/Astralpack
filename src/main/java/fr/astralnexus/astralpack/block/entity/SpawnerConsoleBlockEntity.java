package fr.astralnexus.astralpack.block.entity;

import fr.astralnexus.astralpack.network.Notify;
import fr.astralnexus.astralpack.registry.ModBlockEntities;
import fr.astralnexus.astralpack.spawner.SpawnerControl;
import fr.astralnexus.astralpack.spawner.SpawnerStat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.player.Inventory;
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

    /** Clic sur un bouton de l'écran de la console : prend l'objet dans le sac (ou un exemplaire en créatif) et monte ou descend le réglage d'un cran. */
    public void tryUpgradeFromScreen(ServerPlayer player, SpawnerStat stat, boolean bonus) {
        ItemStack found = null;
        Inventory inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (stat.matchesItem(stack, bonus)) {
                found = stack;
                break;
            }
        }
        if (found == null && stat.matchesItem(player.getOffhandItem(), bonus)) {
            found = player.getOffhandItem();
        }
        if (found == null && player.getAbilities().instabuild) {
            found = stat.sampleStack(bonus);
        }
        if (found == null || found.isEmpty()) {
            Notify.send(player, Component.translatable("message.astralpack.spawner_console.missing", stat.itemName(bonus)), Notify.Kind.WARNING);
            return;
        }
        this.tryUpgrade(player, found);
    }

    private static String json(Component component) {
        return Component.Serializer.toJson(component);
    }

    private Component valueOf(SpawnerStat stat, List<SpawnerBlockEntity> spawners) {
        String prefix = "message.astralpack.spawner_console.value.";
        int level = this.level(stat);
        if (stat == SpawnerStat.HEALTH) {
            if (level == 0) {
                return Component.translatable(prefix + "health.same");
            }
            if (level <= -SpawnerStat.MAX_LEVEL) {
                return Component.translatable(prefix + "health.min");
            }
            double factor = SpawnerStat.health(1.0D, level);
            return Component.translatable(prefix + "health", factor == Math.floor(factor)
                    ? Integer.toString((int) factor) : String.format(java.util.Locale.ROOT, "%.1f", factor));
        }
        if (spawners.isEmpty()) {
            return Component.translatable(prefix + "none");
        }
        SpawnerBlockEntity first = spawners.get(0);
        String[] keys = stat.keys();
        StringBuilder before = new StringBuilder();
        StringBuilder after = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            int base = SpawnerControl.baseValue(first, keys[i]);
            before.append(i > 0 ? "-" : "").append(base);
            after.append(i > 0 ? "-" : "").append(stat.apply(base, level));
        }
        return level == 0 ? Component.translatable(prefix + stat.id() + ".same", before.toString())
                : Component.translatable(prefix + stat.id(), before.toString(), after.toString());
    }

    /** Les données de l'écran de la console (format de {@code FordixConsoles}, de Fordix) : niveaux, objets demandés, valeurs, monstres des spawners. */
    public CompoundTag screenData() {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", fr.astralnexus.astralpack.network.ConsoleBridge.KIND);
        tag.putIntArray("pos", new int[]{this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ()});
        List<SpawnerBlockEntity> spawners = this.level == null ? List.of() : SpawnerControl.spawnersInChunk(this.level, this.worldPosition);
        ChunkPos chunk = new ChunkPos(this.worldPosition);
        tag.putString("title", json(Component.translatable("block.astralpack.spawner_console")));
        tag.putString("subtitle", json(Component.translatable("message.astralpack.spawner_console.subtitle", chunk.x, chunk.z)));
        tag.putString("pill", json(spawners.isEmpty() ? Component.translatable("message.astralpack.spawner_console.pill_none")
                : Component.translatable("message.astralpack.spawner_console.pill", spawners.size())));
        tag.putString("footer", json(Component.translatable("message.astralpack.spawner_console.footer")));
        tag.putInt("max", SpawnerStat.MAX_LEVEL);
        ListTag chips = new ListTag();
        List<String> seen = new java.util.ArrayList<>();
        for (SpawnerBlockEntity spawner : spawners) {
            String id = spawner.saveWithoutMetadata().getCompound("SpawnData").getCompound("entity").getString("id");
            if (!id.isEmpty() && !seen.contains(id) && seen.size() < 8) {
                seen.add(id);
                chips.add(StringTag.valueOf(json(EntityType.byString(id).map(EntityType::getDescription).orElse(Component.literal(id)))));
            }
        }
        tag.put("chips", chips);
        ListTag rows = new ListTag();
        for (SpawnerStat stat : SpawnerStat.values()) {
            CompoundTag row = new CompoundTag();
            row.putString("id", stat.id());
            row.putString("name", json(Component.translatable(stat.translationKey())));
            row.putString("effect", json(Component.translatable(stat.translationKey() + ".effect")));
            row.putInt("level", this.level(stat));
            row.putString("value", json(valueOf(stat, spawners)));
            CompoundTag malus = new CompoundTag();
            malus.putString("item", stat.spec(false));
            malus.putString("name", json(stat.malusName()));
            CompoundTag bonus = new CompoundTag();
            bonus.putString("item", stat.spec(true));
            bonus.putString("name", json(stat.bonusName()));
            row.put("malus", malus);
            row.put("bonus", bonus);
            rows.add(row);
        }
        tag.put("rows", rows);
        return tag;
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
