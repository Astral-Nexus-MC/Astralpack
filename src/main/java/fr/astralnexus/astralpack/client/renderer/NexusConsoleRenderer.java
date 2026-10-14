package fr.astralnexus.astralpack.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.astralnexus.astralpack.block.NexusConsoleBlock;
import fr.astralnexus.astralpack.block.entity.NexusConsoleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Dessine, plantée dans le pupitre, la vraie faux astrale avec son modèle GeckoLib complet. Un modèle de bloc plafonne à
 * environ un bloc de haut : seul le rendu de l'objet lui-même permet de montrer la faux entière, avec sa lueur.
 *
 * <p>La faux est retournée : c'est sa tête, avec la lame, qui est enfoncée dans le socle, et le manche qui en sort vers le
 * haut, à travers l'ouverture sombre du plateau, en penchant légèrement.
 */
public class NexusConsoleRenderer implements BlockEntityRenderer<NexusConsoleBlockEntity> {
    /** Échelle de la faux : elle mesure environ 5 blocs à l'échelle 1, soit 2,7 blocs une fois plantée. */
    private static final float SCALE = 0.5F;
    /**
     * Hauteur, dans le bloc, du bout du manche côté lame (là où la lame s'attache). Elle est sous la collerette, qui
     * s'arrête vers 0,9 : la lame est donc enfoncée dans la colonne, et seul le bout de son arc en dépasse sur le côté.
     */
    private static final float HEAD_HEIGHT = 0.5F;
    /** Hauteur, dans le modèle de la faux, du bout du manche côté lame, en seizièmes de bloc. */
    private static final float HEAD_MODEL_Y = 46.0F;
    /** Inclinaison, en degrés, du manche : une lame plantée ne tient jamais parfaitement droite. */
    private static final float LEAN_DEGREES = 10.0F;

    public NexusConsoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NexusConsoleBlockEntity console, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack scythe = console.getScythe();
        if (scythe.isEmpty()) {
            return;
        }
        Direction facing = console.getBlockState().getValue(NexusConsoleBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        // Même rotation que le bloc : la lame part du même côté quelle que soit l'orientation du pupitre.
        poseStack.mulPose(Axis.YP.rotationDegrees(-((facing.toYRot() + 180.0F) % 360.0F)));
        poseStack.translate(0.0D, HEAD_HEIGHT, 0.0D);
        // Le manche penche à l'opposé de la lame ; la faux est retournée (tête en bas) autour de ce point.
        poseStack.mulPose(Axis.ZP.rotationDegrees(-LEAN_DEGREES));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(0.0D, -HEAD_MODEL_Y / 16.0D, 0.0D);
        Minecraft.getInstance().getItemRenderer().renderStatic(scythe, ItemDisplayContext.NONE, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, console.getLevel(),
                (int) console.getBlockPos().asLong());
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(NexusConsoleBlockEntity console) {
        return true;
    }
}
