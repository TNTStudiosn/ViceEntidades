package com.TNTStudios.viceentidades;

import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import com.TNTStudios.viceentidades.registry.ViceEntityTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class Viceentidades implements ModInitializer {
    @Override
    public void onInitialize() {
        ViceEntityTypes.register();
        FabricDefaultAttributeRegistry.register(ViceEntityTypes.DIAMITA, DiamitaEntity.createAttributes());
    }
}
