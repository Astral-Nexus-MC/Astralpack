package fr.astralnexus.astralpack.client;

import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.client.pose.TwoHandedPose;
import fr.astralnexus.astralpack.client.renderer.FennecRenderer;
import fr.astralnexus.astralpack.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    static {
        // La pose de bras ajoute une valeur à l'enum vanilla : elle doit exister avant le premier rendu d'un joueur,
        // sinon la table de correspondance interne de HumanoidModel est trop courte (ArrayIndexOutOfBounds).
        TwoHandedPose.register();
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.FENNEC.get(), FennecRenderer::new);
    }
}
