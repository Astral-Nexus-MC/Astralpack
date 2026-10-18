package fr.astralnexus.astralpack.item;

import fr.astralnexus.astralpack.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Clé à molette : un clic droit sur un spawner à monstres le décroche sans le casser. Le spawner devient un objet
 * ({@link MovedSpawnerItem}) qui garde son monstre et ses réglages, et se repose ailleurs tel quel.
 */
public class SpawnerWrenchItem extends Item {
    public SpawnerWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (!level.getBlockState(pos).is(Blocks.SPAWNER)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack tool = context.getItemInHand();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), tool)
                || !level.mayInteract(player, pos)) {
            return InteractionResult.FAIL;
        }
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof SpawnerBlockEntity spawner)) {
            return InteractionResult.FAIL;
        }
        CompoundTag data = spawner.saveWithoutMetadata();
        ItemStack moved = new ItemStack(ModItems.MOVED_SPAWNER.get());
        moved.getOrCreateTag().put(MovedSpawnerItem.DATA_TAG, data);
        level.removeBlockEntity(pos);
        level.removeBlock(pos, false);
        if (!player.getInventory().add(moved)) {
            player.drop(moved, false);
        }
        ServerLevel server = (ServerLevel) level;
        server.playSound(null, pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 1.0F, 0.8F);
        server.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 20, 0.4D,
                0.4D, 0.4D, 0.1D);
        tool.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(context.getHand()));
        return InteractionResult.CONSUME;
    }

    /** Irréparable : ni enclume, ni meule, ni Raccommodage ; elle se casse après ses dix utilisations. */
    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.world.item.enchantment.Enchantment enchantment) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.astralpack.spawner_wrench.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.astralpack.spawner_wrench.unrepairable").withStyle(ChatFormatting.DARK_GRAY));
    }
}
