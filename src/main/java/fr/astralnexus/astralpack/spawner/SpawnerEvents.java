package fr.astralnexus.astralpack.spawner;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.block.entity.SpawnerConsoleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.chunk.LevelChunk;
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
 *   expérience multipliés). Chaque spawner a sa population max ; les spawners à fusion du même monstre
 *   dont les zones se touchent partagent un groupe dont la limite est la somme de leurs populations max.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID)
public final class SpawnerEvents {
    /** Donnée persistante d'un spawner posé avec la clé à molette : ses monstres fusionnent. */
    public static final String STACKING = "AstralStacking";
    private static final String HEALTH_MARK = "AstralHealthLevel";
    private static final String STACK = "AstralStack";
    private static final String FRESH = "AstralFresh";
    /** Probabilité de rafale par cran de bonus de cadence, et taille de la rafale en multiples de la quantité. */
    private static final float BURST_CHANCE_PER_LEVEL = 0.10F;
    private static final int BURST_FACTOR = 5;
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

    private static AABB zone(SpawnerBlockEntity spawner) {
        return new AABB(spawner.getBlockPos()).inflate(Math.max(1, spawner.saveWithoutMetadata().getShort("SpawnRange")));
    }

    private static String entityId(SpawnerBlockEntity spawner) {
        return spawner.saveWithoutMetadata().getCompound("SpawnData").getCompound("entity").getString("id");
    }

    /**
     * Spawners à fusion du même monstre dont la zone touche celle de ce spawner (lui compris). Chacun apporte sa
     * propre population max : le groupe commun peut atteindre leur somme.
     */
    private static List<SpawnerBlockEntity> sameKind(ServerLevel level, SpawnerBlockEntity spawner) {
        AABB own = zone(spawner);
        String id = entityId(spawner);
        List<SpawnerBlockEntity> cluster = new ArrayList<>();
        cluster.add(spawner);
        // Zone de recherche : assez large pour toucher les spawners voisins (leur rayon est petit, 16 blocs au plus).
        AABB search = own.inflate(16.0D);
        for (int cx = (int) Math.floor(search.minX) >> 4; cx <= (int) Math.floor(search.maxX) >> 4; cx++) {
            for (int cz = (int) Math.floor(search.minZ) >> 4; cz <= (int) Math.floor(search.maxZ) >> 4; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (entity != spawner && entity instanceof SpawnerBlockEntity other && !other.isRemoved()
                            && other.getPersistentData().getBoolean(STACKING) && zone(other).intersects(own)
                            && entityId(other).equals(id)) {
                        cluster.add(other);
                    }
                }
            }
        }
        return cluster;
    }

    private static void mergeNear(ServerLevel level, SpawnerBlockEntity spawner) {
        CompoundTag data = spawner.saveWithoutMetadata();
        List<SpawnerBlockEntity> cluster = sameKind(level, spawner);
        int limit = 0;
        AABB box = zone(spawner);
        for (SpawnerBlockEntity member : cluster) {
            limit += Math.max(1, member.saveWithoutMetadata().getShort("MaxNearbyEntities"));
            box = box.minmax(zone(member));
        }
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box, Entity::isAlive);
        Map<EntityType<?>, Mob> groups = new HashMap<>();
        Map<EntityType<?>, Integer> totals = new HashMap<>();
        List<Mob> fresh = new ArrayList<>();
        for (Mob mob : mobs) {
            if (mob.getPersistentData().getBoolean(FRESH)) {
                fresh.add(mob);
            } else if (mob.getPersistentData().contains(STACK)) {
                totals.merge(mob.getType(), mob.getPersistentData().getInt(STACK), Integer::sum);
                Mob group = groups.putIfAbsent(mob.getType(), mob);
                if (group != null) {
                    // Deux groupes du même monstre dans la zone commune (ex. un par spawner) : le second rejoint le premier.
                    setStack(group, group.getPersistentData().getInt(STACK) + mob.getPersistentData().getInt(STACK));
                    mob.discard();
                }
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
        if (!fresh.isEmpty()) {
            burst(level, spawner, fresh.get(0).getType(), groups, totals, limit, data.getShort("SpawnCount"));
        }
    }

    /**
     * Rafale de la cadence : à chaque cycle d'apparition d'un spawner à fusion, une probabilité (10 % par cran de
     * bonus de cadence) d'ajouter d'un coup tout un groupe de monstres (cinq fois la quantité du spawner) au groupe du
     * spawner, dans la limite de la population max.
     */
    private static void burst(ServerLevel level, SpawnerBlockEntity spawner, EntityType<?> type, Map<EntityType<?>, Mob> groups,
                              Map<EntityType<?>, Integer> totals, int limit, int count) {
        SpawnerConsoleBlockEntity console = SpawnerControl.findConsole(level, spawner.getBlockPos());
        int cadence = console == null ? 0 : console.level(SpawnerStat.CADENCE);
        Mob group = groups.get(type);
        if (cadence <= 0 || group == null || level.random.nextFloat() >= BURST_CHANCE_PER_LEVEL * cadence) {
            return;
        }
        int room = limit - totals.getOrDefault(type, 0);
        int extra = Math.min(room, BURST_FACTOR * Math.max(1, count));
        if (extra <= 0) {
            return;
        }
        setStack(group, group.getPersistentData().getInt(STACK) + extra);
        totals.put(type, totals.getOrDefault(type, 0) + extra);
        level.sendParticles(ParticleTypes.POOF, spawner.getBlockPos().getX() + 0.5D, spawner.getBlockPos().getY() + 1.0D,
                spawner.getBlockPos().getZ() + 0.5D, 25, 0.4D, 0.4D, 0.4D, 0.05D);
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
