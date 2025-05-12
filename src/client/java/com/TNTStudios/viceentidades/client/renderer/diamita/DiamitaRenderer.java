package com.TNTStudios.viceentidades.client.renderer.diamita;

import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import com.TNTStudios.viceentidades.client.model.diamita.DiamitaModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;

public class DiamitaRenderer extends GeoEntityRenderer<DiamitaEntity> {
    public DiamitaRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DiamitaModel());
    }
}
