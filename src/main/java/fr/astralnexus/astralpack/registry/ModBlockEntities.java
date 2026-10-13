package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.block.entity.NexusConsoleBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Astralpack.MOD_ID);

    public static final RegistryObject<BlockEntityType<NexusConsoleBlockEntity>> NEXUS_CONSOLE =
            BLOCK_ENTITIES.register("nexus_console", () -> BlockEntityType.Builder
                    .of(NexusConsoleBlockEntity::new, ModBlocks.NEXUS_CONSOLE.get()).build(null));
}
