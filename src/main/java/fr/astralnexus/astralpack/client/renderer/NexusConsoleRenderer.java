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
 * <p>Elle est plantée par la pointe de sa lame : on la fait pivoter autour de cette pointe jusqu'à ce qu'elle soit le point
 * le plus bas de la faux (à environ 105°, vérifié sur la géométrie), la pointe est plantée dans l'encoche sombre du plateau, et
 * le manche part en diagonale vers le haut, comme une lame plantée en terre.
 */
public class NexusConsoleRenderer implements BlockEntityRenderer<NexusConsoleBlockEntity> {
    /** Échelle de la faux : elle mesure environ 5 blocs à l'échelle 1. */
    private static final float SCALE = 0.6F;
    /**
     * Position de l'encoche du plateau, dans le bloc, mesurée sur le modèle (Blockbench) : le plateau est incliné de
     * 22,5°, donc l'encoche est à 0,89 de haut et décalée de 0,06 bloc vers l'avant du pupitre. La pointe de la lame y est
     * plantée, enfoncée de huit centièmes de bloc (l'encoche affleure à 0,9).
     */
    private static final float TIP_HEIGHT = 0.82F;
    private static final float TIP_FRONT_OFFSET = -0.06F;
    /**
     * Position de la pointe de la lame dans le fichier du modèle de la faux (x, y), en seizièmes de bloc. GeckoLib inverse
     * l'axe horizontal du modèle à l'affichage : dans le jeu, la pointe est à x = -TIP_MODEL_X.
     */
    private static final float TIP_MODEL_X = 34.6F;
    private static final float TIP_MODEL_Y = 31.77F;
    /**
     * Rotation de la faux autour de sa pointe : 105° vue de face dans le sens horaire (le modèle étant inversé, le sens
     * antihoraire de l'espace de rendu). Entre 65° et 125° la pointe est le point le plus bas ; à 105° la lame monte de l'encoche
     * en s'incurvant et le manche part en diagonale vers le haut.
     */
    private static final float ROLL_DEGREES = 105.0F;

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
        poseStack.translate(0.0D, TIP_HEIGHT, TIP_FRONT_OFFSET);
        // Retournée de 180° autour de la verticale de la pointe : la tête de la faux est du côté opposé au manche.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(ROLL_DEGREES));
        poseStack.scale(SCALE, SCALE, SCALE);
        poseStack.translate(TIP_MODEL_X / 16.0D, -TIP_MODEL_Y / 16.0D, 0.0D);
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
