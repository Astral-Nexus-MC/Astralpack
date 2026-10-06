package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    @SubscribeEvent
    public static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FENNEC.get(), FennecEntity.createAttributes().build());
    }
}
