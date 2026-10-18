package fr.astralnexus.astralpack.spawner;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.block.entity.SpawnerConsoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Apparitions issues d'un spawner :
 * <ul>
 *   <li>vie des monstres réglée par la console (marqué à l'initialisation, appliqué à l'entrée dans le monde, après
 *   que le jeu a fini d'initialiser le monstre : un slime, par exemple, règle lui-même sa vie) ;</li>
 *   <li>fusion des monstres des spawners posés avec la clé à molette : au lieu d'une foule d'entités, un seul monstre
 *   porte un nombre ({@code AstralStack}) affiché devant son nom ; il emporte tout le groupe en mourant (butin et
 *   expérience multipliés). La population max du spawner limite le total du groupe.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID)
public final class SpawnerEvents {
    /** Donnée persistante d'un spawner posé avec la clé à molette : ses monstres fusionnent. */
    public static final String STACKING = "AstralStacking";
    private static final String HEALTH_MARK = "AstralHealthLevel";
    private static final String STACK = "AstralStack";
    private static final String FRESH = "AstralFresh";
    private static final Map<String, Set<BlockPos>> PENDING = new HashMap<>();

    private SpawnerEvents() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() != MobSpawnType.SPAWNER) {
            return;
        }
        BaseSpawner baseSpawner = event.getSpawner();
        BlockEntity entity = baseSpawner == null ? null : baseSpawner.getSpawnerBlockEntity();
        if (!(entity instanceof SpawnerBlockEntity spawner) || spawner.getLevel() == null) {
            return;
        }
        Mob mob = event.getEntity();
        if (spawner.getPersistentData().getBoolean(STACKING)) {
            mob.getPersistentData().putBoolean(FRESH, true);
            PENDING.computeIfAbsent(spawner.getLevel().dimension().location().toString(), key -> new HashSet<>())
                    .add(spawner.getBlockPos().immutable());
        }
        SpawnerConsoleBlockEntity console = SpawnerControl.findConsole(spawner.getLevel(), spawner.getBlockPos());
        int level = console == null ? 0 : console.level(SpawnerStat.HEALTH);
        if (level != 0) {
            mob.getPersistentData().putInt(HEALTH_MARK, level);
        }
    }

    /**
     * Les monstres qui viennent d'apparaître d'un spawner à fusion sont regroupés à la fin du tick : chacun est
     * absorbé par le groupe voisin du même type (le plus ancien devient le groupe s'il n'y en a pas), tant que le
     * total reste sous la population max du spawner ; le surplus disparaît. (Le jeu ne permet pas d'annuler une
     * apparition de spawner, d'où ce passage après coup.)
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || PENDING.isEmpty()) {
            return;
        }
        Set<BlockPos> positions = PENDING.remove(level.dimension().location().toString());
        if (positions == null) {
            return;
        }
        for (BlockPos pos : positions) {
            if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
                mergeNear(level, spawner);
            }
        }
    }

    private static void mergeNear(ServerLevel level, SpawnerBlockEntity spawner) {
        CompoundTag data = spawner.saveWithoutMetadata();
        int limit = Math.max(1, data.getShort("MaxNearbyEntities"));
        int range = Math.max(1, data.getShort("SpawnRange"));
        AABB box = new AABB(spawner.getBlockPos()).inflate(range);
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box, Entity::isAlive);
        Map<EntityType<?>, Mob> groups = new HashMap<>();
        Map<EntityType<?>, Integer> totals = new HashMap<>();
        List<Mob> fresh = new ArrayList<>();
        for (Mob mob : mobs) {
            if (mob.getPersistentData().getBoolean(FRESH)) {
                fresh.add(mob);
            } else if (mob.getPersistentData().contains(STACK)) {
                groups.putIfAbsent(mob.getType(), mob);
                totals.merge(mob.getType(), mob.getPersistentData().getInt(STACK), Integer::sum);
            }
        }
        for (Mob mob : fresh) {
            mob.getPersistentData().remove(FRESH);
            Mob group = groups.get(mob.getType());
            if (group == null) {
                mob.getPersistentData().putInt(STACK, 1);
                groups.put(mob.getType(), mob);
                totals.put(mob.getType(), 1);
            } else {
                int total = totals.getOrDefault(mob.getType(), 0);
                if (total < limit) {
                    setStack(group, group.getPersistentData().getInt(STACK) + 1);
                    totals.put(mob.getType(), total + 1);
                }
                mob.discard();
            }
        }
    }

    private static void setStack(Mob mob, int size) {
        mob.getPersistentData().putInt(STACK, size);
        if (size > 1) {
            mob.setCustomName(Component.literal("x" + size + " ").append(mob.getType().getDescription()));
            mob.setCustomNameVisible(true);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide || !(entity instanceof Mob mob)
                || !mob.getPersistentData().contains(HEALTH_MARK)) {
            return;
        }
        int level = mob.getPersistentData().getInt(HEALTH_MARK);
        mob.getPersistentData().remove(HEALTH_MARK);
        AttributeInstance attribute = mob.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }
        double health = SpawnerStat.health(attribute.getBaseValue(), level);
        attribute.setBaseValue(Math.max(1.0D, health));
        mob.setHealth((float) Math.min(health, mob.getMaxHealth()));
    }

    /** Un groupe qui meurt rapporte autant d'expérience que tous ses monstres. */
    @SubscribeEvent
    public static void onExperience(LivingExperienceDropEvent event) {
        int size = groupSize(event.getEntity());
        if (size > 1) {
            event.setDroppedExperience(event.getDroppedExperience() * size);
        }
    }

    /** Un groupe qui meurt laisse le butin de tous ses monstres, regroupé en piles complètes. */
    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        int size = groupSize(event.getEntity());
        if (size <= 1) {
            return;
        }
        List<ItemEntity> extra = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            long total = (long) stack.getCount() * size;
            int max = stack.getMaxStackSize();
            int first = (int) Math.min(total, max);
            stack.setCount(first);
            drop.setItem(stack);
            total -= first;
            while (total > 0) {
                int part = (int) Math.min(total, max);
                ItemStack copy = stack.copy();
                copy.setCount(part);
                extra.add(new ItemEntity(drop.level(), drop.getX(), drop.getY(), drop.getZ(), copy));
                total -= part;
            }
        }
        event.getDrops().addAll(extra);
    }

    private static int groupSize(Entity entity) {
        return entity instanceof Mob mob ? mob.getPersistentData().getInt(STACK) : 0;
    }
}
