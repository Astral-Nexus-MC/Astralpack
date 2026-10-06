package fr.astralnexus.astralpack.registry;

import fr.astralnexus.astralpack.Astralpack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Astralpack.MOD_ID);

    public static final RegistryObject<SoundEvent> FENNEC_AMBIENT = register("entity.fennec.ambient");
    public static final RegistryObject<SoundEvent> FENNEC_HURT = register("entity.fennec.hurt");
    public static final RegistryObject<SoundEvent> FENNEC_DEATH = register("entity.fennec.death");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Astralpack.MOD_ID, name)));
    }
}
