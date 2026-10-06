package fr.astralnexus.astralpack.entity.custom;

/** Ordre donné à un Fennec apprivoisé (clic main vide du propriétaire : suivre, rester, errer). */
public enum FennecOrder {
    FOLLOW("follow"),
    STAY("stay"),
    WANDER("wander");

    private final String id;

    FennecOrder(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public FennecOrder next() {
        FennecOrder[] all = values();
        return all[(ordinal() + 1) % all.length];
    }

    public static FennecOrder byOrdinal(int ordinal) {
        FennecOrder[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : FOLLOW;
    }
}
