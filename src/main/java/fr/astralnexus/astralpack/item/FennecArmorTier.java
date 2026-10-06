package fr.astralnexus.astralpack.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum FennecArmorTier {
    IRON("iron",
            new int[]{1, 2, 1}, new float[]{0.0F, 0.0F, 0.0F}, 0.0F,
            SoundEvents.ARMOR_EQUIP_IRON, SoundEvents.ANVIL_HIT, SoundEvents.CHAIN_STEP),
    GOLD("gold",
            new int[]{1, 1, 1}, new float[]{0.0F, 0.0F, 0.0F}, 0.0F,
            SoundEvents.ARMOR_EQUIP_GOLD, SoundEvents.COPPER_HIT, SoundEvents.COPPER_STEP),
    DIAMOND("diamond",
            new int[]{2, 3, 1}, new float[]{0.5F, 1.0F, 0.5F}, 0.0F,
            SoundEvents.ARMOR_EQUIP_DIAMOND, SoundEvents.AMETHYST_BLOCK_HIT, SoundEvents.AMETHYST_BLOCK_STEP),
    NETHERITE("netherite",
            new int[]{2, 3, 2}, new float[]{1.0F, 1.0F, 1.0F}, 0.1F / 3.0F,
            SoundEvents.ARMOR_EQUIP_NETHERITE, SoundEvents.NETHERITE_BLOCK_HIT, SoundEvents.NETHERITE_BLOCK_STEP);

    private final String name;
    private final int[] defense;
    private final float[] toughness;
    private final float knockbackResistance;
    private final SoundEvent equipSound;
    private final SoundEvent hitSound;
    private final SoundEvent stepSound;

    FennecArmorTier(String name, int[] defense, float[] toughness, float knockbackResistance,
                    SoundEvent equipSound, SoundEvent hitSound, SoundEvent stepSound) {
        this.name = name;
        this.defense = defense;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.equipSound = equipSound;
        this.hitSound = hitSound;
        this.stepSound = stepSound;
    }

    public String getName() {
        return name;
    }

    public int getDefense(FennecArmorSlot slot) {
        return defense[slot.ordinal()];
    }

    public float getToughness(FennecArmorSlot slot) {
        return toughness[slot.ordinal()];
    }

    public float getKnockbackResistance() {
        return knockbackResistance;
    }

    public SoundEvent getEquipSound() {
        return equipSound;
    }

    public SoundEvent getHitSound() {
        return hitSound;
    }

    public SoundEvent getStepSound() {
        return stepSound;
    }
}
