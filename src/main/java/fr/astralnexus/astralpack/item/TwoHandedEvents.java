package fr.astralnexus.astralpack.item;

import fr.astralnexus.astralpack.Astralpack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Applique le malus des armes à deux mains : main secondaire occupée, les dégâts de l'arme sont réduits. */
@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID)
public class TwoHandedEvents {
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
