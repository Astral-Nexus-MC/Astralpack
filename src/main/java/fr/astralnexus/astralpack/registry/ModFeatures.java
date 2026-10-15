package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.world.feature.CraterFeature;
import fr.astralnexus.astralpack.world.feature.CrystalSpireFeature;
import fr.astralnexus.astralpack.world.feature.FloatingDebrisFeature;
import fr.astralnexus.astralpack.world.feature.RuinFeature;
import fr.astralnexus.astralpack.world.feature.StalactiteFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Éléments de décor du Nexus : aiguilles de cristal, stalactites, cratères, ruines, débris flottants. */
public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Astralpack.MOD_ID);

    public static final RegistryObject<Feature<?>> CRYSTAL_SPIRE = FEATURES.register("crystal_spire", CrystalSpireFeature::new);
    public static final RegistryObject<Feature<?>> STALACTITE = FEATURES.register("stalactite", StalactiteFeature::new);
    public static final RegistryObject<Feature<?>> CRATER = FEATURES.register("crater", CraterFeature::new);
    public static final RegistryObject<Feature<?>> RUIN = FEATURES.register("ruin", RuinFeature::new);
    public static final RegistryObject<Feature<?>> FLOATING_DEBRIS = FEATURES.register("floating_debris", FloatingDebrisFeature::new);
}
