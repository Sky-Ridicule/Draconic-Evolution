package com.brandon3055.draconicevolution.client.render.entity;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.OBJParser;
import com.brandon3055.draconicevolution.entity.EntityChaosImplosion;
import com.brandon3055.draconicevolution.helpers.ResourceHelperDE;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

/**
 * Created by brandon3055 on 3/10/2015.
 */
public class RenderEntityChaosVortex extends EntityRenderer<EntityChaosImplosion> {
    private static CCModel model;

    public RenderEntityChaosVortex(EntityRendererManager manager) {
        super(manager);
        Map<String, CCModel> map = OBJParser.parseModels(ResourceHelperDE.getResource("models/reactor_core_model.obj"));
        model = CCModel.combine(map.values());
    }

    public void doRender(EntityChaosImplosion entity, double x, double y, double z, float f1, float tick) {
//        if (entity.ticksExisted < 100) {
//            return;
//        }
//        GlStateManager.pushMatrix();
//        GlStateManager.translate(x, y, z);
//        GlStateManager.disableLighting();
//        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 200F, 200F);
//        //ResourceHandler.bindResource("textures/models/white.png");
//
//        float scale = ((float) entity.ticksExisted - 100F) + tick;
//        scale /= 500F;
//        if (scale > 0.5F) scale = 0.5F;
//
//        GlStateManager.scale(scale, scale, scale);
//        //uvSphere.renderAll();
//
//        GlStateManager.enableLighting();
//        GlStateManager.popMatrix();
    }


    @Override
    protected ResourceLocation getEntityTexture(EntityChaosImplosion entity) {
        return null;
    }

}
