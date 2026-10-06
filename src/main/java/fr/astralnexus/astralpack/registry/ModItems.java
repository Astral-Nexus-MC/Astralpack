package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.item.FennecArmorItem;
import fr.astralnexus.astralpack.item.FennecArmorSlot;
import fr.astralnexus.astralpack.item.FennecArmorTier;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Astralpack.MOD_ID);

    public static final RegistryObject<Item> FENNEC_SPAWN_EGG =
            ITEMS.register("fennec_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.FENNEC, 0xE8C58A, 0x7A5A3A, new Item.Properties()));

    static {
        for (FennecArmorTier tier : FennecArmorTier.values()) {
            for (FennecArmorSlot slot : FennecArmorSlot.values()) {
                String id = "fennec_" + tier.getName() + "_" + slot.getItemSuffix();
                boolean fireResistant = tier == FennecArmorTier.NETHERITE;
                ITEMS.register(id, () -> {
                    Item.Properties properties = new Item.Properties();
                    return new FennecArmorItem(tier, slot, fireResistant ? properties.fireResistant() : properties);
                });
            }
        }
    }
}
