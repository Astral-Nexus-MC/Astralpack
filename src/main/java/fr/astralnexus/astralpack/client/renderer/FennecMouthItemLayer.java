package fr.astralnexus.astralpack.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

/** Affiche l'objet tenu dans la gueule, accroché à l'os `mouth_item` du modèle. */
public class FennecMouthItemLayer extends BlockAndItemGeoLayer<FennecEntity> {
    private static final String MOUTH_BONE = "mouth_item";
    private static final float ITEM_SCALE = 0.4F;
    /** Rotation dans le plan du sprite : couche à l'horizontale les objets dessinés en diagonale (outils, bâtons). */
    private static final float ITEM_TILT = -45.0F;

    public FennecMouthItemLayer(GeoRenderer<FennecEntity> renderer) {
        super(renderer,
                (bone, fennec) -> MOUTH_BONE.equals(bone.getName()) && !fennec.getMainHandItem().isEmpty()
                        ? fennec.getMainHandItem() : null,
                (bone, fennec) -> null);
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, FennecEntity animatable) {
        return ItemDisplayContext.GROUND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, FennecEntity animatable,
                                      MultiBufferSource bufferSource, float partialTick, int packedLight,
                                      int packedOverlay) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(ITEM_TILT));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight,
                packedOverlay);
        poseStack.popPose();
    }
}
