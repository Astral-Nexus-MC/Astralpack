package fr.astralnexus.astralpack.item;

import fr.astralnexus.astralpack.Astralpack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Applique le malus des armes à deux mains : main secondaire occupée, les dégâts de l'arme sont réduits. */
@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID)
public class TwoHandedEvents {
    /**
     * Une arme à deux mains libère la main secondaire : l'objet qui s'y trouve part dans la hotbar, sinon dans
     * l'inventaire, sinon au sol.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide) {
            return;
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty() || !(player.getMainHandItem().getItem() instanceof TwoHanded)) {
            return;
        }
        ItemStack moved = offhand.copy();
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        // Inventory.add parcourt d'abord la hotbar (emplacements 0 à 8), puis le reste de l'inventaire.
        player.getInventory().add(moved);
        if (!moved.isEmpty()) {
            player.drop(moved, false);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || event.getSource().getDirectEntity() != player
                || player.level().isClientSide) {
            return;
        }
        if (player.getMainHandItem().getItem() instanceof TwoHanded weapon && !player.getOffhandItem().isEmpty()) {
            event.setAmount(event.getAmount() * weapon.offhandDamageFactor());
        }
    }
}
