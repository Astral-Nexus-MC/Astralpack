package fr.astralnexus.astralpack.client.pose;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

/** Pose du joueur pour les armes à deux mains : les deux bras tiennent la hampe devant le corps. */
public final class TwoHandedPose {
    public static final HumanoidModel.ArmPose POSE = HumanoidModel.ArmPose.create("ASTRALPACK_TWO_HANDED", true,
            (model, entity, arm) -> {
                boolean mainRight = arm == HumanoidArm.RIGHT;
                ModelPart main = mainRight ? model.rightArm : model.leftArm;
                ModelPart support = mainRight ? model.leftArm : model.rightArm;
                float side = mainRight ? 1.0F : -1.0F;
                // La main dominante tient la hampe haut, la main d'appui plus bas et plus au centre.
                main.xRot = -1.15F + model.head.xRot;
                main.yRot = -0.35F * side + model.head.yRot;
                main.zRot = 0.0F;
                support.xRot = -1.35F + model.head.xRot;
                support.yRot = 0.7F * side + model.head.yRot;
                support.zRot = 0.0F;
                // Frappe à deux mains : la main d'appui reprend l'arc de balayage vanilla de la main principale.
                float t = model.attackTime;
                if (t > 0.0F) {
                    float f = 1.0F - t;
                    f = 1.0F - f * f * f * f;
                    float arc = (float) Math.sin(f * Math.PI);
                    float tilt = (float) Math.sin(t * Math.PI) * -(model.head.xRot - 0.7F) * 0.75F;
                    support.xRot -= arc * 1.2F + tilt;
                }
            });

    /** Force le chargement de la classe, donc la création de la pose. */
    public static void register() {
    }

    private TwoHandedPose() {
    }
}
