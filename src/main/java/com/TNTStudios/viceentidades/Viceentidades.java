package com.TNTStudios.viceentidades;

import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import com.TNTStudios.viceentidades.registry.ViceEntityTypes;
import com.TNTStudios.viceentidades.registry.ViceItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import com.TNTStudios.viceentidades.entity.action.ActionEntity;

public class Viceentidades implements ModInitializer {
    public static final String MOD_ID = "viceentidades";  // ← esto
    @Override
    public void onInitialize() {
        ViceEntityTypes.register();
        FabricDefaultAttributeRegistry.register(ViceEntityTypes.DIAMITA, DiamitaEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ViceEntityTypes.ACTION, ActionEntity.createAttributes());

        ViceItems.register();
    }
}
