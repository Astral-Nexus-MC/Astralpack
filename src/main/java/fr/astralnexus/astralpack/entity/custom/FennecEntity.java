package fr.astralnexus.astralpack.entity.custom;

import fr.astralnexus.astralpack.item.FennecArmorItem;
import fr.astralnexus.astralpack.item.FennecArmorSlot;
import fr.astralnexus.astralpack.item.FennecArmorTier;
import fr.astralnexus.astralpack.registry.ModEntities;
import fr.astralnexus.astralpack.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Predicate;

public class FennecEntity extends TamableAnimal implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.fennec.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.fennec.walk");
    private static final RawAnimation SIT = RawAnimation.begin().thenPlayAndHold("animation.fennec.sit");
    private static final RawAnimation ATTACK = RawAnimation.begin().thenPlayAndHold("animation.fennec.attack");
    private static final RawAnimation SHAKE = RawAnimation.begin().then("animation.fennec.shake", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation BITE = RawAnimation.begin().then("animation.fennec.bite", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.fennec.sleep");
    private static final RawAnimation BELLY_SCRATCH = RawAnimation.begin().thenLoop("animation.fennec.belly_scratch");

    private static final Ingredient FOOD = Ingredient.of(Items.CHICKEN, Items.RABBIT);
    private static final Ingredient TEMPT_ITEMS = FOOD;
    private static final Predicate<LivingEntity> PREY = e -> e instanceof Chicken || e instanceof Rabbit;
    private static final int SCRATCH_DURATION = 100;

    // Équilibrage : références du loup (vie 8/20, dégâts 2/4, vitesse 0,3, poursuite x1,5, délai d'attaque 20 ticks).
    // Le Fennec a la même vie et les mêmes dégâts, mais se déplace et attaque plus vite.
    private static final double WILD_HEALTH = 8.0D;
    private static final double TAMED_HEALTH = 20.0D;
    private static final double WILD_DAMAGE = 2.0D;
    private static final double TAMED_DAMAGE = 4.0D;
    private static final double MOVEMENT_SPEED = 0.38D;
    private static final double CHASE_SPEED = 1.8D;
    private static final int ATTACK_INTERVAL = 14;

    // Rythme nocturne : actif la nuit (bonus), endormi puis affaibli le jour (malus).
    private static final double NIGHT_DAMAGE_BONUS = 0.25D;
    private static final double DAY_DAMAGE_MALUS = -0.25D;
    private static final int NIGHT_ATTACK_INTERVAL = 11;
    private static final int DAY_ATTACK_INTERVAL = 19;
    private static final UUID NOCTURNAL_UUID = UUID.fromString("3c8f1b52-7d0e-4a9b-b6c4-1e5f2a7d9c30");
    private static final int PICKUP_COOLDOWN_AFTER_DROP = 400;

    private static final EntityDataAccessor<Boolean> DOZING =
            SynchedEntityData.defineId(FennecEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> SCRATCHING =
            SynchedEntityData.defineId(FennecEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<ItemStack> ARMOR_HEAD =
            SynchedEntityData.defineId(FennecEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> ARMOR_BODY =
            SynchedEntityData.defineId(FennecEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack> ARMOR_FEET =
            SynchedEntityData.defineId(FennecEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<ItemStack>[] ARMOR = new EntityDataAccessor[]{ARMOR_HEAD, ARMOR_BODY, ARMOR_FEET};

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private FennecOrder order = FennecOrder.FOLLOW;
    private int timePhase = 99;
    private int pickupCooldown;
    private boolean wasWet;
    private int scratchTicks;

    public FennecEntity(EntityType<? extends FennecEntity> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(true);
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, WILD_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, WILD_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ScratchGoal());
        this.goalSelector.addGoal(2, new FennecSleepGoal());
        this.goalSelector.addGoal(3, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(5, new FennecMeleeGoal());
        this.goalSelector.addGoal(6, new FennecFollowGoal());
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new TemptGoal(this, 1.1D, TEMPT_ITEMS, false));
        this.goalSelector.addGoal(8, new FennecSearchItemsGoal());
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SCRATCHING, false);
        this.entityData.define(DOZING, false);
        for (EntityDataAccessor<ItemStack> accessor : ARMOR) {
            this.entityData.define(accessor, ItemStack.EMPTY);
        }
    }

    public boolean hasArmor() {
        for (FennecArmorSlot slot : FennecArmorSlot.values()) {
            if (this.getArmorTier(slot) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public FennecArmorTier getArmorTier(FennecArmorSlot slot) {
        ItemStack stack = this.entityData.get(ARMOR[slot.ordinal()]);
        return stack.getItem() instanceof FennecArmorItem armor ? armor.getTier() : null;
    }

    private void setArmor(FennecArmorSlot slot, ItemStack stack) {
        EntityDataAccessor<ItemStack> accessor = ARMOR[slot.ordinal()];
        ItemStack previous = this.entityData.get(accessor);
        if (!previous.isEmpty()) {
            this.spawnAtLocation(previous);
        }
        this.entityData.set(accessor, stack);
        this.applyArmorModifiers();
        FennecArmorTier tier = this.getArmorTier(slot);
        if (tier != null) {
            this.playSound(tier.getEquipSound(), 1.0F, slot.getEquipPitch());
        } else if (!previous.isEmpty() && previous.getItem() instanceof FennecArmorItem old) {
            this.playSound(old.getTier().getEquipSound(), 0.8F, slot.getEquipPitch() - 0.3F);
        }
    }

    private void removeAllArmor() {
        for (FennecArmorSlot slot : FennecArmorSlot.values()) {
            if (this.getArmorTier(slot) != null) {
                this.setArmor(slot, ItemStack.EMPTY);
            }
        }
    }

    private void applyArmorModifiers() {
        for (FennecArmorSlot slot : FennecArmorSlot.values()) {
            double defense = 0.0D;
            double toughness = 0.0D;
            double knockback = 0.0D;
            if (this.entityData.get(ARMOR[slot.ordinal()]).getItem() instanceof FennecArmorItem armor) {
                defense = armor.getDefense();
                toughness = armor.getToughness();
                knockback = armor.getKnockbackResistance();
            }
            this.replaceModifier(Attributes.ARMOR, slot, "armor", defense);
            this.replaceModifier(Attributes.ARMOR_TOUGHNESS, slot, "toughness", toughness);
            this.replaceModifier(Attributes.KNOCKBACK_RESISTANCE, slot, "knockback", knockback);
        }
    }

    private void replaceModifier(Attribute attribute, FennecArmorSlot slot, String kind, double amount) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        UUID id = UUID.nameUUIDFromBytes(("fennec_armor_" + slot.name() + "_" + kind).getBytes(StandardCharsets.UTF_8));
        instance.removeModifier(id);
        if (amount > 0.0D) {
            instance.addTransientModifier(new AttributeModifier(id, "Fennec " + kind + " " + slot.name(), amount, AttributeModifier.Operation.ADDITION));
        }
    }

    @Nullable
    private FennecArmorTier getSoundTier(FennecArmorSlot... priority) {
        for (FennecArmorSlot slot : priority) {
            FennecArmorTier tier = this.getArmorTier(slot);
            if (tier != null) {
                return tier;
            }
        }
        return null;
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        super.playHurtSound(source);
        FennecArmorTier tier = this.getSoundTier(FennecArmorSlot.BODY, FennecArmorSlot.HEAD);
        if (tier != null) {
            this.playSound(tier.getHitSound(), 0.6F, 0.8F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
        FennecArmorTier tier = this.getArmorTier(FennecArmorSlot.FEET);
        if (tier != null) {
            this.playSound(tier.getStepSound(), 0.25F, 1.0F + this.random.nextFloat() * 0.2F);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (FennecArmorSlot slot : FennecArmorSlot.values()) {
            ItemStack armor = this.entityData.get(ARMOR[slot.ordinal()]);
            if (!armor.isEmpty()) {
                this.spawnAtLocation(armor);
            }
        }
    }

    public static boolean checkFennecSpawnRules(EntityType<FennecEntity> type, LevelAccessor level,
                                                MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        boolean ground = below.is(BlockTags.SAND) || below.is(BlockTags.TERRACOTTA)
                || below.is(BlockTags.DIRT) || below.is(Blocks.GRASS_BLOCK);
        return ground && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isDozing() ? null : ModSounds.FENNEC_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.FENNEC_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.FENNEC_DEATH.get();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        this.entityData.set(DOZING, false);
        if (this.order == FennecOrder.STAY) {
            this.setOrder(FennecOrder.FOLLOW);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
            if (!this.level().isClientSide) {
                this.triggerAnim("action", "bite");
            }
        }
        return hit;
    }

    public boolean isScratching() {
        return this.entityData.get(SCRATCHING);
    }

    private void startScratching() {
        this.scratchTicks = SCRATCH_DURATION;
        this.entityData.set(SCRATCHING, true);
        this.getNavigation().stop();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        if (this.pickupCooldown > 0) {
            this.pickupCooldown--;
        }
        if (this.tickCount % 20 == 0) {
            this.updateNocturnalPhase();
        }
        boolean wet = this.isInWaterOrRain();
        if (this.wasWet && !wet && !this.isScratching()) {
            this.triggerAnim("action", "shake");
        }
        this.wasWet = wet;

        if (this.isScratching()) {
            if (--this.scratchTicks <= 0 || this.isOrderedToSit() || this.getTarget() != null) {
                this.entityData.set(SCRATCHING, false);
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.level().isClientSide) {
            boolean canInteract = this.isTame() ? this.isOwnedBy(player) : stack.is(Items.CHICKEN);
            return canInteract ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        this.entityData.set(DOZING, false);

        if (this.isTame()) {
            if (!this.isOwnedBy(player)) {
                return InteractionResult.PASS;
            }
            if (stack.getItem() instanceof FennecArmorItem armorItem) {
                ItemStack worn = stack.copy();
                worn.setCount(1);
                this.setArmor(armorItem.getSlot(), worn);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (stack.is(Items.SHEARS) && this.hasArmor()) {
                this.removeAllArmor();
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                return InteractionResult.SUCCESS;
            }
            if (FOOD.test(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(4.0F);
                return InteractionResult.SUCCESS;
            }
            if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND && !this.getMainHandItem().isEmpty()) {
                this.dropMouthItem();
                return InteractionResult.SUCCESS;
            }
            if (stack.isEmpty() && hand == InteractionHand.MAIN_HAND) {
                if (player.isShiftKeyDown()) {
                    if (this.order == FennecOrder.STAY) {
                        this.setOrder(FennecOrder.FOLLOW);
                    }
                    this.startScratching();
                } else {
                    this.entityData.set(SCRATCHING, false);
                    FennecOrder next = this.order.next();
                    this.setOrder(next);
                    player.displayClientMessage(
                            Component.translatable("message.astralpack.fennec.order." + next.getId()), true);
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }

        if (stack.is(Items.CHICKEN)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrder(FennecOrder.STAY);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void setTame(boolean tamed) {
        super.setTame(tamed);
        double maxHealth = tamed ? TAMED_HEALTH : WILD_HEALTH;
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(tamed ? TAMED_DAMAGE : WILD_DAMAGE);
        if (tamed) {
            this.setHealth((float) maxHealth);
        }
    }

    public boolean isDozing() {
        return this.entityData.get(DOZING);
    }

    /** Bonus la nuit, malus le jour (dégâts et cadence d'attaque) ; neutre sans cycle jour/nuit. */
    private void updateNocturnalPhase() {
        int phase = this.level().isNight() ? 1 : this.level().isDay() ? -1 : 0;
        if (phase == this.timePhase) {
            return;
        }
        this.timePhase = phase;
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage == null) {
            return;
        }
        damage.removeModifier(NOCTURNAL_UUID);
        if (phase != 0) {
            double amount = phase > 0 ? NIGHT_DAMAGE_BONUS : DAY_DAMAGE_MALUS;
            damage.addTransientModifier(new AttributeModifier(NOCTURNAL_UUID, "Fennec nocturnal",
                    amount, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    private int currentAttackInterval() {
        return this.timePhase == 1 ? NIGHT_ATTACK_INTERVAL
                : this.timePhase == -1 ? DAY_ATTACK_INTERVAL : ATTACK_INTERVAL;
    }

    /** Un Fennec sauvage dort le jour ; un Fennec apprivoisé seulement sous l'ordre « rester ». */
    private boolean canNap() {
        if (!this.level().isDay() || !this.onGround() || this.isInWaterOrBubble()) {
            return false;
        }
        if (this.getTarget() != null || this.getLastHurtByMob() != null || this.isAggressive()
                || this.isScratching() || this.isLeashed()) {
            return false;
        }
        if (this.isTame()) {
            return this.order == FennecOrder.STAY;
        }
        return this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(3.0D),
                p -> !p.isSpectator() && !p.isCrouching()).isEmpty();
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return this.canHoldItem(stack);
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        return this.getMainHandItem().isEmpty() && this.pickupCooldown <= 0 && !this.isDozing()
                && !this.isScratching() && !this.isOrderedToSit() && !this.isBaby();
    }

    /** Prend un seul objet dans la gueule ; le reste de la pile reste au sol. */
    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (!this.canHoldItem(stack)) {
            return;
        }
        this.onItemPickup(itemEntity);
        this.setItemSlot(EquipmentSlot.MAINHAND, stack.split(1));
        this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);
        this.take(itemEntity, 1);
        if (stack.isEmpty()) {
            itemEntity.discard();
        }
    }

    private void dropMouthItem() {
        ItemStack held = this.getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.pickupCooldown = PICKUP_COOLDOWN_AFTER_DROP;
        this.spawnAtLocation(held);
    }

    public FennecOrder getOrder() {
        return this.order;
    }

    /** Applique un ordre : STAY assoit le Fennec, FOLLOW et WANDER le laissent debout. */
    private void setOrder(FennecOrder newOrder) {
        this.order = newOrder;
        this.setOrderedToSit(newOrder == FennecOrder.STAY);
        this.jumping = false;
        this.navigation.stop();
        if (newOrder == FennecOrder.STAY) {
            this.setTarget(null);
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return FOOD.test(stack);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        FennecEntity baby = ModEntities.FENNEC.get().create(level);
        if (baby != null && this.isTame() && this.getOwnerUUID() != null) {
            baby.setOwnerUUID(this.getOwnerUUID());
            baby.setTame(true);
        }
        return baby;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FennecOrder", this.order.ordinal());
        for (FennecArmorSlot slot : FennecArmorSlot.values()) {
            ItemStack armor = this.entityData.get(ARMOR[slot.ordinal()]);
            if (!armor.isEmpty()) {
                tag.put("FennecArmor" + slot.name(), armor.save(new CompoundTag()));
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("FennecOrder")) {
            this.order = FennecOrder.byOrdinal(tag.getInt("FennecOrder"));
        } else {
            this.order = this.isOrderedToSit() ? FennecOrder.STAY : FennecOrder.FOLLOW;
        }
        for (FennecArmorSlot slot : FennecArmorSlot.values()) {
            String key = "FennecArmor" + slot.name();
            if (tag.contains(key)) {
                this.entityData.set(ARMOR[slot.ordinal()], ItemStack.of(tag.getCompound(key)));
            }
        }
        this.applyArmorModifiers();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementController));
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("shake", SHAKE)
                .triggerableAnim("bite", BITE));
    }

    private PlayState movementController(AnimationState<FennecEntity> state) {
        if (this.isScratching()) {
            return state.setAndContinue(BELLY_SCRATCH);
        }
        if (this.isDozing()) {
            return state.setAndContinue(SLEEP);
        }
        if (this.isInSittingPose()) {
            return state.setAndContinue(SIT);
        }
        if (state.isMoving()) {
            return state.setAndContinue(WALK);
        }
        if (this.isAggressive()) {
            return state.setAndContinue(ATTACK);
        }
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Attaque au corps à corps plus rapide que le loup (14 ticks au lieu de 20). */
    private class FennecMeleeGoal extends MeleeAttackGoal {
        FennecMeleeGoal() {
            super(FennecEntity.this, CHASE_SPEED, true);
        }

        @Override
        protected int getAttackInterval() {
            return this.adjustedTickDelay(FennecEntity.this.currentAttackInterval());
        }
    }

    /** Dort quand les conditions de sommeil sont réunies (voir canNap). */
    private class FennecSleepGoal extends Goal {
        FennecSleepGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return FennecEntity.this.canNap() && FennecEntity.this.getRandom().nextInt(20) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return FennecEntity.this.canNap();
        }

        @Override
        public void start() {
            FennecEntity.this.getNavigation().stop();
            FennecEntity.this.entityData.set(DOZING, true);
        }

        @Override
        public void stop() {
            FennecEntity.this.entityData.set(DOZING, false);
        }
    }

    /** Va chercher un objet au sol pour le prendre dans la gueule. */
    private class FennecSearchItemsGoal extends Goal {
        private ItemEntity target;
        private int ticks;

        FennecSearchItemsGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            FennecEntity fennec = FennecEntity.this;
            if (!fennec.getMainHandItem().isEmpty() || fennec.pickupCooldown > 0 || fennec.isDozing()
                    || fennec.isOrderedToSit() || fennec.isBaby() || fennec.getTarget() != null
                    || fennec.getRandom().nextInt(10) != 0
                    || !ForgeEventFactory.getMobGriefingEvent(fennec.level(), fennec)) {
                return false;
            }
            if (fennec.isTame() && (fennec.getOwner() == null || fennec.distanceToSqr(fennec.getOwner()) > 144.0D)) {
                return false;
            }
            this.target = fennec.level().getEntitiesOfClass(ItemEntity.class,
                            fennec.getBoundingBox().inflate(8.0D, 4.0D, 8.0D),
                            e -> e.isAlive() && !e.hasPickUpDelay() && fennec.canHoldItem(e.getItem()))
                    .stream().min(java.util.Comparator.comparingDouble(fennec::distanceToSqr)).orElse(null);
            return this.target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.ticks < 200
                    && FennecEntity.this.getMainHandItem().isEmpty() && !FennecEntity.this.isDozing();
        }

        @Override
        public void start() {
            this.ticks = 0;
            FennecEntity.this.getNavigation().moveTo(this.target, 1.2D);
        }

        @Override
        public void tick() {
            this.ticks++;
            if (this.ticks % 20 == 0) {
                FennecEntity.this.getNavigation().moveTo(this.target, 1.2D);
            }
        }

        @Override
        public void stop() {
            this.target = null;
            FennecEntity.this.getNavigation().stop();
        }
    }

    /** Suit le propriétaire seulement sous l'ordre FOLLOW (en STAY il reste assis, en WANDER il erre). */
    private class FennecFollowGoal extends FollowOwnerGoal {
        FennecFollowGoal() {
            super(FennecEntity.this, 1.1D, 10.0F, 2.0F, false);
        }

        @Override
        public boolean canUse() {
            return FennecEntity.this.order == FennecOrder.FOLLOW && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return FennecEntity.this.order == FennecOrder.FOLLOW && super.canContinueToUse();
        }
    }

    private class ScratchGoal extends Goal {
        ScratchGoal() {
            this.setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return FennecEntity.this.isScratching();
        }

        @Override
        public boolean canContinueToUse() {
            return FennecEntity.this.isScratching();
        }
    }
}
