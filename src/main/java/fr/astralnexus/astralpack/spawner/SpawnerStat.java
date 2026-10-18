package fr.astralnexus.astralpack.spawner;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Les cinq réglages d'un spawner que la console peut modifier. Chaque réglage a un niveau de -5 à +5 : un objet de
 * « bonus » le monte d'un cran, un objet de « malus » le descend d'un cran (un objet consommé par cran).
 * C'est ici que se règlent les effets et les objets demandés.
 */
public enum SpawnerStat {
    /** Délai entre deux apparitions : x 0,75 par cran de bonus (minimum 10 ticks), x 1,33 par cran de malus. */
    CADENCE("cadence", "MinSpawnDelay", "MaxSpawnDelay", "astralpack:astralite", "minecraft:cobweb"),
    /** Nombre de monstres par apparition : +1 par cran (de 1 à 12). */
    COUNT("count", "SpawnCount", null, "astralpack:nebula_shard", "minecraft:iron_bars"),
    /** Monstres autour du spawner avant qu'il s'arrête : +4 par cran (de 1 à 64). */
    POPULATION("population", "MaxNearbyEntities", null, "minecraft:bone_block", "minecraft:lead"),
    /** Distance d'un joueur qui active le spawner : +8 blocs par cran (de 4 à 64). */
    RANGE("range", "RequiredPlayerRange", null, "minecraft:ender_eye", "minecraft:slime_ball"),
    /** Rayon dans lequel les monstres apparaissent : +2 blocs par cran (de 1 à 16). */
    RADIUS("radius", "SpawnRange", null, "minecraft:compass", "minecraft:string");

    public static final int MAX_LEVEL = 5;

    private final String id;
    private final String key;
    private final String secondKey;
    private final String bonusItem;
    private final String malusItem;

    SpawnerStat(String id, String key, String secondKey, String bonusItem, String malusItem) {
        this.id = id;
        this.key = key;
        this.secondKey = secondKey;
        this.bonusItem = bonusItem;
        this.malusItem = malusItem;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return "spawner_stat.astralpack." + this.id;
    }

    /** Objet de bonus de ce réglage. */
    public Item bonusItem() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.bonusItem));
    }

    /** Objet de malus de ce réglage. */
    public Item malusItem() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(this.malusItem));
    }

    /** Le réglage que cet objet améliore (+1) ou dégrade (-1), ou {@code null}. */
    public static SpawnerStat forItem(ItemStack stack, boolean[] bonus) {
        for (SpawnerStat stat : values()) {
            if (stack.is(stat.bonusItem())) {
                bonus[0] = true;
                return stat;
            }
            if (stack.is(stat.malusItem())) {
                bonus[0] = false;
                return stat;
            }
        }
        return null;
    }

    /** Clés NBT du spawner modifiées par ce réglage. */
    public String[] keys() {
        return this.secondKey == null ? new String[]{this.key} : new String[]{this.key, this.secondKey};
    }

    /** Valeur modifiée à partir de la valeur d'origine du spawner. */
    public int apply(int base, int level) {
        return switch (this) {
            case CADENCE -> (int) Math.max(10, Math.round(base * Math.pow(0.75D, level)));
            case COUNT -> Math.max(1, Math.min(12, base + level));
            case POPULATION -> Math.max(1, Math.min(64, base + 4 * level));
            case RANGE -> Math.max(4, Math.min(64, base + 8 * level));
            case RADIUS -> Math.max(1, Math.min(16, base + 2 * level));
        };
    }
}
