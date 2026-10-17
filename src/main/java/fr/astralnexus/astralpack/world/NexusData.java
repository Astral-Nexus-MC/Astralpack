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
    /** Le portail actuellement ouvert (une seule faux, donc un seul portail à la fois) : pupitre et anneau. */
    private boolean portalActive;
    private String portalDimension = "";
    private long portalConsole;
    private int portalConsoleFacing;
    /** Construction du hub : version du plan, étape atteinte, hauteur de la place. */
    private int hubKey;
    private int hubStage;
    private int hubY;
    private int hubHomes;
    private int hubRole;
    private long portalCenter;

    public static NexusData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(NexusData::load, NexusData::new, NAME);
    }

    private static NexusData load(CompoundTag tag) {
        NexusData data = new NexusData();
        data.scytheCrafted = tag.getBoolean("ScytheCrafted");
        data.hasReturnGate = tag.getBoolean("HasReturnGate");
        data.returnGateCenter = tag.getLong("ReturnGateCenter");
        data.portalActive = tag.getBoolean("PortalActive");
        data.portalDimension = tag.getString("PortalDimension");
        data.portalConsole = tag.getLong("PortalConsole");
        data.portalConsoleFacing = tag.getInt("PortalConsoleFacing");
        data.portalCenter = tag.getLong("PortalCenter");
        data.hubKey = tag.getInt("HubKey");
        data.hubStage = tag.getInt("HubStage");
        data.hubY = tag.getInt("HubY");
        data.hubHomes = tag.getInt("HubHomes");
        data.hubRole = tag.getInt("HubRole");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("ScytheCrafted", this.scytheCrafted);
        tag.putBoolean("HasReturnGate", this.hasReturnGate);
        tag.putLong("ReturnGateCenter", this.returnGateCenter);
        tag.putBoolean("PortalActive", this.portalActive);
        tag.putString("PortalDimension", this.portalDimension);
        tag.putLong("PortalConsole", this.portalConsole);
        tag.putInt("PortalConsoleFacing", this.portalConsoleFacing);
        tag.putLong("PortalCenter", this.portalCenter);
        tag.putInt("HubKey", this.hubKey);
        tag.putInt("HubStage", this.hubStage);
        tag.putInt("HubY", this.hubY);
        tag.putInt("HubHomes", this.hubHomes);
        tag.putInt("HubRole", this.hubRole);
        return tag;
    }

    public boolean isScytheCrafted() {
        return this.scytheCrafted;
    }

    public void setScytheCrafted(boolean crafted) {
        this.scytheCrafted = crafted;
        this.setDirty();
    }

    /** Mémorise le portail qui vient de s'ouvrir : pupitre (avec son orientation) et centre de l'anneau. */
    public void setActivePortal(String dimension, BlockPos console, int consoleFacing, BlockPos ringCenter) {
        this.portalActive = true;
        this.portalDimension = dimension;
        this.portalConsole = console.asLong();
        this.portalConsoleFacing = consoleFacing;
        this.portalCenter = ringCenter.asLong();
        this.setDirty();
    }

    public void clearActivePortal() {
        if (this.portalActive) {
            this.portalActive = false;
            this.setDirty();
        }
    }

    public boolean hasActivePortal() {
        return this.portalActive;
    }

    public String getPortalDimension() {
        return this.portalDimension;
    }

    public BlockPos getPortalConsole() {
        return BlockPos.of(this.portalConsole);
    }

    /** Orientation du pupitre : indice de {@code Direction.from2DDataValue}. */
    public int getPortalConsoleFacing() {
        return this.portalConsoleFacing;
    }

    public BlockPos getPortalCenter() {
        return BlockPos.of(this.portalCenter);
    }

    /** Centre du portail de retour du hub, ou {@code null} s'il n'a pas encore été généré. */
    public BlockPos getReturnGate() {
        return this.hasReturnGate ? BlockPos.of(this.returnGateCenter) : null;
    }

    /** Oublie la position du portail de retour : il sera reconstruit à la prochaine arrivée, à la hauteur du sol. */
    public void clearReturnGate() {
        this.hasReturnGate = false;
        this.setDirty();
    }

    public void setReturnGate(BlockPos center) {
        this.hasReturnGate = true;
        this.returnGateCenter = center.asLong();
        this.setDirty();
    }

    /** Un plan ou un terrain différent de celui déjà construit : la construction du hub repart de zéro. */
    public void syncHubKey(int key) {
        if (this.hubKey != key) {
            this.hubKey = key;
            this.hubStage = 0;
            this.hubHomes = 0;
            this.hubRole = 0;
            this.setDirty();
        }
    }

    public int getHubStage() {
        return this.hubStage;
    }

    public void setHubStage(int stage) {
        if (stage == 0) {
            this.hubHomes = 0;
            this.hubRole = 0;
        }
        this.hubStage = stage;
        this.setDirty();
    }

    public void setHubY(int y) {
        this.hubY = y;
        this.setDirty();
    }

    /** Nombre de maisons de joueurs construites pendant la construction du hub en cours. */
    public int getHomes() {
        return this.hubHomes;
    }

    public void addHome() {
        this.hubHomes++;
        this.setDirty();
    }

    /** Numéro du prochain bâtiment du village à construire. */
    public int nextRole() {
        int role = this.hubRole++;
        this.setDirty();
        return role;
    }
}
