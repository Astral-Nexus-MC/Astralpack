package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Astralpack.MOD_ID);

    public static final RegistryObject<EntityType<FennecEntity>> FENNEC =
            ENTITY_TYPES.register("fennec", () -> EntityType.Builder
                    .of(FennecEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.7F)
                    .clientTrackingRange(8)
                    .build("fennec"));
}
