package fr.astralnexus.astralpack.client.pose;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * Pose du joueur pour les armes à deux mains : la main principale tient la hampe devant le corps, la main d'appui est
 * placée par le calcul sur la même hampe, plus bas. Repère du modèle : x vers la gauche du joueur, y vers le bas, z vers
 * l'arrière (l'avant du joueur est en z négatif). Un bras pend le long de +y et tourne selon X puis Y.
 */
public final class TwoHandedPose {
    /** Distance de l'épaule au centre de la main, en pixels de modèle. */
    private static final float REACH = 11.0F;
    /** Distance entre les deux mains le long de la hampe. */
    private static final float GRIP_GAP = 5.0F;
    /** Inclinaison du bras principal : légèrement sous l'horizontale, la hampe penche vers l'avant. */
    private static final float MAIN_X = -1.22F;
    private static final float MAIN_Y = -0.5F;

    public static final HumanoidModel.ArmPose POSE = HumanoidModel.ArmPose.create("ASTRALPACK_TWO_HANDED", true,
            (model, entity, arm) -> {
                boolean mainRight = arm == HumanoidArm.RIGHT;
                ModelPart main = mainRight ? model.rightArm : model.leftArm;
                ModelPart support = mainRight ? model.leftArm : model.rightArm;
                float side = mainRight ? 1.0F : -1.0F;

                float a = MAIN_X + model.head.xRot;
                float b = MAIN_Y * side + model.head.yRot;
                main.xRot = a;
                main.yRot = b;
                main.zRot = 0.0F;

                // Position de la main principale et direction de la hampe (vers le haut de l'arme).
                float sinA = Mth.sin(a);
                float cosA = Mth.cos(a);
                float sinB = Mth.sin(b);
                float cosB = Mth.cos(b);
                float shoulderX = -5.0F * side;
                float handX = shoulderX + REACH * sinA * sinB;
                float handY = 2.0F + REACH * cosA;
                float handZ = REACH * sinA * cosB;
                float upX = -cosA * sinB;
                float upY = sinA;
                float upZ = -cosA * cosB;

                // La main d'appui vise un point de la hampe situé plus bas.
                float targetX = handX - GRIP_GAP * upX;
                float targetY = handY - GRIP_GAP * upY;
                float targetZ = handZ - GRIP_GAP * upZ;
                float dx = targetX - (-shoulderX);
                float dy = targetY - 2.0F;
                float dz = targetZ;
                float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
                dx /= length;
                dy /= length;
                dz /= length;
                float horizontal = Mth.sqrt(Math.max(1.0E-4F, 1.0F - dy * dy));
                support.xRot = -(float) Math.acos(Mth.clamp(dy, -1.0F, 1.0F));
                support.yRot = (float) Math.asin(Mth.clamp(dx / -horizontal, -1.0F, 1.0F));
                support.zRot = 0.0F;

                // Frappe à deux mains : la main d'appui reprend l'arc de balayage vanilla de la main principale.
                float t = model.attackTime;
                if (t > 0.0F) {
                    float f = 1.0F - t;
                    f = 1.0F - f * f * f * f;
                    float arc = Mth.sin(f * (float) Math.PI);
                    float tilt = Mth.sin(t * (float) Math.PI) * -(model.head.xRot - 0.7F) * 0.75F;
                    support.xRot -= arc * 1.2F + tilt;
                }
            });

    /** Force le chargement de la classe, donc la création de la pose. */
    public static void register() {
    }

    private TwoHandedPose() {
    }
}
