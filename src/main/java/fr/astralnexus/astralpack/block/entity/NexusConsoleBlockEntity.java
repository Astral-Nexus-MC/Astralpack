package fr.astralnexus.astralpack.block.entity;

import fr.astralnexus.astralpack.block.NexusConsoleBlock;
import fr.astralnexus.astralpack.item.DeathScytheItem;
import fr.astralnexus.astralpack.registry.ModBlockEntities;
import fr.astralnexus.astralpack.world.NexusDimension;
import fr.astralnexus.astralpack.world.NexusRing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

/**
 * Mémoire du pupitre : la Death Scythe plantée, l'anneau qu'elle alimente et l'état du portail. Le portail est ouvert
 * tant que la faux est plantée et que l'anneau est complet.
 */
public class NexusConsoleBlockEntity extends BlockEntity {
    /** Intervalle, en ticks, entre deux contrôles de l'anneau. */
    private static final int CHECK_INTERVAL = 40;

    private ItemStack scythe = ItemStack.EMPTY;
    private boolean open;
    private BlockPos ringCenter = BlockPos.ZERO;
    private Direction.Axis ringAxis = Direction.Axis.X;

    public NexusConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEXUS_CONSOLE.get(), pos, state);
    }

    /** La faux plantée, affichée au-dessus du pupitre par le client. */
    public ItemStack getScythe() {
        return this.scythe;
    }

    /** Envoie l'état du pupitre (dont la faux) aux clients qui le voient. */
    private void sync() {
        if (this.level != null && !this.level.isClientSide) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /** La faux dépasse largement du bloc : sans cela, elle disparaîtrait dès que le pupitre sort du champ. */
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(2.0D, 0.0D, 2.0D).expandTowards(0.0D, 4.0D, 0.0D);
    }

    /** Clic droit : planter la faux, ou la reprendre à main vide. */
    public void interact(Player player) {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }
        if (NexusDimension.isNexus(level)) {
            player.displayClientMessage(Component.translatable("message.astralpack.nexus.console.wrong_dimension"), true);
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!this.scythe.isEmpty()) {
            if (held.isEmpty()) {
                this.take(player);
            } else {
                player.displayClientMessage(Component.translatable(this.open
                        ? "message.astralpack.nexus.console.status_open"
                        : "message.astralpack.nexus.console.status_closed"), true);
            }
            return;
        }
        if (!(held.getItem() instanceof DeathScytheItem)) {
            player.displayClientMessage(Component.translatable("message.astralpack.nexus.console.needs_scythe"), true);
            return;
        }
        Optional<NexusRing.Found> ring = NexusRing.find(level, this.worldPosition);
        if (ring.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.astralpack.nexus.console.no_ring"), true);
            return;
        }
        this.scythe = held.copy();
        held.shrink(1);
        this.openPortal(ring.get());
        this.setPlanted(true);
        this.sync();
        level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.astralpack.nexus.console.opened"), true);
        this.setChanged();
    }

    private void take(Player player) {
        this.closePortal();
        ItemStack back = this.scythe;
        this.scythe = ItemStack.EMPTY;
        if (!player.getInventory().add(back)) {
            player.drop(back, false);
        }
        this.setPlanted(false);
        this.sync();
        this.level.playSound(null, this.worldPosition, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.astralpack.nexus.console.closed"), true);
        this.setChanged();
    }

    /** Pupitre cassé : le portail se ferme et la faux est rendue. */
    public void onBroken() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        this.closePortal();
        if (!this.scythe.isEmpty()) {
            Containers.dropItemStack(this.level, this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
                    this.worldPosition.getZ() + 0.5D, this.scythe);
            this.scythe = ItemStack.EMPTY;
        }
    }

    private void openPortal(NexusRing.Found ring) {
        this.ringCenter = ring.center();
        this.ringAxis = ring.axis();
        NexusRing.open(this.level, this.ringCenter, this.ringAxis);
        this.open = true;
    }

    private void closePortal() {
        if (this.open) {
            NexusRing.close(this.level, this.ringCenter, this.ringAxis);
            this.open = false;
        }
    }

    private void setPlanted(boolean planted) {
        BlockState state = this.getBlockState();
        if (state.getValue(NexusConsoleBlock.PLANTED) != planted) {
            this.level.setBlock(this.worldPosition, state.setValue(NexusConsoleBlock.PLANTED, planted), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NexusConsoleBlockEntity console) {
        if (console.scythe.isEmpty() || level.getGameTime() % CHECK_INTERVAL != 0) {
            return;
        }
        if (console.open) {
            if (!NexusRing.isComplete(level, console.ringCenter, console.ringAxis)) {
                console.closePortal();
                console.setChanged();
            }
        } else {
            NexusRing.find(level, pos).ifPresent(ring -> {
                console.openPortal(ring);
                console.setChanged();
            });
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Scythe", this.scythe.save(new CompoundTag()));
        tag.putBoolean("Open", this.open);
        tag.putLong("RingCenter", this.ringCenter.asLong());
        tag.putBoolean("RingAxisX", this.ringAxis == Direction.Axis.X);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.scythe = ItemStack.of(tag.getCompound("Scythe"));
        this.open = tag.getBoolean("Open");
        this.ringCenter = BlockPos.of(tag.getLong("RingCenter"));
        this.ringAxis = tag.getBoolean("RingAxisX") ? Direction.Axis.X : Direction.Axis.Z;
    }
}
