package fr.astralnexus.astralpack.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fr.astralnexus.astralpack.client.renderer.DeathScytheRenderer;
import net.minecraft.ChatFormatting;
import fr.astralnexus.astralpack.client.pose.TwoHandedPose;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Faux de la Mort : arme à deux mains. À pleine puissance (main secondaire vide) : 9 dégâts, allonge +1 bloc, frappe en
 * arc comme l'épée (héritée de SwordItem) et Wither I pendant 3 secondes sur la cible. Main secondaire occupée : dégâts
 * divisés par trois et pas de Wither (voir TwoHandedEvents).
 */
public class DeathScytheItem extends SwordItem implements GeoItem, TwoHanded {
    /** Modificateur de l'arme : 1 (base) + 3 (tier) + 5 = 9 dégâts. */
    private static final int ATTACK_DAMAGE_MODIFIER = 5;
    /** Cadence : 4,0 - 3,0 = 1,0 attaque par seconde. */
    private static final float ATTACK_SPEED_MODIFIER = -3.0F;
    private static final double EXTRA_REACH = 1.0D;
    private static final UUID REACH_MODIFIER_ID = UUID.fromString("3c5f6c1e-7a52-4d1f-9a3e-5d0b6a1c7e21");
    private static final float OFFHAND_DAMAGE_FACTOR = 1.0F / 3.0F;
    private static final int WITHER_TICKS = 60;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DeathScytheItem(Properties properties) {
        super(ModTiers.DEATH, ATTACK_DAMAGE_MODIFIER, ATTACK_SPEED_MODIFIER, properties);
    }

    @Override
    public float offhandDamageFactor() {
        return OFFHAND_DAMAGE_FACTOR;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> base = super.getAttributeModifiers(slot, stack);
        if (slot != EquipmentSlot.MAINHAND) {
            return base;
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(REACH_MODIFIER_ID, "Weapon modifier", EXTRA_REACH, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean hit = super.hurtEnemy(stack, target, attacker);
        if (hit && attacker instanceof Player player && player.getOffhandItem().isEmpty() && !player.level().isClientSide) {
            if (target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS, 0), attacker)) {
                player.level().playSound(null, target.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS,
                        0.4F, 1.4F);
            }
            if (player.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL, target.getX(), target.getY(0.6D), target.getZ(), 8, 0.3D, 0.4D,
                        0.3D, 0.03D);
            }
        }
        return hit;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.astralpack.death_scythe.two_handed").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.astralpack.death_scythe.wither").withStyle(ChatFormatting.DARK_PURPLE));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private DeathScytheRenderer renderer;

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (hand == InteractionHand.MAIN_HAND) {
                    return TwoHandedPose.POSE;
                }
                return null;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new DeathScytheRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
