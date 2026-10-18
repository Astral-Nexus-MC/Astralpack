package fr.astralnexus.astralpack.spawner;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.block.entity.SpawnerConsoleBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Vie des monstres issus d'un spawner contrôlé par une console. L'apparition est repérée par le spawner qui la
 * provoque (étape 1 : on marque le monstre) puis la vie est réglée quand il entre dans le monde (étape 2), après que le
 * jeu a fini d'initialiser le monstre (un slime, par exemple, règle lui-même sa vie à sa taille).
 */
@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID)
public final class SpawnerEvents {
    private static final String MARK = "AstralHealthLevel";

    private SpawnerEvents() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() != MobSpawnType.SPAWNER) {
            return;
        }
        BaseSpawner spawner = event.getSpawner();
        BlockEntity entity = spawner == null ? null : spawner.getSpawnerBlockEntity();
        if (!(entity instanceof SpawnerBlockEntity) || entity.getLevel() == null) {
            return;
        }
        SpawnerConsoleBlockEntity console = SpawnerControl.findConsole(entity.getLevel(), entity.getBlockPos());
        if (console == null) {
            return;
        }
        int level = console.level(SpawnerStat.HEALTH);
        if (level != 0) {
            event.getEntity().getPersistentData().putInt(MARK, level);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel().isClientSide || !(entity instanceof Mob mob) || !mob.getPersistentData().contains(MARK)) {
            return;
        }
        int level = mob.getPersistentData().getInt(MARK);
        mob.getPersistentData().remove(MARK);
        AttributeInstance attribute = mob.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) {
            return;
        }
        double max = SpawnerStat.health(attribute.getBaseValue(), level);
        attribute.setBaseValue(max);
        mob.setHealth((float) mob.getMaxHealth());
    }
}
