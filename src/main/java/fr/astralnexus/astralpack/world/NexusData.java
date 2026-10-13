package fr.astralnexus.astralpack.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Données du Nexus enregistrées dans le monde principal : la Death Scythe n'existe qu'une fois par serveur, et le
 * portail de retour du hub n'est généré qu'une fois.
 */
public class NexusData extends SavedData {
    private static final String NAME = "astralpack_nexus";

    private boolean scytheCrafted;
    private boolean hasReturnGate;
    private long returnGateCenter;

    public static NexusData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(NexusData::load, NexusData::new, NAME);
    }

    private static NexusData load(CompoundTag tag) {
        NexusData data = new NexusData();
        data.scytheCrafted = tag.getBoolean("ScytheCrafted");
        data.hasReturnGate = tag.getBoolean("HasReturnGate");
        data.returnGateCenter = tag.getLong("ReturnGateCenter");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("ScytheCrafted", this.scytheCrafted);
        tag.putBoolean("HasReturnGate", this.hasReturnGate);
        tag.putLong("ReturnGateCenter", this.returnGateCenter);
        return tag;
    }

    public boolean isScytheCrafted() {
        return this.scytheCrafted;
    }

    public void setScytheCrafted(boolean crafted) {
        this.scytheCrafted = crafted;
        this.setDirty();
    }

    /** Centre du portail de retour du hub, ou {@code null} s'il n'a pas encore été généré. */
    public BlockPos getReturnGate() {
        return this.hasReturnGate ? BlockPos.of(this.returnGateCenter) : null;
    }

    public void setReturnGate(BlockPos center) {
        this.hasReturnGate = true;
        this.returnGateCenter = center.asLong();
        this.setDirty();
    }
}
