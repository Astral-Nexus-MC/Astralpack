package fr.astralnexus.astralpack.item;

/**
 * Arme à deux mains : pleins dégâts et effets seulement quand la main secondaire est vide. Le malus est appliqué côté
 * serveur par {@link TwoHandedEvents}.
 */
public interface TwoHanded {
    /** Part des dégâts conservée quand la main secondaire est occupée. */
    float offhandDamageFactor();
}
