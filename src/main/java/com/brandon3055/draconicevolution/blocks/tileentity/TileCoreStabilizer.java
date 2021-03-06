package com.brandon3055.draconicevolution.blocks.tileentity;


import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.lib.Vec3I;
import com.brandon3055.brandonscore.lib.datamanager.ManagedBool;
import com.brandon3055.brandonscore.lib.datamanager.ManagedVec3I;
import com.brandon3055.draconicevolution.DEContent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.brandon3055.brandonscore.lib.datamanager.DataFlags.SAVE_NBT_SYNC_TILE;
import static com.brandon3055.brandonscore.lib.datamanager.DataFlags.TRIGGER_UPDATE;

/**
 * Created by brandon3055 on 30/3/2016.
 */
public class TileCoreStabilizer extends TileBCore/* implements ITickableTileEntity, IMultiBlockPart, IMovableStructure*/ {

    public TileCoreStabilizer() {
        super(DEContent.tile_core_stabilizer);
    }

    public final ManagedVec3I coreOffset = register(new ManagedVec3I("core_offset", new Vec3I(0, -1, 0), SAVE_NBT_SYNC_TILE));
    public final ManagedBool hasCoreLock = register(new ManagedBool("has_core_lock", SAVE_NBT_SYNC_TILE));
    public final ManagedBool isCoreActive = register(new ManagedBool("is_core_active", SAVE_NBT_SYNC_TILE));
    public final ManagedBool isValidMultiBlock = register(new ManagedBool("is_valid_multi_block", SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public Direction.Axis multiBlockAxis = Direction.Axis.Y;
    public Direction coreDirection = Direction.DOWN;
    public float rotation = 0;
    public float rotationSpeed = 0;
    private boolean moveCheckComplete = false;

//    //region Beam
//
//    @Override
//    public void update() {
//        super.update();
//        if (world.isRemote && hasCoreLock.get() && isCoreActive.get()) {
//            rotation = ClientEventHandler.elapsedTicks;
//            updateVisual();
////            if (isValidMultiBlock.value) {
////                updateVisual();
////            }
//        }
//    }
//
//    @OnlyIn(Dist.CLIENT)
//    private void updateVisual() {
//        Vec3D spawn = new Vec3D(pos);
//        spawn.add(0.5, 0.5, 0.5);
//        double rand = world.rand.nextInt(100) / 12D;
//        double randOffset = rand * (Math.PI * 2D);
//        double offsetX = Math.sin((ClientEventHandler.elapsedTicks / 180D * Math.PI) + randOffset);
//        double offsetY = Math.cos((ClientEventHandler.elapsedTicks / 180D * Math.PI) + randOffset);
//
//        if (!isValidMultiBlock.get() || world.rand.nextBoolean()) {
//            double d = isValidMultiBlock.get() ? 1.1 : 0.25;
//            double inset = isValidMultiBlock.get() ? 1 : 0;
//            if (coreDirection.getAxis() == Direction.Axis.Z) {
//                spawn.add(offsetX * d, offsetY * d, (world.rand.nextBoolean() ? -0.38 : 0.38) * inset);
//            } else if (coreDirection.getAxis() == Direction.Axis.Y) {
//                spawn.add(offsetX * d, (world.rand.nextBoolean() ? -0.38 : 0.38) * inset, offsetY * d);
//            } else if (coreDirection.getAxis() == Direction.Axis.X) {
//                spawn.add((world.rand.nextBoolean() ? -0.38 : 0.38) * inset, offsetY * d, offsetX * d);
//            }
//            BCEffectHandler.spawnFX(DEParticles.ENERGY_CORE_FX, world, spawn, new Vec3D(pos).subtract(coreOffset.get().getPos()).add(0.5, 0.5, 0.5), 1, (int) (randOffset * 100D), isValidMultiBlock.get() ? 1 : 0);
//        } else {
//            if (coreDirection.getAxis() == Direction.Axis.Z) {
//                spawn.add(offsetX * 1.2, offsetY * 1.2, world.rand.nextBoolean() ? -0.38 : 0.38);
//            } else if (coreDirection.getAxis() == Direction.Axis.Y) {
//                spawn.add(offsetX * 1.2, world.rand.nextBoolean() ? -0.38 : 0.38, offsetY * 1.2);
//            } else if (coreDirection.getAxis() == Direction.Axis.X) {
//                spawn.add(world.rand.nextBoolean() ? -0.38 : 0.38, offsetY * 1.2, offsetX * 1.2);
//            }
//            BCEffectHandler.spawnFX(DEParticles.ENERGY_CORE_FX, world, spawn, new Vec3D(pos).add(0.5, 0.5, 0.5), 0);
//        }
//    }
//
//    //endregion
//
//    //region Activation
//
    public void onTileClicked(World world, BlockPos pos, BlockState state, PlayerEntity player) {
//        if (world.isRemote) return;
//
//        TileEnergyStorageCore core = getCore();
//        if (core == null) {
//            core = findCore();
//        }
//
//        if (core != null) {
//            core.onStructureClicked(world, pos, state, player);
//        } else {
//            player.sendMessage(new TextComponentTranslation("msg.de.coreNotFound.txt").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
//        }
    }

    public boolean isStabilizerValid(int coreTier, TileStorageCore core) {
        if (coreTier < 5 && !isValidMultiBlock.get()) {
            return true;
        } else if (coreTier >= 5 && isValidMultiBlock.get()) {
            BlockPos offset = pos.subtract(core.getPos());
            Direction direction = Direction.getFacingFromVector(offset.getX(), offset.getY(), offset.getZ()).getOpposite();
            return direction.getAxis() == multiBlockAxis;
        }
        return false;
    }

//    //endregion
//
//    //region MultiBlock
//
//    public void onPlaced() {
//        if (world.isRemote || checkAndFormMultiBlock()) {
//            return;
//        }
//
//        for (Direction facing1 : Direction.values()) {
//            BlockPos search = pos.add(facing1.getFrontOffsetX(), facing1.getFrontOffsetY(), facing1.getFrontOffsetZ());
//
//            TileEntity stabilizer = world.getTileEntity(search);
//
//            if (stabilizer instanceof TileEnergyCoreStabilizer && ((TileEnergyCoreStabilizer) stabilizer).checkAndFormMultiBlock()) {
//                return;
//            }
//
//            for (Direction facing2 : Direction.values()) {
//                if (facing2 != facing1 && facing2 != facing1.getOpposite()) {
//                    BlockPos s2 = search.add(facing2.getFrontOffsetX(), facing2.getFrontOffsetY(), facing2.getFrontOffsetZ());
//                    stabilizer = world.getTileEntity(s2);
//
//                    if (stabilizer instanceof TileEnergyCoreStabilizer && ((TileEnergyCoreStabilizer) stabilizer).checkAndFormMultiBlock()) {
//                        return;
//                    }
//                }
//            }
//        }
//    }
//
//    private boolean checkAxisValid(Direction.Axis axis) {
//        for (BlockPos offset : FacingUtils.getAroundAxis(axis)) {
//            if (!isAvailable(pos.add(offset))) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    /**
//     * Checks if this block is at the center of a valid multiblock and if so activates the structure.
//     *
//     * @return true if structure was activated.
//     */
//    private boolean checkAndFormMultiBlock() {
//        if (hasCoreLock.get() && getCore() != null && getCore().active.get()) {
//            return false;
//        }
//
//        for (Direction.Axis axis : Direction.Axis.values()) {
//            if (checkAxisValid(axis)) {
//                buildMultiBlock(axis);
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    /**
//     * @return true if there is a stabilizer at the given pos and it is available for use in a structure.
//     * If structure is already formed will check if the block is an invisible tile with this as its master
//     * In the case of the structure already formed this should be called from the controller.
//     */
//    private boolean isAvailable(BlockPos pos) {
//        if (isValidMultiBlock.get()) {
//            TileEntity tile = world.getTileEntity(pos);
//            return tile instanceof TileInvisECoreBlock && ((TileInvisECoreBlock) tile).getController() == this;
//        }
//
//        TileEntity stabilizer = world.getTileEntity(pos);
//        return stabilizer instanceof TileEnergyCoreStabilizer && (!((TileEnergyCoreStabilizer) stabilizer).hasCoreLock.get() || ((TileEnergyCoreStabilizer) stabilizer).getCore() == null || !((TileEnergyCoreStabilizer) stabilizer).getCore().active.get());
//    }
//
//    private void buildMultiBlock(Direction.Axis axis) {
//        world.setBlockState(pos, world.getBlockState(pos).withProperty(ParticleGenerator.TYPE, "stabilizer2"));
//
//        for (BlockPos offset : FacingUtils.getAroundAxis(axis)) {
//            world.setBlockState(pos.add(offset), DEFeatures.invisECoreBlock.getDefaultState());
//            TileEntity tile = world.getTileEntity(pos.add(offset));
//
//            if (tile instanceof TileInvisECoreBlock) {
//                ((TileInvisECoreBlock) tile).blockName = "draconicevolution:particle_generator";
//                ((TileInvisECoreBlock) tile).setController(this);
//            }
//        }
//
//        isValidMultiBlock.set(true);
//        multiBlockAxis = axis;
//    }
//
//    public void deFormStructure() {
//        isValidMultiBlock.set(false);
//        if (world.getBlockState(pos).getBlock() == DEFeatures.particleGenerator) {
//            world.setBlockState(pos, DEFeatures.particleGenerator.getDefaultState().withProperty(ParticleGenerator.TYPE, "stabilizer"));
//        }
//
//        if (getCore() != null) {
//            getCore().deactivateCore();
//        }
//
//        for (BlockPos offset : FacingUtils.getAroundAxis(multiBlockAxis)) {
//            TileEntity tile = world.getTileEntity(pos.add(offset));
//            if (tile instanceof TileInvisECoreBlock) {
//                ((TileInvisECoreBlock) tile).revert();
//            }
//        }
//    }
//
//    @Override
//    public boolean validateStructure() {
//        if (checkAxisValid(multiBlockAxis)) {
//            return true;
//        }
//
//        deFormStructure();
//
//        return false;
//    }
//
//    //region Unused IMultiBlock
//
//    @Override
//    public boolean isStructureValid() {
//        return isValidMultiBlock.get();
//    }
//
//    @Override
//    public IMultiBlockPart getController() {
//        return this;
//    }
//
//    //endregion
//
//    //endregion
//
//    //region Getters & Setters
//
//    public TileEnergyStorageCore findCore() {
//        if (getCore() != null) {
//            return getCore();
//        }
//
//        for (Direction facing : Direction.values()) {
//            for (int i = 0; i < 16; i++) {
//                TileEntity tile = world.getTileEntity(pos.add(facing.getFrontOffsetX() * i, facing.getFrontOffsetY() * i, facing.getFrontOffsetZ() * i));
//                if (tile instanceof TileEnergyStorageCore) {
//                    TileEnergyStorageCore core = (TileEnergyStorageCore) tile;
//                    core.validateStructure();
//                    if (core.active.get()) {
//                        continue;
//                    }
//                    return core;
//                }
//            }
//        }
//
//        return null;
//    }
//
    public TileStorageCore getCore() {
        if (hasCoreLock.get()) {
            TileEntity tile = world.getTileEntity(getCorePos());
            if (tile instanceof TileStorageCore) {
                return (TileStorageCore) tile;
            } else {
                hasCoreLock.set(false);
            }
        }
        return null;
    }

    private BlockPos getCorePos() {
        return pos.subtract(coreOffset.get().getPos());
    }

    public void setCore(TileStorageCore core) {
        BlockPos offset = pos.subtract(core.getPos());
        coreOffset.set(new Vec3I(offset));
        hasCoreLock.set(true);
        coreDirection = Direction.getFacingFromVector(offset.getX(), offset.getY(), offset.getZ()).getOpposite();
        updateBlock();
    }

//    //endregion
//
//    //region Save
//
//    @Override
//    public SPacketUpdateTileEntity getUpdatePacket() {
//        SPacketUpdateTileEntity packet = (SPacketUpdateTileEntity) super.getUpdatePacket();
//        CompoundNBT compound = packet.nbt;
//        compound.putByte("StructureAxis", (byte) multiBlockAxis.ordinal());
//        compound.putByte("CoreDirection", (byte) coreDirection.getIndex());
//        return packet;
//    }
//
//    @Override
//    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
//        super.onDataPacket(net, pkt);
//        CompoundNBT compound = pkt.getNbtCompound();
//
//        Direction.Axis[] values = Direction.Axis.values();
//        int i = compound.getByte("StructureAxis");
//        multiBlockAxis = i >= 0 && i < values.length ? values[i] : Direction.Axis.Y;
//        coreDirection = Direction.getFront(compound.getByte("CoreDirection"));
//    }
//
//    @Override
//    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newSate) {
//        if (oldState.getBlock() != newSate.getBlock()) {
//            return true;
//        }
//
//        boolean wasStab = oldState.getValue(ParticleGenerator.TYPE).endsWith("stabilizer") || oldState.getValue(ParticleGenerator.TYPE).endsWith("stabilizer2");
//        boolean isStab = newSate.getValue(ParticleGenerator.TYPE).endsWith("stabilizer") || newSate.getValue(ParticleGenerator.TYPE).endsWith("stabilizer2");
//
//        return wasStab != isStab;
//    }
//
//    @Override
//    public void writeExtraNBT(CompoundNBT compound) {
//        if (multiBlockAxis != null) {
//            compound.putByte("StructureAxis", (byte) multiBlockAxis.ordinal());
//            compound.putByte("CoreDirection", (byte) coreDirection.getIndex());
//        }
//    }
//
//    @Override
//    public void readExtraNBT(CompoundNBT compound) {
//        Direction.Axis[] values = Direction.Axis.values();
//        int i = compound.getByte("StructureAxis");
//        multiBlockAxis = i >= 0 && i < values.length ? values[i] : Direction.Axis.Y;
//        coreDirection = Direction.getFront(compound.getByte("CoreDirection"));
//    }
//
//    //endregion
//
//    //Frame Movement
//
//    private Set<BlockPos> getStabilizerBlocks() {
//        Set<BlockPos> blocks = new HashSet<>();
//        blocks.add(pos);
//        if (isValidMultiBlock.get()) {
//            for (BlockPos offset : FacingUtils.getAroundAxis(multiBlockAxis)) {
//                blocks.add(pos.add(offset));
//            }
//        }
//
//        return blocks;
//    }
//
//    @Override
//    public Iterable<BlockPos> getBlocksForFrameMove() {
//        TileEnergyStorageCore core = getCore();
//        if (core != null && !core.moveBlocksProvided) {
//            HashSet<BlockPos> blocks = new HashSet<>();
//
//            for (ManagedVec3I offset : core.stabOffsets) {
//                BlockPos stabPos = core.getPos().subtract(offset.get().getPos());
//                TileEntity tile = world.getTileEntity(stabPos);
//                if (tile instanceof TileEnergyCoreStabilizer) {
//                    blocks.addAll(((TileEnergyCoreStabilizer) tile).getStabilizerBlocks());
//                }
//            }
//
//            EnergyCoreStructure structure = core.coreStructure;
//            MultiBlockStorage storage = structure.getStorageForTier(core.tier.get());
//            BlockPos start = core.getPos().add(structure.getCoreOffset(core.tier.get()));
//            storage.forEachBlock(start, (e, e2) -> blocks.add(e));
//
//            return blocks;
//        }
//        return Collections.emptyList();
//    }
//
//    @Override
//    public EnumActionResult canMove() {
//        TileEnergyStorageCore core = getCore();
//        if (core != null && core.structureValid.get() && core.active.get()) {
//            if (core.isFrameMoving) {
//                return EnumActionResult.SUCCESS;
//            }
//            if (!moveCheckComplete) {
//                core.frameMoveContactPoints++;
//            }
//
//            moveCheckComplete = true;
//            if (core.frameMoveContactPoints == 4) {
//                core.frameMoveContactPoints = 0;
//                core.isFrameMoving = true;
//                return EnumActionResult.SUCCESS;
//            }
//        }
//
//        return EnumActionResult.FAIL;
//    }
}
