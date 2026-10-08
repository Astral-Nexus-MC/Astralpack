package fr.astralnexus.astralpack.client.renderer;

import fr.astralnexus.astralpack.client.model.DeathScytheModel;
import fr.astralnexus.astralpack.item.DeathScytheItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class DeathScytheRenderer extends GeoItemRenderer<DeathScytheItem> {
    public DeathScytheRenderer() {
        super(new DeathScytheModel());
        // Croix, lame et bagues violettes brillent dans le noir (death_scythe_glowmask.png)
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
