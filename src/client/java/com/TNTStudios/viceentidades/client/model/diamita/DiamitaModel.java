package com.TNTStudios.viceentidades.client.model.diamita;

import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class DiamitaModel extends GeoModel<DiamitaEntity> {
    @Override
    public Identifier getModelResource(DiamitaEntity entity) {
        return new Identifier("viceentidades", "geo/diamita.geo.json");
    }

    @Override
    public Identifier getTextureResource(DiamitaEntity entity) {
        return new Identifier("viceentidades", "textures/entity/diamita.png");
    }

    @Override
    public Identifier getAnimationResource(DiamitaEntity entity) {
        return new Identifier("viceentidades", "animations/diamita.animation.json");
    }
}
