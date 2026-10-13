package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.block.FennecBurrowBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import fr.astralnexus.astralpack.block.NexusConsoleBlock;
import fr.astralnexus.astralpack.block.NexusPortalBlock;
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

    /** Segment de l'anneau du portail (13 x 13 blocs). */
    public static final RegistryObject<Block> ASTRAL_RING_BLOCK = BLOCKS.register("astral_ring_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(5.0F, 12.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
                    .sound(SoundType.DEEPSLATE_TILES)));

    /** Pupitre où l'on plante la Death Scythe pour ouvrir le portail. */
    public static final RegistryObject<Block> NEXUS_CONSOLE = BLOCKS.register("nexus_console",
            () -> new NexusConsoleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(4.0F, 12.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(NexusConsoleBlock.PLANTED) ? 10 : 3)
                    .sound(SoundType.DEEPSLATE_TILES)
                    .noOcclusion()));

    /** Surface du portail : sans objet, indestructible, remplit l'anneau tant qu'il est ouvert. */
    public static final RegistryObject<Block> NEXUS_PORTAL = BLOCKS.register("nexus_portal",
            () -> new NexusPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .noCollission()
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .lightLevel(state -> 11)
                    .noOcclusion()
                    .sound(SoundType.GLASS)));
}
