package com.TNTStudios.viceentidades.command;

import com.TNTStudios.viceentidades.entity.diamantado.DiamantadoEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.border.WorldBorder;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

public class AtacarCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("atacar")
                .requires(src -> src.hasPermissionLevel(4))
                .executes(ctx -> {
                    ServerCommandSource src = ctx.getSource();

                    // Construir caja basada en el borde del mundo
                    WorldBorder border = src.getWorld().getWorldBorder();
                    double size = border.getSize();
                    double radius = size / 2.0;
                    double centerX = border.getCenterX();
                    double centerZ = border.getCenterZ();

                    Box worldBox = new Box(
                            centerX - radius, src.getWorld().getBottomY(), centerZ - radius,
                            centerX + radius, src.getWorld().getTopY(), centerZ + radius
                    );

                    // Buscar todos los DiamantadoEntity
                    List<DiamantadoEntity> bosses = src.getWorld().getEntitiesByClass(
                            DiamantadoEntity.class,
                            worldBox,
                            entity -> true
                    );

                    // Asignar a cada uno el jugador más cercano
                    for (DiamantadoEntity boss : bosses) {
                        src.getServer().getPlayerManager().getPlayerList().stream()
                                .filter(ServerPlayerEntity::isAlive)
                                .min((a, b) -> Double.compare(a.squaredDistanceTo(boss), b.squaredDistanceTo(boss)))
                                .ifPresent(boss::startAttack);
                    }

                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
