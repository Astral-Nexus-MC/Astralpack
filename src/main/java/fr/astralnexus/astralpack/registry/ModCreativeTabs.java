package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Astralpack.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Astralpack.MOD_ID + ".main"))
                    .icon(() -> ModItems.FENNEC_SPAWN_EGG.get().getDefaultInstance())
                    .displayItems((params, output) -> ModItems.ITEMS.getEntries()
                            .forEach(item -> output.accept(item.get())))
                    .build());
}
