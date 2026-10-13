package fr.astralnexus.astralpack;

import fr.astralnexus.astralpack.world.RingGeometry;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Forme de l'anneau du portail : cercle de 13 x 13, fermé, symétrique, avec une surface pleine à l'intérieur. */
class RingGeometryTest {
    private static Set<String> keys(List<int[]> cells) {
        Set<String> set = new HashSet<>();
        for (int[] c : cells) {
            set.add(c[0] + "," + c[1]);
        }
        return set;
    }

    @Test
    void ringIsThirteenWide() {
        int min = 0;
        int max = 0;
        for (int[] c : RingGeometry.ring()) {
            min = Math.min(min, Math.min(c[0], c[1]));
            max = Math.max(max, Math.max(c[0], c[1]));
        }
        assertEquals(-6, min);
        assertEquals(6, max);
        assertTrue(RingGeometry.ring().size() >= 30 && RingGeometry.ring().size() <= 50,
                "taille de l'anneau inattendue : " + RingGeometry.ring().size());
    }

    @Test
    void ringIsSymmetricAndClosed() {
        Set<String> ring = keys(RingGeometry.ring());
        for (int[] c : RingGeometry.ring()) {
            assertTrue(ring.contains(-c[0] + "," + c[1]) && ring.contains(c[0] + "," + -c[1])
                    && ring.contains(c[1] + "," + c[0]), "anneau non symétrique en " + c[0] + "," + c[1]);
            int neighbours = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if ((dx != 0 || dy != 0) && ring.contains((c[0] + dx) + "," + (c[1] + dy))) {
                        neighbours++;
                    }
                }
            }
            assertTrue(neighbours >= 2, "case isolée de l'anneau en " + c[0] + "," + c[1]);
        }
    }

    @Test
    void interiorIsFilledAndSeparatedFromRing() {
        Set<String> ring = keys(RingGeometry.ring());
        Set<String> interior = keys(RingGeometry.interior());
        assertFalse(interior.isEmpty());
        for (String k : interior) {
            assertFalse(ring.contains(k), "case à la fois dans l'anneau et dans la surface : " + k);
        }
        assertTrue(interior.contains("0,0"));
        // Une case de la surface au contact de l'extérieur doit toucher l'anneau : la surface est fermée par l'anneau.
        for (int[] c : RingGeometry.interior()) {
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                String n = (c[0] + d[0]) + "," + (c[1] + d[1]);
                assertTrue(interior.contains(n) || ring.contains(n), "trou dans la clôture en " + c[0] + "," + c[1]);
            }
        }
    }
}
