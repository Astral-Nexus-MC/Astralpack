package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.block.FennecBurrowBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Astralpack.MOD_ID);

    public static final RegistryObject<Block> FENNEC_BURROW = BLOCKS.register("fennec_burrow",
            () -> new FennecBurrowBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.6F)
                    .sound(SoundType.SAND)
                    .noOcclusion()));
}
