package fr.astralnexus.astralpack.world;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.astralnexus.astralpack.Astralpack;
import fr.astralnexus.astralpack.network.Notify;
import fr.astralnexus.astralpack.registry.ModItems;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Accès à l'Astral Nexus par commande (v1) et retour sur l'île centrale en cas de chute dans le vide. */
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
                .then(Commands.literal("retour").executes(NexusEvents::back))
                .then(Commands.literal("faux")
                        .executes(NexusEvents::scytheStatus)
                        .then(Commands.literal("reset").executes(context -> setScytheCrafted(context, false)))
                        .then(Commands.literal("bloquer").executes(context -> setScytheCrafted(context, true)))));
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

    private static int scytheStatus(CommandContext<CommandSourceStack> context) {
        boolean crafted = NexusData.get(context.getSource().getServer()).isScytheCrafted();
        context.getSource().sendSuccess(() -> Component.translatable(crafted
                ? "commands.astralpack.nexus.scythe.crafted" : "commands.astralpack.nexus.scythe.available"), false);
        return 1;
    }

    private static int setScytheCrafted(CommandContext<CommandSourceStack> context, boolean crafted) {
        NexusData.get(context.getSource().getServer()).setScytheCrafted(crafted);
        context.getSource().sendSuccess(() -> Component.translatable(crafted
                ? "commands.astralpack.nexus.scythe.locked" : "commands.astralpack.nexus.scythe.unlocked"), true);
        return 1;
    }

    /** La première Death Scythe fabriquée verrouille la recette pour tout le serveur. */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!event.getEntity().level().isClientSide && event.getCrafting().is(ModItems.DEATH_SCYTHE.get())
                && event.getEntity().getServer() != null) {
            NexusData.get(event.getEntity().getServer()).setScytheCrafted(true);
        }
    }

    /** Les trajets par le portail partent à la fin du tick, après le traitement des paquets des joueurs. */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            NexusTravel.runPending();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide
                || !(event.player instanceof ServerPlayer player) || !NexusDimension.isNexus(player.level())
                || player.getY() >= VOID_RESCUE_Y) {
            return;
        }
        NexusDimension.teleportToHub(player, (ServerLevel) player.level());
        Notify.send(player, "message.astralpack.nexus.rescued", Notify.Kind.INFO);
    }
}
