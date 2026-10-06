package fr.astralnexus.astralpack.item;

import net.minecraft.world.item.Item;

public class FennecArmorItem extends Item {
    private final FennecArmorTier tier;
    private final FennecArmorSlot slot;

    public FennecArmorItem(FennecArmorTier tier, FennecArmorSlot slot, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
        this.slot = slot;
    }

    public FennecArmorTier getTier() {
        return tier;
    }

    public FennecArmorSlot getSlot() {
        return slot;
    }

    public int getDefense() {
        return tier.getDefense(slot);
    }

    public float getToughness() {
        return tier.getToughness(slot);
    }

    public float getKnockbackResistance() {
        return tier.getKnockbackResistance();
    }
}
