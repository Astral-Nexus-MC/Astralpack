package fr.astralnexus.astralpack.client.renderer;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import net.minecraft.client.model.OcelotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Rendu provisoire : modèle d'ocelot + texture fennec. À remplacer par le modèle maison. */
public class FennecRenderer extends MobRenderer<FennecEntity, OcelotModel<FennecEntity>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Astralpack.MOD_ID, "textures/entity/fennec.png");

    public FennecRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new OcelotModel<>(ctx.bakeLayer(ModelLayers.OCELOT)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(FennecEntity entity) {
        return TEXTURE;
    }
}
