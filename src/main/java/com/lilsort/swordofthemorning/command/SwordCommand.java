package com.lilsort.swordofthemorning.command;

import com.lilsort.swordofthemorning.retention.RetentionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.Collection;
import java.util.Collections;

public class SwordCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // ONLY /sword is registered!
        dispatcher.register(buildCommand("sword"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCommand(String root) {
        return Commands.literal(root)
                .requires(source -> source.hasPermission(2))
                // /sword -> toggle 3D for self
                .executes(context -> toggleSword(context, Collections.singleton(context.getSource().getPlayerOrException()), false))

                // /sword 2d [targets]
                .then(Commands.literal("2d")
                        .executes(context -> toggleSword(context, Collections.singleton(context.getSource().getPlayerOrException()), true))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> toggleSword(context, EntityArgument.getPlayers(context, "targets"), true))))

                // /sword 3d [targets]
                .then(Commands.literal("3d")
                        .executes(context -> toggleSword(context, Collections.singleton(context.getSource().getPlayerOrException()), false))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> toggleSword(context, EntityArgument.getPlayers(context, "targets"), false))))

                // /sword give ...
                .then(Commands.literal("give")
                        .executes(context -> giveSword(context, Collections.singleton(context.getSource().getPlayerOrException()), false))
                        .then(Commands.literal("2d")
                                .executes(context -> giveSword(context, Collections.singleton(context.getSource().getPlayerOrException()), true))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> giveSword(context, EntityArgument.getPlayers(context, "targets"), true))))
                        .then(Commands.literal("3d")
                                .executes(context -> giveSword(context, Collections.singleton(context.getSource().getPlayerOrException()), false))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> giveSword(context, EntityArgument.getPlayers(context, "targets"), false))))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> giveSword(context, EntityArgument.getPlayers(context, "targets"), false))
                                .then(Commands.literal("2d")
                                        .executes(context -> giveSword(context, EntityArgument.getPlayers(context, "targets"), true)))
                                .then(Commands.literal("3d")
                                        .executes(context -> giveSword(context, EntityArgument.getPlayers(context, "targets"), false)))))

                // /sword remove [targets]
                .then(Commands.literal("remove")
                        .executes(context -> removeSword(context, Collections.singleton(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> removeSword(context, EntityArgument.getPlayers(context, "targets")))))

                // /sword toggle ...
                .then(Commands.literal("toggle")
                        .executes(context -> toggleSword(context, Collections.singleton(context.getSource().getPlayerOrException()), false))
                        .then(Commands.literal("2d")
                                .executes(context -> toggleSword(context, Collections.singleton(context.getSource().getPlayerOrException()), true))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> toggleSword(context, EntityArgument.getPlayers(context, "targets"), true))))
                        .then(Commands.literal("3d")
                                .executes(context -> toggleSword(context, Collections.singleton(context.getSource().getPlayerOrException()), false))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> toggleSword(context, EntityArgument.getPlayers(context, "targets"), false))))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> toggleSword(context, EntityArgument.getPlayers(context, "targets"), false))
                                .then(Commands.literal("2d")
                                        .executes(context -> toggleSword(context, EntityArgument.getPlayers(context, "targets"), true)))
                                .then(Commands.literal("3d")
                                        .executes(context -> toggleSword(context, EntityArgument.getPlayers(context, "targets"), false)))))

                // /sword purge
                .then(Commands.literal("purge")
                        .executes(SwordCommand::purgeAll))

                // /sword help
                .then(Commands.literal("help")
                        .executes(SwordCommand::showHelp));
    }

    private static int toggleSword(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, boolean isClassic) {
        MinecraftServer server = context.getSource().getServer();

        for (ServerPlayer player : targets) {
            if (RetentionManager.hasSwordType(player, isClassic)) {
                // If player already has this exact variant, toggle it off!
                int removed = RetentionManager.purgePlayerSwords(server, player);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9F, 1.0F);

                player.sendSystemMessage(Component.literal("§c§l[Меч Зари] §r§cМеч Зари деактивирован и удален из мира (удалено: " + removed + " шт.)."));
            } else {
                // Equip the requested variant (removes any conflicting variant first)
                RetentionManager.giveSword(player, isClassic);

                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.2F);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.5F);

                String typeName = isClassic ? "2D Classic" : "3D";
                player.sendSystemMessage(Component.literal("§6§l[Меч Зари] §r§eВы призвали Меч Зари (" + typeName + ")! Клинок привязан к вашей душе."));
            }
        }
        return targets.size();
    }

    private static int giveSword(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets, boolean isClassic) {
        for (ServerPlayer player : targets) {
            RetentionManager.giveSword(player, isClassic);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.2F);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.5F);

            String typeName = isClassic ? "2D Classic" : "3D";
            player.sendSystemMessage(Component.literal("§6§l[Меч Зари] §r§eВы призвали Меч Зари (" + typeName + ")! Клинок привязан к вашей душе."));
        }
        return targets.size();
    }

    private static int removeSword(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> targets) {
        MinecraftServer server = context.getSource().getServer();

        for (ServerPlayer player : targets) {
            int removed = RetentionManager.purgePlayerSwords(server, player);

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9F, 1.0F);

            player.sendSystemMessage(Component.literal("§c§l[Меч Зари] §r§7Все экземпляры Меча Зари, привязанные к " + player.getGameProfile().getName() + ", удалены из мира (удалено: " + removed + " шт.)."));
        }

        return targets.size();
    }

    private static int purgeAll(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        int removed = RetentionManager.purgeAllSwords(server);
        context.getSource().sendSuccess(() -> Component.literal("§4§l[Меч Зари] §r§cГлобальная очистка: удалено " + removed + " мечей со всего сервера (из инвентарей, сундуков и земли)."), true);
        return removed;
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("§6§l=== [Меч Зари / Sword of the Morning] Команды ==="), false);
        source.sendSuccess(() -> Component.literal("§e/sword §7- быстрое переключение 3D меча"), false);
        source.sendSuccess(() -> Component.literal("§e/sword 2d §7- переключение 2D Classic меча"), false);
        source.sendSuccess(() -> Component.literal("§e/sword 3d §7- переключение 3D меча"), false);
        source.sendSuccess(() -> Component.literal("§e/sword give <2d|3d> [игрок] §7- выдать меч"), false);
        source.sendSuccess(() -> Component.literal("§e/sword remove [игрок] §7- удалить привязанный меч отовсюду"), false);
        source.sendSuccess(() -> Component.literal("§e/sword toggle <2d|3d> [игрок] §7- переключить меч"), false);
        source.sendSuccess(() -> Component.literal("§e/sword purge §7- экстренная глобальная очистка всех мечей"), false);
        return 1;
    }
}
