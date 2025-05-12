// src/client/java/com/TNTStudios/viceentidades/client/renderer/action/ActionRenderer.java
package com.TNTStudios.viceentidades.client.renderer.action;

import com.TNTStudios.viceentidades.client.model.action.ActionModel;
import com.TNTStudios.viceentidades.entity.diamantado.ActionEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class ActionRenderer extends GeoEntityRenderer<ActionEntity> {
    public ActionRenderer(EntityRendererFactory.Context context) {
        super(context, new ActionModel());
    }
}
