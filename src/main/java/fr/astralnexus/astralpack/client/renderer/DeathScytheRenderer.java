package fr.astralnexus.astralpack.client.renderer;

import fr.astralnexus.astralpack.client.model.DeathScytheModel;
import fr.astralnexus.astralpack.item.DeathScytheItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DeathScytheRenderer extends GeoItemRenderer<DeathScytheItem> {
    public DeathScytheRenderer() {
        super(new DeathScytheModel());
    }
}
