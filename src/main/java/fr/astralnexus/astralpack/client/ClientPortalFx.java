package fr.astralnexus.astralpack.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.network.PortalFxPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Effet de traversée façon Stargate. En entrant : le champ de vision s'élargit comme si l'on était étiré vers la surface,
 * un disque de lumière bleue et blanche grandit depuis le centre de l'écran, des ondes s'en détachent, puis tout devient
 * blanc. À l'arrivée : le blanc se dissipe en ondes qui s'éloignent et le champ de vision se resserre.
 */
@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID, value = Dist.CLIENT)
public final class ClientPortalFx {
    private static final float FOV_KICK = 45.0F;
    private static final int[] BLUE = {74, 168, 255};
    private static final int[] WHITE = {255, 255, 255};
    private static final int[] ICE = {190, 230, 255};

    private static int mode;
    private static long startMs;
    private static long durationMs = 1L;

    private ClientPortalFx() {
    }

    public static void start(int newMode, int ticks) {
        mode = newMode;
        startMs = System.currentTimeMillis();
        durationMs = Math.max(1, ticks) * 50L;
    }

    private static float progress() {
        if (mode == 0) {
            return 1.0F;
        }
        float p = (System.currentTimeMillis() - startMs) / (float) durationMs;
        if (p >= 1.0F && mode == PortalFxPacket.ARRIVE) {
            mode = 0;
        }
        return Mth.clamp(p, 0.0F, 1.0F);
    }

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (mode == 0) {
            return;
        }
        float p = progress();
        float kick = mode == PortalFxPacket.ABSORB ? p * p : (1.0F - p) * (1.0F - p);
        event.setFOV(event.getFOV() + FOV_KICK * kick);
    }

    @SubscribeEvent
    public static void onRender(RenderGuiEvent.Post event) {
        if (mode == 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            mode = 0;
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        float p = progress();
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 500.0F);
        Matrix4f matrix = graphics.pose().last().pose();
        float cx = width / 2.0F;
        float cy = height / 2.0F;
        float diagonal = (float) Math.sqrt((double) width * width + (double) height * height);
        if (mode == PortalFxPacket.ABSORB) {
            absorb(graphics, matrix, p, width, height, cx, cy, diagonal);
        } else {
            arrive(graphics, matrix, p, width, height, cx, cy, diagonal);
        }
        graphics.pose().popPose();
    }

    private static void absorb(GuiGraphics graphics, Matrix4f matrix, float p, int width, int height, float cx, float cy,
                               float diagonal) {
        float grow = (float) Math.pow(p, 1.3D);
        graphics.fill(0, 0, width, height, argb(BLUE, 0.5F * p * p));
        disc(matrix, cx, cy, diagonal * 0.65F * grow, rgba(WHITE, 0.95F * p), rgba(BLUE, 0.0F));
        for (int ring = 0; ring < 3; ring++) {
            float r = Mth.clamp(p * 1.35F - ring * 0.22F, 0.0F, 1.0F);
            if (r > 0.0F && r < 1.0F) {
                ring(matrix, cx, cy, diagonal * 0.7F * r, 5.0F, rgba(ICE, 0.6F * (1.0F - r)));
            }
        }
        if (p > 0.75F) {
            graphics.fill(0, 0, width, height, argb(WHITE, (p - 0.75F) / 0.25F));
        }
    }

    private static void arrive(GuiGraphics graphics, Matrix4f matrix, float p, int width, int height, float cx, float cy,
                               float diagonal) {
        float fade = (float) Math.pow(1.0F - p, 1.5D);
        graphics.fill(0, 0, width, height, argb(WHITE, fade));
        graphics.fill(0, 0, width, height, argb(BLUE, 0.25F * fade * fade));
        for (int ring = 0; ring < 3; ring++) {
            float r = Mth.clamp(p * 1.2F - ring * 0.18F, 0.0F, 1.0F);
            if (r > 0.0F && r < 1.0F) {
                ring(matrix, cx, cy, diagonal * 0.6F * r, 6.0F, rgba(ICE, 0.5F * (1.0F - r)));
            }
        }
    }

    private static int argb(int[] rgb, float alpha) {
        int a = Mth.clamp(Math.round(alpha * 255.0F), 0, 255);
        return (a << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private static int[] rgba(int[] rgb, float alpha) {
        return new int[]{rgb[0], rgb[1], rgb[2], Mth.clamp(Math.round(alpha * 255.0F), 0, 255)};
    }

    /** Disque à dégradé radial : couleur du centre vers couleur du bord. */
    private static void disc(Matrix4f matrix, float cx, float cy, float radius, int[] center, int[] edge) {
        if (radius < 1.0F) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        builder.vertex(matrix, cx, cy, 0.0F).color(center[0], center[1], center[2], center[3]).endVertex();
        int segments = 56;
        for (int i = 0; i <= segments; i++) {
            double angle = i * Math.PI * 2.0D / segments;
            builder.vertex(matrix, cx + (float) Math.cos(angle) * radius, cy + (float) Math.sin(angle) * radius, 0.0F)
                    .color(edge[0], edge[1], edge[2], edge[3]).endVertex();
        }
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
    }

    /** Anneau fin : une onde qui s'éloigne. */
    private static void ring(Matrix4f matrix, float cx, float cy, float radius, float thickness, int[] color) {
        if (radius < 1.0F || color[3] <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        int segments = 72;
        float inner = Math.max(0.0F, radius - thickness);
        for (int i = 0; i <= segments; i++) {
            double angle = i * Math.PI * 2.0D / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            builder.vertex(matrix, cx + cos * inner, cy + sin * inner, 0.0F)
                    .color(color[0], color[1], color[2], color[3]).endVertex();
            builder.vertex(matrix, cx + cos * radius, cy + sin * radius, 0.0F)
                    .color(color[0], color[1], color[2], 0).endVertex();
        }
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
    }
}
