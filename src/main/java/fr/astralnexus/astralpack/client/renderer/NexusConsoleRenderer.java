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
 * Dessine, planté dans le pupitre, la vrai Death Scythe avec son modèle GeckoLib complet. Le modèle de bloc plafonne à
 * environ un bloc de haut : seul le rendu de l'objet lui-même permet de montrer la faux entière, avec sa lueur.
 */
public class NexusConsoleRenderer implements BlockEntityRenderer<NexusConsoleBlockEntity> {
    /** Échelle de la faux : elle mesure environ 5 blocs à l'échelle 1, soit 2,7 blocs une fois plantée. */
    private static final float SCALE = 0.55F;
    /**
     * Hauteur du bas du manche. La collerette du pupitre s'arrête vers 0,9 : le manche y est enfoncé de près d'un demi-bloc,
     * et ressort de l'ouverture sombre du modèle, pour que la faux soit plantée et non posée dessus.
     */
    private static final float HANDLE_BOTTOM = 0.45F;
    /** Inclinaison, en degrés, du manche autour de son pied : une lame plantée ne tient jamais parfaitement droite. */
    private static final float LEAN_DEGREES = 6.0F;
    /** Distance, en unités du modèle divisées par 16, entre l'origine du modèle et le bas du manche. */
    private static final float HANDLE_OFFSET = 21.6F / 16.0F;

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
        poseStack.translate(0.0D, HANDLE_BOTTOM, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(LEAN_DEGREES));
        poseStack.translate(0.0D, HANDLE_OFFSET * SCALE, 0.0D);
        poseStack.scale(SCALE, SCALE, SCALE);
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
