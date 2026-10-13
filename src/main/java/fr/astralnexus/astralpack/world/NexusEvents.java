package fr.astralnexus.astralpack.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.astralnexus.astralpack.Astralpack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Accès au Nexus Astral par commande (v1) et retour sur l'île centrale en cas de chute dans le vide. */
@Mod.EventBusSubscriber(modid = Astralpack.MOD_ID)
public class NexusEvents {
    /** En dessous de cette hauteur, le joueur est ramené au hub (le bas du monde est à 0). */
    private static final double VOID_RESCUE_Y = -16.0D;

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("nexus")
                .requires(source -> source.hasPermission(2))
                .executes(context -> enter(context, NexusDimension.NEXUS))
                .then(Commands.literal("retour").executes(NexusEvents::back)));
    }

    private static int enter(CommandContext<CommandSourceStack> context, ResourceKey<Level> dimension)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = context.getSource().getServer().getLevel(dimension);
        if (level == null) {
            context.getSource().sendFailure(Component.translatable("commands.astralpack.nexus.missing"));
            return 0;
        }
        NexusDimension.teleportToHub(player, level);
        context.getSource().sendSuccess(() -> Component.translatable("commands.astralpack.nexus.entered"), true);
        return 1;
    }

    private static int back(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel overworld = context.getSource().getServer().overworld();
        BlockPos spawn = overworld.getSharedSpawnPos();
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, player.getYRot(), 0.0F);
        player.fallDistance = 0.0F;
        context.getSource().sendSuccess(() -> Component.translatable("commands.astralpack.nexus.left"), true);
        return 1;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player) || !NexusDimension.isNexus(player.level())
                || player.getY() >= VOID_RESCUE_Y) {
            return;
        }
        NexusDimension.teleportToHub(player, (ServerLevel) player.level());
        player.displayClientMessage(Component.translatable("message.astralpack.nexus.rescued"), true);
    }
}
