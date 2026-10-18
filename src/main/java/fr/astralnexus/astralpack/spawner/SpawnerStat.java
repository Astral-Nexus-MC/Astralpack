package fr.astralnexus.astralpack.spawner;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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
    CADENCE("cadence", "MinSpawnDelay", "MaxSpawnDelay", "minecraft:quartz", "minecraft:cobweb"),
    /** Nombre de monstres par apparition : +1 par cran (de 1 à 12). */
    COUNT("count", "SpawnCount", null, "minecraft:lightning_rod", "minecraft:iron_bars"),
    /**
     * Monstres autour du spawner avant qu'il s'arrête : x 4 au premier cran de bonus puis il double à chaque cran (6 devient 24, 48, 96, 192, 384 ; jusqu'à 32 000), x 0,5 par cran de
     * malus. Sur un spawner posé avec la clé à molette, les monstres fusionnent (voir {@link SpawnerEvents}).
     */
    POPULATION("population", "MaxNearbyEntities", null, "minecraft:bone_block", "minecraft:lead"),
    /** Distance d'un joueur qui active le spawner : +8 blocs par cran (de 4 à 64). */
    RANGE("range", "RequiredPlayerRange", null, "minecraft:ender_eye", "minecraft:slime_ball"),
    /** Rayon dans lequel les monstres apparaissent : +2 blocs par cran (de 1 à 16). */
    RADIUS("radius", "SpawnRange", null, "minecraft:compass", "minecraft:string"),
    /**
     * Vie des monstres à l'apparition : +50 % par cran de bonus (jusqu'à x 3,5) ; en malus -20 % par cran, et au
     * niveau -5 les monstres n'ont plus que 0,5 point de vie (un quart de cœur). Ce réglage n'est pas écrit dans le spawner :
     * il s'applique aux monstres au moment où ils apparaissent (voir {@link SpawnerEvents}). Un objet commençant par
     * # est une étiquette d'objets (ici la nourriture crue).
     */
    HEALTH("health", null, null, "#astralpack:raw_food", "minecraft:pointed_dripstone");

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

    private static boolean matches(ItemStack stack, String spec) {
        if (spec.startsWith("#")) {
            return stack.is(TagKey.create(Registries.ITEM, new ResourceLocation(spec.substring(1))));
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(spec));
        return item != null && stack.is(item);
    }

    private static Component name(String spec) {
        if (spec.startsWith("#")) {
            ResourceLocation tag = new ResourceLocation(spec.substring(1));
            return Component.translatable("tag.item." + tag.getNamespace() + "." + tag.getPath());
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(spec));
        return item == null ? Component.literal(spec) : item.getDescription();
    }

    /** Nom de l'objet (ou de la catégorie d'objets) de bonus. */
    public Component bonusName() {
        return name(this.bonusItem);
    }

    /** Nom de l'objet (ou de la catégorie d'objets) de malus. */
    public Component malusName() {
        return name(this.malusItem);
    }

    /** Le réglage que cet objet améliore (+1) ou dégrade (-1), ou {@code null}. */
    public static SpawnerStat forItem(ItemStack stack, boolean[] bonus) {
        for (SpawnerStat stat : values()) {
            if (matches(stack, stat.bonusItem)) {
                bonus[0] = true;
                return stat;
            }
            if (matches(stack, stat.malusItem)) {
                bonus[0] = false;
                return stat;
            }
        }
        return null;
    }

    /** Clés NBT du spawner modifiées par ce réglage. */
    public String[] keys() {
        if (this.key == null) {
            return new String[0];
        }
        return this.secondKey == null ? new String[]{this.key} : new String[]{this.key, this.secondKey};
    }

    /**
     * Vie d'un monstre qui apparaît avec ce niveau de réglage, en points (2 points = 1 cœur) : au niveau -5, 0,5 point,
     * soit un quart de cœur. L'attribut de vie maximale ne descend pas sous 1 : le monstre a alors 1 de vie maximale et
     * une vie actuelle de 0,5.
     */
    public static double health(double base, int level) {
        if (level >= 0) {
            return base * (1.0D + 0.5D * level);
        }
        if (level <= -MAX_LEVEL) {
            return 0.5D;
        }
        return Math.max(0.5D, base * (1.0D + 0.2D * level));
    }

    /** Valeur modifiée à partir de la valeur d'origine du spawner. */
    public int apply(int base, int level) {
        return switch (this) {
            case CADENCE -> (int) Math.max(10, Math.round(base * Math.pow(0.75D, level)));
            case COUNT -> Math.max(1, Math.min(12, base + level));
            case POPULATION -> level >= 0 ? (int) Math.min(32000L, Math.round(base * Math.pow(2.0D, level + 1)))
                    : Math.max(1, (int) Math.round(base * Math.pow(0.5D, -level)));
            case RANGE -> Math.max(4, Math.min(64, base + 8 * level));
            case RADIUS -> Math.max(1, Math.min(16, base + 2 * level));
            case HEALTH -> base;
        };
    }
}
