package fr.astralnexus.astralpack.entity.custom;

/** Ordre donné à un Fennec apprivoisé par la roue d'interaction Fordix (« Suis-moi » / « Reste ici »). */
public enum FennecOrder {
    FOLLOW("follow"),
    STAY("stay");

    private final String id;

    FennecOrder(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static FennecOrder byOrdinal(int ordinal) {
        FennecOrder[] all = values();
        return ordinal >= 0 && ordinal < all.length ? all[ordinal] : FOLLOW;
    }
}
