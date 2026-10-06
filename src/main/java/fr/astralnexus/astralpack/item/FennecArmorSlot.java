package fr.astralnexus.astralpack.item;

public enum FennecArmorSlot {
    HEAD("helmet", 1.15F),
    BODY("chestplate", 0.9F),
    FEET("boots", 1.05F);

    private final String itemSuffix;
    private final float equipPitch;

    FennecArmorSlot(String itemSuffix, float equipPitch) {
        this.itemSuffix = itemSuffix;
        this.equipPitch = equipPitch;
    }

    public String getItemSuffix() {
        return itemSuffix;
    }

    public float getEquipPitch() {
        return equipPitch;
    }
}
