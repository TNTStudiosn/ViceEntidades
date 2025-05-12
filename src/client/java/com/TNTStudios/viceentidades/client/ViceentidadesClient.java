package com.TNTStudios.viceentidades.client;

import com.TNTStudios.viceentidades.client.renderer.diamita.DiamitaRenderer;
import com.TNTStudios.viceentidades.client.renderer.action.ActionRenderer;
import com.TNTStudios.viceentidades.client.renderer.diamantado.DiamantadoRenderer;
import com.TNTStudios.viceentidades.registry.ViceEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ViceentidadesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ViceEntityTypes.DIAMITA, DiamitaRenderer::new);
        EntityRendererRegistry.register(ViceEntityTypes.ACTION, ActionRenderer::new);
        EntityRendererRegistry.register(ViceEntityTypes.DIAMANTADO, DiamantadoRenderer::new);

    }
}
