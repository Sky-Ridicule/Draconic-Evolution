package com.brandon3055.draconicevolution.client.handler;


import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.reflect.ObfMapping;
import codechicken.lib.reflect.ReflectionManager;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.shader.ShaderProgram;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.client.ProcessHandlerClient;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.lib.DelayedExecutor;
import com.brandon3055.brandonscore.lib.PairKV;
import com.brandon3055.brandonscore.utils.ModelUtils;
import com.brandon3055.brandonscore.utils.Utils;
import com.brandon3055.draconicevolution.DEConfig;
import com.brandon3055.draconicevolution.DEContent;
import com.brandon3055.draconicevolution.api.ICrystalBinder;
import com.brandon3055.draconicevolution.api.itemconfig.ToolConfigHelper;
import com.brandon3055.draconicevolution.client.render.shaders.DEShaders;
import com.brandon3055.draconicevolution.handlers.BinderHandler;
import com.brandon3055.draconicevolution.items.tools.CreativeExchanger;
import com.brandon3055.draconicevolution.items.tools.MiningToolBase;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Brandon on 28/10/2014.
 */
public class ClientEventHandler {
    public static Map<PlayerEntity, PairKV<Float, Integer>> playerShieldStatus = new HashMap<PlayerEntity, PairKV<Float, Integer>>();
    public static ObfMapping splashTextMapping = new ObfMapping("net/minecraft/client/gui/GuiMainMenu", "field_110353_x");
    public static FloatBuffer winPos = GLAllocation.createDirectFloatBuffer(3);
    public static volatile int elapsedTicks;
    public static boolean playerHoldingWrench = false;
    public static Minecraft mc;
    private static Random rand = new Random();
    public static IBakedModel shieldModel = null;
    public static BlockPos explosionPos = null;
    public static double explosionAnimation = 0;
    public static int explosionTime = 0;
    public static boolean explosionRetreating = false;

    public static ShaderProgram explosionShader;

