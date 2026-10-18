package fr.astralnexus.astralpack.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Spawner décroché avec la clé à molette. C'est un bloc de spawner qui garde ses réglages (monstre, délais, portée) : à
 * la pose, ils sont rechargés dans le nouveau spawner, ce que l'objet vanilla ne permet pas aux joueurs.
 */
public class MovedSpawnerItem extends BlockItem {
    public static final String DATA_TAG = "SpawnerData";

    public MovedSpawnerItem(Properties properties) {
        super(Blocks.SPAWNER, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack,
                                                 BlockState state) {
        CompoundTag data = stack.getTagElement(DATA_TAG);
        if (data == null) {
            return false;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.load(data.copy());
            spawner.getPersistentData().putBoolean(fr.astralnexus.astralpack.spawner.SpawnerEvents.STACKING, true);
            spawner.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        return true;
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag data = stack.getTagElement(DATA_TAG);
        Component mob = Component.translatable("item.astralpack.moved_spawner.empty");
        if (data != null) {
            String id = data.getCompound("SpawnData").getCompound("entity").getString("id");
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key != null && ForgeRegistries.ENTITY_TYPES.containsKey(key)) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(key);
                mob = type.getDescription();
            }
        }
        tooltip.add(Component.translatable("item.astralpack.moved_spawner.mob", mob).withStyle(ChatFormatting.GRAY));
    }
}
