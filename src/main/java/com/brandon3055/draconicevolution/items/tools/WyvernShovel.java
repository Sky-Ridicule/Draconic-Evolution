package com.brandon3055.draconicevolution.items.tools;

import com.brandon3055.brandonscore.lib.PairKV;
import com.brandon3055.draconicevolution.api.itemconfig.ToolConfigHelper;
import com.brandon3055.draconicevolution.utils.DETextures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;

/**
 * Created by brandon3055 on 2/06/2016.
 */
public class WyvernShovel extends MiningToolBase {

    public WyvernShovel(Properties properties) {
        super(properties, MiningToolBase.SHOVEL_OVERRIDES);
    }

    //    public WyvernShovel(/*double attackDamage, double attackSpeed, */Set<Block> effectiveBlocks) {
//        super(/*attackDamage, attackSpeed, */effectiveBlocks);
//    }
//
//    public WyvernShovel() {
//        super(/*ToolStats.WYV_SHOVEL_ATTACK_DAMAGE, ToolStats.WYV_SHOVEL_ATTACK_SPEED, */SHOVEL_OVERRIDES);
////        this.baseMiningSpeed = (float) ToolStats.WYV_SHOVEL_MINING_SPEED;
////        this.baseAOE = ToolStats.BASE_WYVERN_MINING_AOE;
////        setEnergyStats(ToolStats.WYVERN_BASE_CAPACITY, 512000, 0);
//        this.setHarvestLevel("shovel", 10);
//    }

    @Override
    public double getBaseMinSpeedConfig() {
        return ToolStats.WYV_SHOVEL_MINING_SPEED;
    }

    @Override
    public int getBaseMinAOEConfig() {
        return ToolStats.BASE_WYVERN_MINING_AOE;
    }

    @Override
    public double getBaseAttackSpeedConfig() {
        return ToolStats.WYV_SHOVEL_ATTACK_SPEED;
    }

    @Override
    public double getBaseAttackDamageConfig() {
        return ToolStats.WYV_SHOVEL_ATTACK_DAMAGE;
    }

    @Override
    public void loadEnergyStats() {
        setEnergyStats(ToolStats.WYVERN_BASE_CAPACITY, 512000, 0);
    }

    @Override
    public int getMaxUpgradeLevel(ItemStack stack, String upgrade) {
        return 2;
    }

    @Override
    public int getToolTier(ItemStack stack) {
        return 0;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();

        if (!flattenBlock(stack, player, world, pos, facing)) {
            if (world.getBlockState(pos).getBlock() != Blocks.GRASS_PATH) {
                return ActionResultType.FAIL;
            }
        }
        else {
            modifyEnergy(stack, -energyPerOperation);
        }

        if (player.isSneaking()) {
            return ActionResultType.SUCCESS;
        }

        int AOE = ToolConfigHelper.getIntegerField("digAOE", stack);

        Iterable<BlockPos> blocks = BlockPos.getAllInBoxMutable(pos.add(-AOE, 0, -AOE), pos.add(AOE, 0, AOE));

        for (BlockPos aoePos : blocks) {
            if (aoePos.equals(pos)) {
                continue;
            }

            BlockState repState = world.getBlockState(aoePos.up());
            boolean replaceable = repState.getMaterial().isReplaceable();
            if (world.isAirBlock(aoePos) || !replaceable) {
                continue;
            }

            if (flattenBlock(stack, player, world, aoePos, facing)) {
                modifyEnergy(stack, -energyPerOperation);
            }
        }
        return ActionResultType.SUCCESS;
    }

    private boolean flattenBlock(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction face) {
        if (getEnergyStored(stack) < energyPerOperation && !player.abilities.isCreativeMode) {
            return false;
        }
        else if (!player.canPlayerEdit(pos, face, stack)) {
            return false;
        }
        else {
            BlockState iblockstate = world.getBlockState(pos);
            Block block = iblockstate.getBlock();

            if (face != Direction.DOWN && block == Blocks.GRASS) {
                world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

                if (!world.isRemote) {
                    if (!world.isAirBlock(pos.up())) {
                        world.removeBlock(pos.up(), false);
                    }
                    setBlock(player, world, pos, Blocks.GRASS_PATH.getDefaultState());
                }

                return true;
            }

            return false;
        }
    }

    protected void setBlock(PlayerEntity player, World worldIn, BlockPos pos, BlockState state) {
        worldIn.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, state, 11);
        }
    }

    //region Rendering

    @Override
    public PairKV<TextureAtlasSprite, ResourceLocation> getModels(ItemStack stack) {
        return new PairKV<>(DETextures.WYVERN_SHOVEL, new ResourceLocation("draconicevolution", "models/item/tools/wyvern_shovel.obj"));
    }

    //endregion
}
