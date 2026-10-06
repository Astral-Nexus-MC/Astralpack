package fr.astralnexus.astralpack.client.renderer;

import fr.astralnexus.astralpack.client.model.FennecModel;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FennecRenderer extends GeoEntityRenderer<FennecEntity> {
    private static final float BABY_SCALE = 0.5F;

    public FennecRenderer(EntityRendererProvider.Context context) {
        super(context, new FennecModel());
        this.shadowRadius = 0.3F;
        this.addRenderLayer(new FennecMouthItemLayer(this));
    }

    @Override
    public void render(FennecEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        if (entity.isBaby()) {
            poseStack.scale(BABY_SCALE, BABY_SCALE, BABY_SCALE);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
