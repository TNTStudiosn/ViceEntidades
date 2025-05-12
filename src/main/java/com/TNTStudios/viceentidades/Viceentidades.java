package com.TNTStudios.viceentidades;

import com.TNTStudios.viceentidades.command.AtacarCommand;
import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import com.TNTStudios.viceentidades.registry.ViceEntityTypes;
import com.TNTStudios.viceentidades.registry.ViceItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import com.TNTStudios.viceentidades.entity.diamantado.ActionEntity;

public class Viceentidades implements ModInitializer {
    public static final String MOD_ID = "viceentidades";  // ← esto
    @Override
    public void onInitialize() {
        ViceEntityTypes.register();
        FabricDefaultAttributeRegistry.register(ViceEntityTypes.DIAMITA, DiamitaEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ViceEntityTypes.ACTION, ActionEntity.createAttributes());

        ViceItems.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AtacarCommand.register(dispatcher);
        });

    }
}
