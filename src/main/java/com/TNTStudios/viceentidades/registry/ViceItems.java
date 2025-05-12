package com.TNTStudios.viceentidades.registry;

import com.TNTStudios.viceentidades.Viceentidades;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ViceItems {

    public static final Item DIAMITA_SPAWN_EGG = registerEgg(
            "diamita_spawn_egg",
            ViceEntityTypes.DIAMITA,
            0x44CCFF, 0x226688
    );

    public static final Item ACTION_SPAWN_EGG = registerEgg(
            "action_spawn_egg",
            ViceEntityTypes.ACTION,
            0xFF4444, 0x882222
    );

    private static Item registerEgg(String name, EntityType<? extends net.minecraft.entity.mob.MobEntity> type, int primaryColor, int secondaryColor) {
        Item item = new SpawnEggItem(type, primaryColor, secondaryColor, new FabricItemSettings());
        Registry.register(Registries.ITEM, new Identifier(Viceentidades.MOD_ID, name), item);

        // Añadir al grupo de huevos
        ItemGroupEvents.modifyEntriesEvent(net.minecraft.item.ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(item);
        });

        return item;
    }


    public static void register() {
        // Se llama desde onInitialize()
    }
}
