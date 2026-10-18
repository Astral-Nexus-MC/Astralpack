package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.item.DeathScytheItem;
import fr.astralnexus.astralpack.item.FennecArmorItem;
import fr.astralnexus.astralpack.item.FennecArmorSlot;
import fr.astralnexus.astralpack.item.FennecArmorTier;
import fr.astralnexus.astralpack.item.MovedSpawnerItem;
import fr.astralnexus.astralpack.item.SpawnerWrenchItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    public static final RegistryObject<Item> FENNEC_BURROW =
            ITEMS.register("fennec_burrow", () -> new BlockItem(ModBlocks.FENNEC_BURROW.get(), new Item.Properties()));

    public static final RegistryObject<Item> ASTRAL_RING_BLOCK = ITEMS.register("astral_ring_block",
            () -> new BlockItem(ModBlocks.ASTRAL_RING_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> NEXUS_CONSOLE = ITEMS.register("nexus_console",
            () -> new BlockItem(ModBlocks.NEXUS_CONSOLE.get(), new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> ASTRAL_REGOLITH = ITEMS.register("astral_regolith",
            () -> new BlockItem(ModBlocks.ASTRAL_REGOLITH.get(), new Item.Properties()));

    public static final RegistryObject<Item> STELLAR_STONE = ITEMS.register("stellar_stone",
            () -> new BlockItem(ModBlocks.STELLAR_STONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> REEF_CRYSTAL = ITEMS.register("reef_crystal",
            () -> new BlockItem(ModBlocks.REEF_CRYSTAL.get(), new Item.Properties()));

    public static final RegistryObject<Item> ASTRALITE_ORE = ITEMS.register("astralite_ore",
            () -> new BlockItem(ModBlocks.ASTRALITE_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> NEBULA_ORE = ITEMS.register("nebula_ore",
            () -> new BlockItem(ModBlocks.NEBULA_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> ASTRALITE_BLOCK = ITEMS.register("astralite_block",
            () -> new BlockItem(ModBlocks.ASTRALITE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> ASTRALITE = ITEMS.register("astralite", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> NEBULA_SHARD = ITEMS.register("nebula_shard", () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ABYSSAL_STONE = ITEMS.register("abyssal_stone",
            () -> new BlockItem(ModBlocks.ABYSSAL_STONE.get(), new Item.Properties()));

    public static final RegistryObject<Item> LUMEN_MOSS = ITEMS.register("lumen_moss",
            () -> new BlockItem(ModBlocks.LUMEN_MOSS.get(), new Item.Properties()));

    /** Clé à molette : décroche un spawner à monstres sans le casser. */
    public static final RegistryObject<Item> SPAWNER_WRENCH = ITEMS.register("spawner_wrench",
            () -> new SpawnerWrenchItem(new Item.Properties().stacksTo(1).durability(64)));

    /** Spawner décroché : garde son monstre et ses réglages jusqu'à sa pose. */
    public static final RegistryObject<Item> MOVED_SPAWNER = ITEMS.register("moved_spawner",
            () -> new MovedSpawnerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DEATH_SCYTHE =
            ITEMS.register("death_scythe",
                    () -> new DeathScytheItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

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
