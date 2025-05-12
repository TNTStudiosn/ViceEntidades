package com.TNTStudios.viceentidades.registry;

import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

public class ViceEntityTypes {
    public static final EntityType<DiamitaEntity> DIAMITA = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("viceentidades", "diamita"),
            EntityType.Builder.create(DiamitaEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6F, 1.8F)
                    .disableSummon()
                    .build("diamita") // ← Aquí está el cambio importante
    );

    public static void register() {
        // Método vacío si no necesitas lógica adicional
    }
}
