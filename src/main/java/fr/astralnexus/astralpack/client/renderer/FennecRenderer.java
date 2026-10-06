package fr.astralnexus.astralpack.client.renderer;

import fr.astralnexus.astralpack.client.model.FennecModel;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FennecRenderer extends GeoEntityRenderer<FennecEntity> {
    public FennecRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FennecModel());
        this.shadowRadius = 0.4F;
    }
}
