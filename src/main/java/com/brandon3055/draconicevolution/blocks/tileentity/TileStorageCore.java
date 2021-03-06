package com.brandon3055.draconicevolution.blocks.tileentity;

import codechicken.lib.data.MCDataInput;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.brandonscore.lib.Vec3I;
import com.brandon3055.brandonscore.lib.datamanager.*;
import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.DEContent;
import com.brandon3055.draconicevolution.api.IExtendedRFStorage;
import com.brandon3055.draconicevolution.lib.EnergyCoreBuilder;
import com.brandon3055.draconicevolution.utils.LogHelper;
import com.brandon3055.draconicevolution.world.EnergyCoreStructure;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

import static com.brandon3055.brandonscore.lib.datamanager.DataFlags.*;
import static net.minecraft.util.text.TextFormatting.RED;

/**
 * Created by brandon3055 on 30/3/2016.
 */
public class TileStorageCore extends TileBCore implements ITickableTileEntity, IExtendedRFStorage, IMultiBlockPart {

    //Frame Movement
    public int frameMoveContactPoints = 0;
    public boolean isFrameMoving = false;
    public boolean moveBlocksProvided = false;

    //region Constant Fields

    public static final byte ORIENT_UNKNOWN = 0;
    public static final byte ORIENT_UP_DOWN = 1;
    public static final byte ORIENT_NORTH_SOUTH = 2;
    public static final byte ORIENT_EAST_WEST = 3;

    public static final Direction[][] STAB_ORIENTATIONS = new Direction[][]{{},   // ORIENT_UNKNOWN
            Direction.BY_HORIZONTAL_INDEX,                                                 // ORIENT_UP_DOWN //TODO is 'BY_HORIZONTAL_INDEX' correct?
            {Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST},     // ORIENT_NORTH_SOUTH
            {Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}    // ORIENT_EAST_WEST
    };

    //endregion

