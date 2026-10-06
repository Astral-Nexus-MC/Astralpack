package fr.astralnexus.astralpack.client.model;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FennecModel extends GeoModel<FennecEntity> {
    @Override
    public ResourceLocation getModelResource(FennecEntity entity) {
        return new ResourceLocation(Astralpack.MOD_ID, "geo/fennec.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FennecEntity entity) {
        return new ResourceLocation(Astralpack.MOD_ID, "textures/entity/fennec.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FennecEntity entity) {
        return new ResourceLocation(Astralpack.MOD_ID, "animations/fennec.animation.json");
    }
}
