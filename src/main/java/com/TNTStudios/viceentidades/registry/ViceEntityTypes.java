package com.TNTStudios.viceentidades.registry;

import com.TNTStudios.viceentidades.entity.diamita.DiamitaEntity;
import com.TNTStudios.viceentidades.entity.action.ActionEntity;
import net.minecraft.entity.attribute.EntityAttributes;
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
                    .setDimensions(0.75F, 0.85F)
                    .build("diamita") // ← Aquí está el cambio importante
    );

    public static final EntityType<ActionEntity> ACTION = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier("viceentidades", "action"),
            EntityType.Builder.create(ActionEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(1.7F, 2.0F)
                    .build("action")
    );

    public static void register() {
        // Método vacío si no necesitas lógica adicional
    }
}
