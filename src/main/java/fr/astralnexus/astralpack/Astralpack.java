package fr.astralnexus.astralpack;

import fr.astralnexus.astralpack.registry.ModCreativeTabs;
import fr.astralnexus.astralpack.registry.ModEntities;
import fr.astralnexus.astralpack.registry.ModItems;
import fr.astralnexus.astralpack.registry.ModSounds;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

@Mod(Astralpack.MOD_ID)
public class Astralpack {
    public static final String MOD_ID = "astralpack";

    public Astralpack() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
    }
}
