package fr.astralnexus.astralpack.block.entity;

import fr.astralnexus.astralpack.block.NexusConsoleBlock;
import fr.astralnexus.astralpack.item.DeathScytheItem;
import fr.astralnexus.astralpack.network.Notify;
import fr.astralnexus.astralpack.registry.ModBlockEntities;
import fr.astralnexus.astralpack.world.NexusData;
import fr.astralnexus.astralpack.world.NexusDimension;
import fr.astralnexus.astralpack.world.NexusRing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Mémoire du pupitre : la Death Scythe plantée, l'anneau qu'elle alimente et l'état du portail. Le portail est ouvert
 * tant que la faux est plantée et que l'anneau est complet.
 */
public class NexusConsoleBlockEntity extends BlockEntity {
    /** Intervalle, en ticks, entre deux contrôles de l'anneau. */
    private static final int CHECK_INTERVAL = 40;
    /** Durée, en ticks, de la gerbe d'ouverture du portail. */
    private static final int KAWOOSH_TICKS = 16;

    private ItemStack scythe = ItemStack.EMPTY;
    private boolean open;
    private BlockPos ringCenter = BlockPos.ZERO;
    private Direction.Axis ringAxis = Direction.Axis.X;
    /** Ticks restants de la gerbe d'ouverture (non enregistré : une réouverture au chargement n'en rejoue pas). */
    private int openingTicks;

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
            Notify.send(player, "message.astralpack.nexus.console.wrong_dimension", Notify.Kind.WARNING);
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!this.scythe.isEmpty()) {
            if (held.isEmpty()) {
                this.take(player);
            } else {
                if (this.open) {
                Notify.send(player, "message.astralpack.nexus.console.status_open", Notify.Kind.INFO);
            } else {
                Notify.send(player, "message.astralpack.nexus.console.status_closed", Notify.Kind.WARNING);
            }
            }
            return;
        }
        if (!(held.getItem() instanceof DeathScytheItem)) {
            Notify.send(player, "message.astralpack.nexus.console.needs_scythe", Notify.Kind.INFO);
            return;
        }
        Optional<NexusRing.Found> ring = NexusRing.find(level, this.worldPosition);
        if (ring.isEmpty()) {
            Notify.send(player, "message.astralpack.nexus.console.no_ring", Notify.Kind.WARNING);
            return;
        }
        this.scythe = held.copy();
        held.shrink(1);
        this.openPortal(ring.get());
        this.setPlanted(true);
        this.sync();
        level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        Notify.send(player, "message.astralpack.nexus.console.opened", Notify.Kind.SUCCESS);
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
        Notify.send(player, "message.astralpack.nexus.console.closed", Notify.Kind.INFO);
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
        // Le trajet de retour se fait devant ce pupitre : on retient lequel, et comment il est orienté.
        NexusData.get(this.level.getServer()).setActivePortal(this.level.dimension().location().toString(),
                this.worldPosition, this.getBlockState().getValue(NexusConsoleBlock.FACING).get2DDataValue(),
                this.ringCenter);
        this.openingTicks = KAWOOSH_TICKS;
        Vec3 center = Vec3.atCenterOf(this.ringCenter);
        this.level.playSound(null, center.x, center.y, center.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS,
                1.2F, 1.5F);
    }

    private void closePortal() {
        if (this.open) {
            NexusRing.close(this.level, this.ringCenter, this.ringAxis);
            this.open = false;
            this.openingTicks = 0;
            NexusData.get(this.level.getServer()).clearActivePortal();
        }
    }

    /**
     * Ouverture façon Stargate : une gerbe de lumière jaillit de la surface, de chaque côté de l'anneau, gonfle puis
     * retombe en une seconde environ.
     */
    private void kawoosh(ServerLevel server) {
        int step = KAWOOSH_TICKS - this.openingTicks;
        double radius = 4.8D * Math.sin(Math.PI * step / (KAWOOSH_TICKS - 1.0D));
        if (radius < 0.3D) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(this.ringCenter);
        boolean alongX = this.ringAxis == Direction.Axis.X;
        double spread = radius * 0.5D;
        for (int side = -1; side <= 1; side += 2) {
            double offset = side * radius * 0.9D;
            double x = center.x + (alongX ? 0.0D : offset);
            double z = center.z + (alongX ? offset : 0.0D);
            double dx = alongX ? spread : 0.25D;
            double dz = alongX ? 0.25D : spread;
            server.sendParticles(ParticleTypes.END_ROD, x, center.y, z, 14, dx, spread, dz, 0.01D);
            server.sendParticles(ParticleTypes.REVERSE_PORTAL, x, center.y, z, 8, dx, spread, dz, 0.05D);
        }
    }

    private void setPlanted(boolean planted) {
        BlockState state = this.getBlockState();
        if (state.getValue(NexusConsoleBlock.PLANTED) != planted) {
            this.level.setBlock(this.worldPosition, state.setValue(NexusConsoleBlock.PLANTED, planted), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NexusConsoleBlockEntity console) {
        if (console.openingTicks > 0 && level instanceof ServerLevel serverLevel) {
            console.kawoosh(serverLevel);
            console.openingTicks--;
        }
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
