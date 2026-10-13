package fr.astralnexus.astralpack.world;

import java.util.ArrayList;
import java.util.List;

/** Forme de l'anneau du portail : un cercle de 13 x 13 (rayon 6), sans dépendance à Minecraft. */
public final class RingGeometry {
    public static final int RADIUS = 6;

    private static final List<int[]> RING = new ArrayList<>();
    private static final List<int[]> INTERIOR = new ArrayList<>();

    static {
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d >= RADIUS - 0.5 && d < RADIUS + 0.5) {
                    RING.add(new int[]{dx, dy});
                } else if (d < RADIUS - 0.5) {
                    INTERIOR.add(new int[]{dx, dy});
                }
            }
        }
    }

    private RingGeometry() {
    }

    /** Cases de l'anneau (dx, dy) autour du centre. */
    public static List<int[]> ring() {
        return RING;
    }

    /** Cases de la surface du portail (dx, dy) autour du centre. */
    public static List<int[]> interior() {
        return INTERIOR;
    }
}
