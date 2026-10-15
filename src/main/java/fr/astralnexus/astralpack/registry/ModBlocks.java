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

    /** Sol de l'île centrale et des satellites : régolithe violet nuit. */
    public static final RegistryObject<Block> ASTRAL_REGOLITH = BLOCKS.register("astral_regolith",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(0.7F)
                    .sound(SoundType.SAND)));

    /** Roche sous la surface du Nexus. */
    public static final RegistryObject<Block> STELLAR_STONE = BLOCKS.register("stellar_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    /** Surface du récif de cristal : cristal teal lumineux. */
    public static final RegistryObject<Block> REEF_CRYSTAL = BLOCKS.register("reef_crystal",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 5)
                    .sound(SoundType.AMETHYST)));

    /** Minerai d'astralite : îles satellites et récif seulement. */
    public static final RegistryObject<Block> ASTRALITE_ORE = BLOCKS.register("astralite_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(4.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
                    .sound(SoundType.DEEPSLATE)));

    /** Minerai de nébuleuse : îles satellites seulement. */
    public static final RegistryObject<Block> NEBULA_ORE = BLOCKS.register("nebula_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
                    .sound(SoundType.DEEPSLATE)));

    public static final RegistryObject<Block> ASTRALITE_BLOCK = BLOCKS.register("astralite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 8)
                    .sound(SoundType.AMETHYST)));

    /** Roche profonde du Nexus : remplace l'ardoise des abysses sous la surface. */
    public static final RegistryObject<Block> ABYSSAL_STONE = BLOCKS.register("abyssal_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0F, 9.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.DEEPSLATE)));

    /** Mousse qui tache le sol du hub et des îles, éclaire légèrement. */
    public static final RegistryObject<Block> LUMEN_MOSS = BLOCKS.register("lumen_moss",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(0.1F)
                    .lightLevel(state -> 6)
                    .sound(SoundType.MOSS)));
}
