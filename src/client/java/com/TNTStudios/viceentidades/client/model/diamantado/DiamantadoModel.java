package com.TNTStudios.viceentidades.client.model.diamantado;

import com.TNTStudios.viceentidades.entity.diamantado.DiamantadoEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class DiamantadoModel extends GeoModel<DiamantadoEntity> {
    @Override public Identifier getModelResource(DiamantadoEntity entity) {
        return new Identifier("viceentidades", "geo/diamantado.geo.json");
    }
    @Override public Identifier getTextureResource(DiamantadoEntity entity) {
        return new Identifier("viceentidades", "textures/entity/diamantado.png");
    }
    @Override public Identifier getAnimationResource(DiamantadoEntity entity) {
        return new Identifier("viceentidades", "animations/diamantado.animation.json");
    }
}