    @SubscribeEvent
    public void renderGameOverlay(RenderGameOverlayEvent.Post event) {
        HudHandler.drawHUD(event);

        if (explosionPos != null && event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            mc = Minecraft.getInstance();
            updateExplosionAnimation(mc, mc.world, event.getWindow(), mc.getRenderPartialTicks());
        }
    }

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.type != TickEvent.Type.CLIENT || event.side != LogicalSide.CLIENT) {
            return;
        }

        elapsedTicks++;
        HudHandler.clientTick();

        if (explosionPos != null) {
            updateExplosion();
        }

        playerShieldStatus.entrySet().removeIf(entry -> elapsedTicks - entry.getValue().getValue() > 5);

        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            playerHoldingWrench = (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ICrystalBinder) || (!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() instanceof ICrystalBinder);
        }
    }

    @SubscribeEvent
    public void renderPlayerEvent(RenderPlayerEvent.Post event) {
        if (!DEConfig.disableShieldHitEffect &&  playerShieldStatus.containsKey(event.getEntityPlayer())) {
            if (shieldModel == null) {
                try {
//                    shieldModel = OBJLoader.INSTANCE.loadModel(ResourceHelperDE.getResource("models/armor/shield_sphere.obj")).bake(TransformUtils.DEFAULT_BLOCK, DefaultVertexFormats.BLOCK, TextureUtils::getTexture);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            GlStateManager.pushMatrix();
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();
            GlStateManager.disableAlphaTest();
            GlStateManager.enableBlend();
            GlStateManager.disableLighting();

            float p = playerShieldStatus.get(event.getEntityPlayer()).getKey();

            PlayerEntity viewingPlayer = Minecraft.getInstance().player;

            int i = 5 - (elapsedTicks - playerShieldStatus.get(event.getEntityPlayer()).getValue());

            //GlStateManager.color(1F - p, 0F, p, i / 5F);

            if (viewingPlayer != event.getEntityPlayer()) {
                double translationXLT = event.getEntityPlayer().prevPosX - viewingPlayer.prevPosX;
                double translationYLT = event.getEntityPlayer().prevPosY - viewingPlayer.prevPosY;
                double translationZLT = event.getEntityPlayer().prevPosZ - viewingPlayer.prevPosZ;

                double translationX = translationXLT + (((event.getEntityPlayer().posX - viewingPlayer.posX) - translationXLT) * event.getPartialRenderTick());
                double translationY = translationYLT + (((event.getEntityPlayer().posY - viewingPlayer.posY) - translationYLT) * event.getPartialRenderTick());
                double translationZ = translationZLT + (((event.getEntityPlayer().posZ - viewingPlayer.posZ) - translationZLT) * event.getPartialRenderTick());

                GlStateManager.translated(translationX, translationY + 1.1, translationZ);
            }
            else {
                //GL11.glTranslated(0, -0.5, 0);
                GlStateManager.translated(0, 1.15, 0);
            }

            GlStateManager.scaled(1, 1.5, 1);

            GlStateManager.bindTexture(Minecraft.getInstance().getTextureMap().getGlTextureId());

            ModelUtils.renderQuadsARGB(shieldModel.getQuads(null, null, rand), new ColourRGBA(1D - p, 0D, p, i / 5D).argb());

            GlStateManager.enableCull();
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void guiOpenEvent(GuiOpenEvent event) {
        if (event.getGui() instanceof MainMenuScreen && rand.nextInt(150) == 0) {
            try {
                String s = rand.nextBoolean() ? "Icosahedrons proudly brought to you by CCL!!!" : Utils.addCommas(Long.MAX_VALUE) + " RF!!!!";
                ReflectionManager.setField(splashTextMapping, event.getGui(), s);
            }
            catch (Exception e) {}
        }
    }

    @SubscribeEvent
    public void renderWorldEvent(RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            return;
        }

        ClientPlayerEntity player = Minecraft.getInstance().player;
        World world = player.getEntityWorld();
        ItemStack stack = player.getHeldItemMainhand();
        ItemStack offStack = player.getHeldItemOffhand();
        Minecraft mc = Minecraft.getInstance();
        float partialTicks = event.getPartialTicks();

        if (!stack.isEmpty() && stack.getItem() instanceof ICrystalBinder) {
            BinderHandler.renderWorldOverlay(player, world, stack, mc, partialTicks);
            return;
        }
        else if (!stack.isEmpty() && offStack.getItem() instanceof ICrystalBinder) {
            BinderHandler.renderWorldOverlay(player, world, offStack, mc, partialTicks);
            return;
        }


        if (!(mc.objectMouseOver instanceof BlockRayTraceResult)) {
            return;
        }

        if (!stack.isEmpty() && stack.getItem() == DEContent.creative_exchanger) {

            List<BlockPos> blocks = CreativeExchanger.getBlocksToReplace(stack, ((BlockRayTraceResult)mc.objectMouseOver).getPos(), world, ((BlockRayTraceResult)mc.objectMouseOver).getFace());

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            double offsetX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
            double offsetY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
            double offsetZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color4f(1F, 1F, 1F, 1F);
            GlStateManager.lineWidth(2.0F);
            GlStateManager.disableTexture();

            for (BlockPos block : blocks) {
                if (world.isAirBlock(block)) {
                    continue;
                }

                double renderX = block.getX() - offsetX;
                double renderY = block.getY() - offsetY;
                double renderZ = block.getZ() - offsetZ;

                Cuboid6 box = new Cuboid6(renderX, renderY, renderZ, renderX + 1, renderY + 1, renderZ + 1).expand(0.001, 0.001, 0.001);
                float colour = 1F;
                if (!world.getBlockState(block.offset(((BlockRayTraceResult)mc.objectMouseOver).getFace())).getMaterial().isReplaceable()) {
                    GlStateManager.disableDepthTest();
                    colour = 0.2F;
                }
                GL11.glColor4f(colour, colour, colour, colour);

                RenderUtils.drawCuboidOutline(box);

                if (!world.getBlockState(block.offset(((BlockRayTraceResult) mc.objectMouseOver).getFace())).getMaterial().isReplaceable()) {
                    GlStateManager.enableDepthTest();
                }
            }

            GlStateManager.enableTexture();
            GlStateManager.disableBlend();
        }

        if (stack.isEmpty() || !(stack.getItem() instanceof MiningToolBase) || !ToolConfigHelper.getBooleanField("showDigAOE", stack)) {
            return;
        }

        BlockPos pos = ((BlockRayTraceResult) mc.objectMouseOver).getPos();
        BlockState state = world.getBlockState(pos);
        MiningToolBase tool = (MiningToolBase) stack.getItem();

        if (!tool.isToolEffective(stack, state)) {
            return;
        }

        renderMiningAOE(world, stack, pos, player, partialTicks);
    }

    private void renderMiningAOE(World world, ItemStack stack, BlockPos pos, ClientPlayerEntity player, float partialTicks) {
        MiningToolBase tool = (MiningToolBase) stack.getItem();
        PairKV<BlockPos, BlockPos> aoe = tool.getMiningArea(pos, player, tool.getDigAOE(stack), tool.getDigDepth(stack));
        List<BlockPos> blocks = Lists.newArrayList(BlockPos.getAllInBoxMutable(aoe.getKey(), aoe.getValue()));
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        double offsetX = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
        double offsetY = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks;
        double offsetZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color4f(1F, 1F, 1F, 1F);
        GlStateManager.lineWidth(2.0F);
        GlStateManager.disableTexture();
        GlStateManager.disableDepthTest();


        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        for (BlockPos block : blocks) {
            BlockState state = world.getBlockState(block);

            if (!tool.isToolEffective(stack, state)) {
                continue;
            }

            double renderX = block.getX() - offsetX;
            double renderY = block.getY() - offsetY;
            double renderZ = block.getZ() - offsetZ;

            AxisAlignedBB box = new AxisAlignedBB(renderX, renderY, renderZ, renderX + 1, renderY + 1, renderZ + 1).shrink(0.49D);

            double rDist = Utils.getDistanceSq(pos.getX(), pos.getY(), pos.getZ(), block.getX(), block.getY(), block.getZ());


            float colour = 1F - (float) rDist / 100F;
            if (colour < 0.1F) {
                colour = 0.1F;
            }
            float alpha = colour;
            if (alpha < 0.15) {
                alpha = 0.15F;
            }

            float r = 0F;
            float g = 1F;
            float b = 1F;


            buffer.pos(box.minX, box.minY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.maxX, box.maxY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

            buffer.pos(box.maxX, box.minY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.minX, box.maxY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

            buffer.pos(box.minX, box.minY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.maxX, box.maxY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

            buffer.pos(box.maxX, box.minY, box.maxZ).color(r * colour, g * colour, b * colour, alpha).endVertex();
            buffer.pos(box.minX, box.maxY, box.minZ).color(r * colour, g * colour, b * colour, alpha).endVertex();

        }

        tessellator.draw();

        GlStateManager.enableDepthTest();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public static void triggerExplosionEffect(BlockPos pos) {
        explosionPos = pos;
        explosionRetreating = false;
        explosionAnimation = 0;
        explosionTime = 0;

        ProcessHandlerClient.addProcess(new DelayedExecutor(5) {
            @Override
            public void execute(Object[] args) {
//                FMLClientHandler.instance().reloadRenderers();TODO Reload
            }
        });
    }

    private void updateExplosion() {
        if (Minecraft.getInstance().isGamePaused()) {
            return;
        }
        explosionTime++;
        if (!explosionRetreating) {
            explosionAnimation += 0.05;
            if (explosionAnimation >= 1) {
                explosionAnimation = 1;
                explosionRetreating = true;
            }
        }
        else {
            if (explosionAnimation <= 0) {
                explosionAnimation = 0;
                explosionPos = null;
                return;
            }
            explosionAnimation -= 0.01;
        }
    }

    public static final IntBuffer VIEWPORT = GLAllocation.createDirectByteBuffer(16 << 2).asIntBuffer();
    public static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    public static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private void updateExplosionAnimation(Minecraft mc, World world, MainWindow resolution, float partialTick) {
        //region TargetPoint Calculation

        GL11.glGetFloatv(2982, MODELVIEW);
        GL11.glGetFloatv(2983, PROJECTION);
        GlStateManager.getInteger(2978, VIEWPORT);

        Entity entity = mc.getRenderViewEntity();
        float x = (float) (entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTick);
        float y = (float) (entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTick);
        float z = (float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTick);
        Vector3 targetPos = Vector3.fromBlockPosCenter(explosionPos);
        targetPos.subtract(x, y, z);
        gluProject((float) targetPos.x, (float) targetPos.y, (float) targetPos.z, MODELVIEW, PROJECTION, VIEWPORT, winPos);

        boolean behind = winPos.get(2) > 1;
        float screenX = behind ? -1 : winPos.get(0) / resolution.getWidth();
        float screenY = behind ? -1 : winPos.get(1) / resolution.getHeight();

        //endregion

        //region No Shader
        if (!DEShaders.useShaders() || explosionRetreating) {
            float alpha;
            if (explosionAnimation <= 0) {
                alpha = 0;
            }
            else if (explosionRetreating) {
                alpha = (float) explosionAnimation - (partialTick * 0.003F);
            }
            else {
                alpha = (float) explosionAnimation + (partialTick * 0.2F);
            }
            GuiHelper.drawColouredRect(0, 0, resolution.getScaledWidth(), resolution.getScaledHeight(), 0x00FFFFFF | (int) (alpha * 255F) << 24);
        }
        //endregion

        else {
            if (explosionShader == null) {
                explosionShader = new ShaderProgram();
                explosionShader.attachShader(DEShaders.explosionOverlay);
            }

            explosionShader.useShader(cache -> {
                cache.glUniform2F("screenPos", screenX, screenY);
                cache.glUniform1F("intensity", (float) explosionAnimation);
                cache.glUniform2F("screenSize", resolution.getWidth(), resolution.getHeight());
            });

            GuiHelper.drawColouredRect(0, 0, resolution.getScaledWidth(), resolution.getScaledHeight(), 0xFFFFFFFF);

            explosionShader.releaseShader();
        }

    }

    private static final float[] in = new float[4];
    private static final float[] out = new float[4];

    public static boolean gluProject(
            float objx,
            float objy,
            float objz,
            FloatBuffer modelMatrix,
            FloatBuffer projMatrix,
            IntBuffer viewport,
            FloatBuffer win_pos) {

        float[] in = ClientEventHandler.in;
        float[] out = ClientEventHandler.out;

        in[0] = objx;
        in[1] = objy;
        in[2] = objz;
        in[3] = 1.0f;

        __gluMultMatrixVecf(modelMatrix, in, out);
        __gluMultMatrixVecf(projMatrix, out, in);

        if (in[3] == 0.0)
            return false;

        in[3] = (1.0f / in[3]) * 0.5f;

        // Map x, y and z to range 0-1
        in[0] = in[0] * in[3] + 0.5f;
        in[1] = in[1] * in[3] + 0.5f;
        in[2] = in[2] * in[3] + 0.5f;

        // Map x,y to viewport
        win_pos.put(0, in[0] * viewport.get(viewport.position() + 2) + viewport.get(viewport.position() + 0));
        win_pos.put(1, in[1] * viewport.get(viewport.position() + 3) + viewport.get(viewport.position() + 1));
        win_pos.put(2, in[2]);

        return true;
    }

    private static void __gluMultMatrixVecf(FloatBuffer m, float[] in, float[] out) {
        for (int i = 0; i < 4; i++) {
            out[i] =
                    in[0] * m.get(m.position() + 0*4 + i)
                            + in[1] * m.get(m.position() + 1*4 + i)
                            + in[2] * m.get(m.position() + 2*4 + i)
                            + in[3] * m.get(m.position() + 3*4 + i);

        }
    }
}