    public final EnergyCoreStructure coreStructure = new EnergyCoreStructure().initialize(this);
    public final ManagedBool active = register(new ManagedBool("active", SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public final ManagedBool structureValid = register(new ManagedBool("structure_valid", SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public final ManagedBool coreValid = register(new ManagedBool("core_valid", SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public final ManagedString invalidMessage = register(new ManagedString("invalid_message", SAVE_NBT));
    public final ManagedBool buildGuide = register(new ManagedBool("build_guide", SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public final ManagedBool stabilizersOK = register(new ManagedBool("stabilizers_ok", SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public final ManagedByte tier = register(new ManagedByte("tier", (byte)1, SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE));
    public final ManagedLong energy = register(new ManagedLong("energy", SAVE_NBT_SYNC_TILE));
    public final ManagedVec3I[] stabOffsets = new ManagedVec3I[4];
    public final ManagedLong transferRate = register(new ManagedLong("transfer_rate", SYNC_CONTAINER));

    private int ticksElapsed = 0;
    private long[] flowArray = new long[20];
    private EnergyCoreBuilder activeBuilder = null;
    public float rotation = 0;

    public TileStorageCore() {
        super(DEContent.tile_storage_core);

        for (int i = 0; i < stabOffsets.length; i++) {
            stabOffsets[i] = register(new ManagedVec3I("stabOffset" + i, new Vec3I(0, -1, 0), SAVE_NBT_SYNC_TILE));
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            flowArray[ticksElapsed % 20] = (energy.get() - energy.get());
            long total = 0;
            for (long i : flowArray) {
                total += i;
            }
            transferRate.set(total / 20L);

            if (activeBuilder != null) {
                if (activeBuilder.isDead()) {
                    activeBuilder = null;
                }
                else {
                    activeBuilder.updateProcess();
                }
            }

            if (ticksElapsed % 500 == 0) {
                validateStructure();
            }
        }
        else {
            rotation++;
        }

        super.tick();

        if (ticksElapsed % 20 == 0 && !world.isRemote && transferRate.isDirty(true)) {
            dataManager.forceSync(transferRate);
        }

        if (world.isRemote && active.get()) {
            List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1)).grow(10, 10, 10));
            for (PlayerEntity player : players) {
                double dist = Vec3D.getCenter(this).distance(new Vec3D(player));
                double distNext = new Vec3D(player).distance(new Vec3D(pos.getX() + player.getMotion().x + 0.5, pos.getY() + player.getMotion().y - 0.4, pos.getZ() + player.getMotion().z + 0.5));
                double threshold = tier.get() > 2 ? tier.get() - 0.5 : tier.get() + 0.5;
                double boundary = distNext - threshold;
                double dir = dist - distNext;

                if (boundary <= 0) {
                    if (dir < 0) {
                        player.move(MoverType.PLAYER, new Vec3d(-player.getMotion().x * 1.5, -player.getMotion().y * 1.5, -player.getMotion().z * 1.5));
                    }

                    double multiplier = (threshold - dist) * 0.05;

                    double xm = ((pos.getX() + 0.5 - player.posX) / distNext) * multiplier;
                    double ym = ((pos.getY() - 0.4 - player.posY) / distNext) * multiplier;
                    double zm = ((pos.getZ() + 0.5 - player.posZ) / distNext) * multiplier;

                    player.move(MoverType.PLAYER, new Vec3d(-xm, -ym, -zm));
                }
            }
        }

        ticksElapsed++;
    }

    //region Activation

    public void onStructureClicked(World world, BlockPos blockClicked, BlockState state, PlayerEntity player) {
        if (!world.isRemote) {
            validateStructure();
//            FMLNetworkHandler.openGui(player, DraconicEvolution.instance, GuiHandler.GUIID_ENERGY_CORE, world, pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public void activateCore() {
        if (world.isRemote || !validateStructure()) {
            return;
        }

        if (energy.get() > getCapacity()) {
            energy.set(getCapacity());
        }

        buildGuide.set(false);
        coreStructure.formTier(tier.get());
        active.set(true);
        updateStabilizers(true);
    }

    public void deactivateCore() {
        if (world.isRemote) {
            return;
        }

        coreStructure.revertTier(tier.get());
        active.set(false);
        updateStabilizers(false);
    }

    private long getCapacity() {
        if (tier.get() <= 0 || tier.get() > 8) {
            LogHelper.error("Tier not valid! WTF!!!");
            return 0;
        }
        return (long) DEConfig.coreCapacity[tier.get() - 1];
    }

    @Override
    public void receivePacketFromClient(MCDataInput data, ServerPlayerEntity client, int id) {
        if (id == 0) { //Activate
            if (active.get()) {
                deactivateCore();
            }
            else {
                activateCore();
            }
        }
        else if (id == 1) { //Tier Up
            if (!active.get() && tier.get() < 8) {
                tier.inc();
                validateStructure();
            }
        }
        else if (id == 2) { //Tier Down
            if (!active.get() && tier.get() > 1) {
                tier.dec();
                validateStructure();
            }
        }
        else if (id == 3) { //Toggle Guide
            if (!active.get()) {
                buildGuide.set(!buildGuide.get());
            }
        }
        else if (id == 4) { //Build
            if (!active.get()) {
                startBuilder(client);
            }
        }
    }

    private void startBuilder(PlayerEntity player) {
        if (activeBuilder != null && !activeBuilder.isDead()) {
            player.sendMessage(new TranslationTextComponent("ecore.de.already_assembling.txt").setStyle(new Style().setColor(RED)));
        }
        else {
            activeBuilder = new EnergyCoreBuilder(this, player);
        }
    }

    /**
     * Sets the "isCoreActive" value in each of the stabilizers
     */
    private void updateStabilizers(boolean coreActive) {
        for (ManagedVec3I offset : stabOffsets) {
            BlockPos tilePos = pos.add(-offset.get().x, -offset.get().y, -offset.get().z);
            TileEntity tile = world.getTileEntity(tilePos);

            if (tile instanceof TileCoreStabilizer) {
                ((TileCoreStabilizer) tile).isCoreActive.set(coreActive);
            }
        }
    }

    //endregion

    //region Structure

    /**
     * If the structure has already been validated this method will check that it is still valit.
     * Otherwise it will check if the structure is valid.
     */
    public boolean validateStructure() {
        boolean valid = checkStabilizers();

        if (!(coreValid.set(coreStructure.checkTier(tier.get())))) {
            BlockPos pos = coreStructure.invalidBlock;
            invalidMessage.set("Error At: " + "x:" + pos.getX() + ", y:" + pos.getY() + ", z:" + pos.getZ() + " Expected: " + coreStructure.expectedBlock);
            valid = false;
        }

        if (!valid && active.get()) {
            active.set(false);
            deactivateCore();
        }

        structureValid.set(valid);

        if (valid) {
            invalidMessage.set("");
        }

        return valid;
    }

    /**
     * If stabilizersOK is true this method will check to make sure the stabilisers are still valid.
     * Otherwise it will check for a valid stabilizer configuration.
     */
    public boolean checkStabilizers() {
        boolean flag = true;
        if (stabilizersOK.get()) {
            for (ManagedVec3I offset : stabOffsets) {
                BlockPos tilePos = pos.subtract(offset.get().getPos());
                TileEntity tile = world.getTileEntity(tilePos);

                if (!(tile instanceof TileCoreStabilizer) || !((TileCoreStabilizer) tile).hasCoreLock.get() || ((TileCoreStabilizer) tile).getCore() != this || !((TileCoreStabilizer) tile).isStabilizerValid(tier.get(), this)) {
                    flag = false;
                    break;
                }
            }

            if (!flag) {
                stabilizersOK.set(false);
                releaseStabilizers();
            }
        }
        else {

            //Foe each of the 3 possible axises
            for (int orient = 1; orient < STAB_ORIENTATIONS.length; orient++) {
                Direction[] dirs = STAB_ORIENTATIONS[orient];
                List<TileCoreStabilizer> stabsFound = new ArrayList<TileCoreStabilizer>();

                //For each of the 4 possible directions around the axis
                for (int fIndex = 0; fIndex < dirs.length; fIndex++) {
                    Direction facing = dirs[fIndex];

                    for (int dist = 0; dist < 16; dist++) {
                        BlockPos pos1 = pos.add(facing.getXOffset() * dist, facing.getYOffset() * dist, facing.getZOffset() * dist);
                        TileEntity stabilizer = world.getTileEntity(pos1);
                        if (stabilizer instanceof TileCoreStabilizer && (!((TileCoreStabilizer) stabilizer).hasCoreLock.get() || ((TileCoreStabilizer) stabilizer).getCore().equals(this)) && ((TileCoreStabilizer) stabilizer).isStabilizerValid(tier.get(), this)) {
                            stabsFound.add((TileCoreStabilizer) stabilizer);
                            break;
                        }
                    }
                }

                if (stabsFound.size() == 4) {
                    for (TileCoreStabilizer stab : stabsFound) {
                        stabOffsets[stabsFound.indexOf(stab)].set(new Vec3I(pos.getX() - stab.getPos().getX(), pos.getY() - stab.getPos().getY(), pos.getZ() - stab.getPos().getZ()));
                        stab.setCore(this);
                    }
                    stabilizersOK.set(true);
                    break;
                }

                //Did not find 4 stabilizers
                flag = false;
            }
        }

        return flag;
    }

    /**
     * Frees any stabilizers that are still linked to the core and clears the offset list
     */
    private void releaseStabilizers() {
        for (ManagedVec3I offset : stabOffsets) {
            BlockPos tilePos = pos.add(-offset.get().x, -offset.get().y, -offset.get().z);
            TileEntity tile = world.getTileEntity(tilePos);

            if (tile instanceof TileCoreStabilizer) {
                ((TileCoreStabilizer) tile).hasCoreLock.set(false);
                ((TileCoreStabilizer) tile).coreOffset.get().y = 0;
            }

            offset.set(new Vec3I(0, -1, 0));
        }
    }

    //endregion

    //region Energy Transfer

    public long receiveEnergy(long maxReceive, boolean simulate) {
        if (world.isRemote) {
            return 0;
        }
        long energyReceived = Math.min(getExtendedCapacity() - energy.get(), maxReceive);

        if (!simulate) {
            energy.add(energyReceived);
            markDirty();
        }
        return energyReceived;
    }

    public long extractEnergy(long maxExtract, boolean simulate) {
        if (world.isRemote) {
            return 0;
        }
        long energyExtracted = Math.min(energy.get(), maxExtract);

        if (!simulate) {
            energy.subtract(energyExtracted);
            markDirty();
        }
        return energyExtracted;
    }

    @Override
    public long getExtendedStorage() {
        return energy.get();
    }

    @Override
    public long getExtendedCapacity() {
        return getCapacity();
    }

    //endregion

    //region IMultiBlock

    @Override
    public boolean isStructureValid() {
        return structureValid.get();
    }

    @Override
    public IMultiBlockPart getController() {
        return this;
    }

    //endregion

    //region Rendering



    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return true;
//    }

    //endregion
}

