package com.tontoque28.CreeperInstaBoomHard;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CreeperDifficultyCommand {

    public enum Difficulty {
        EASY, NORMAL, HARD
    }

    public static Difficulty currentDifficulty = Difficulty.HARD;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("creeperdifficulty")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("mode", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        builder.suggest("easy");
                        builder.suggest("normal");
                        builder.suggest("hard");
                        return builder.buildFuture();
                    })
                    .executes(context -> setDifficulty(context.getSource(), StringArgumentType.getString(context, "mode")))
                )
        );
    }

    private static int setDifficulty(CommandSourceStack source, String mode) {
        switch (mode.toLowerCase()) {
            case "easy" -> {
                currentDifficulty = Difficulty.EASY;
                source.sendSuccess(() -> Component.literal("Creeper Difficulty set to EASY"), true);
            }
            case "normal" -> {
                currentDifficulty = Difficulty.NORMAL;
                source.sendSuccess(() -> Component.literal("Creeper Difficulty set to NORMAL"), true);
            }
            case "hard" -> {
                currentDifficulty = Difficulty.HARD;
                source.sendSuccess(() -> Component.literal("Creeper Difficulty set to HARD"), true);
            }
            default -> {
                source.sendFailure(Component.literal("Invalid mode. Use: easy, normal, hard"));
                return 0;
            }
        }
        return 1;
    }
}
