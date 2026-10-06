package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Astralpack.MOD_ID);

    public static final RegistryObject<Item> FENNEC_SPAWN_EGG =
            ITEMS.register("fennec_spawn_egg",
                    () -> new SpawnEggItem(ModEntities.FENNEC.get(), 0xE8C58A, 0x7A5A3A, new Item.Properties()));
}
