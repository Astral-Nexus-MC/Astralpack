package fr.astralnexus.astralpack.item;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Matériaux d'outils et d'armes du mod. Dégâts affichés = 1 + bonus du tier + modificateur de l'arme. */
public enum ModTiers implements Tier {
    /** Faux de la Mort : niveau netherite, durabilité réduite, plus offensive (bonus 3, pour 9 dégâts avec +5 de l'arme). */
    DEATH(4, 1400, 8.0F, 3.0F, 15);

    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;

    ModTiers(int level, int uses, float speed, float damage, int enchantmentValue) {
        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return damage;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.NETHERITE_INGOT);
    }
}
