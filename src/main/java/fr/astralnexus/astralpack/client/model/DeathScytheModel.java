package fr.astralnexus.astralpack.client.model;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.item.DeathScytheItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DeathScytheModel extends GeoModel<DeathScytheItem> {
    private static final ResourceLocation MODEL = new ResourceLocation(Astralpack.MOD_ID, "geo/death_scythe.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Astralpack.MOD_ID, "textures/item/death_scythe.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Astralpack.MOD_ID, "animations/death_scythe.animation.json");

    @Override
    public ResourceLocation getModelResource(DeathScytheItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DeathScytheItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DeathScytheItem animatable) {
        return ANIMATION;
    }
}
