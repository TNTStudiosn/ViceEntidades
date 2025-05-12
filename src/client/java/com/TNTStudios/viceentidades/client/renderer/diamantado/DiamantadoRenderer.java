package com.TNTStudios.viceentidades.client.renderer.diamantado;

import com.TNTStudios.viceentidades.client.model.diamantado.DiamantadoModel;
import com.TNTStudios.viceentidades.entity.diamantado.DiamantadoEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class DiamantadoRenderer extends GeoEntityRenderer<DiamantadoEntity> {
    public DiamantadoRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new DiamantadoModel());
    }
}
