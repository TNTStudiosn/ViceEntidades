// src/client/java/com/TNTStudios/viceentidades/client/model/action/ActionModel.java
package com.TNTStudios.viceentidades.client.model.action;

import com.TNTStudios.viceentidades.entity.action.ActionEntity;
import software.bernie.geckolib.model.GeoModel;
import net.minecraft.util.Identifier;

public class ActionModel extends GeoModel<ActionEntity> {
    @Override
    public Identifier getModelResource(ActionEntity entity) {
        return new Identifier("viceentidades", "geo/action.geo.json");
    }

    @Override
    public Identifier getTextureResource(ActionEntity entity) {
        return new Identifier("viceentidades", "textures/entity/action.png");
    }

    @Override
    public Identifier getAnimationResource(ActionEntity entity) {
        return new Identifier("viceentidades", "animations/action.animation.json");
    }
}
