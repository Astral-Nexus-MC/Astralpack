package fr.astralnexus.astralpack.client.model;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.entity.custom.FennecEntity;
import fr.astralnexus.astralpack.item.FennecArmorSlot;
import fr.astralnexus.astralpack.item.FennecArmorTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

import java.util.EnumMap;
import java.util.Map;

public class FennecModel extends GeoModel<FennecEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(Astralpack.MOD_ID, "geo/fennec.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Astralpack.MOD_ID, "textures/entity/fennec.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Astralpack.MOD_ID, "animations/fennec.animation.json");

    private static final float JAW_OPEN = 0.9F;

    private static final Map<FennecArmorSlot, String[]> PIECES = new EnumMap<>(FennecArmorSlot.class);

    static {
        PIECES.put(FennecArmorSlot.HEAD, new String[]{"helmet"});
        PIECES.put(FennecArmorSlot.BODY, new String[]{"chest", "tail"});
        PIECES.put(FennecArmorSlot.FEET, new String[]{"leg_front_left", "leg_front_right", "leg_back_left", "leg_back_right"});

    }

    @Override
    public ResourceLocation getModelResource(FennecEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FennecEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FennecEntity animatable) {
        return ANIMATION;
    }

    @Override
    public void setCustomAnimations(FennecEntity animatable, long instanceId, AnimationState<FennecEntity> animationState) {
        this.trackHead(animatable, animationState);
        this.openJaw(animatable);
        for (FennecArmorSlot slot : FennecArmorSlot.values()) {
            FennecArmorTier worn = animatable.getArmorTier(slot);
            for (FennecArmorTier tier : FennecArmorTier.values()) {
                boolean hidden = worn != tier;
                for (String piece : PIECES.get(slot)) {
                    CoreGeoBone bone = this.getAnimationProcessor().getBone("armor_" + piece + "_" + tier.getName());
                    if (bone != null) {
                        bone.setHidden(hidden);
                        bone.setChildrenHidden(hidden);
                    }
                }
            }
        }
    }

    /**
     * Gueule ouverte quand le Fennec tient un objet entre ses dents. Angle absolu depuis la pose de repos : la mâchoire
     * n'a pas de keyframes au repos, un ajout relatif s'accumulerait à chaque image et la ferait sauter.
     */
    private void openJaw(FennecEntity animatable) {
        if (animatable.getMainHandItem().isEmpty() || animatable.isDozing()) {
            return;
        }
        CoreGeoBone jaw = this.getAnimationProcessor().getBone("jaw");
        if (jaw != null) {
            jaw.setRotX(jaw.getInitialSnapshot().getRotX() + JAW_OPEN);
        }
    }

    private void trackHead(FennecEntity animatable, AnimationState<FennecEntity> animationState) {
        if (animatable.isScratching() || animatable.isDozing() || animatable.isBuilding()) {
            return;
        }
        CoreGeoBone head = this.getAnimationProcessor().getBone("head");
        if (head == null) {
            return;
        }
        EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        float yaw = Mth.clamp(data.netHeadYaw(), -45.0F, 45.0F);
        float pitch = Mth.clamp(data.headPitch(), -30.0F, 30.0F);
        head.setRotX(head.getRotX() + pitch * Mth.DEG_TO_RAD);
        head.setRotY(head.getRotY() + yaw * Mth.DEG_TO_RAD);
    }
}
