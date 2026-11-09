package com.tontoque28.creeperinstaboomhard;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CreeperDifficultyCommand extends CommandBase {

    public enum Difficulty {
        EASY, NORMAL, HARD
    }

    public static Difficulty currentDifficulty = Difficulty.HARD;

    @Override
    public String getName() {
        return "creeperdifficulty";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/creeperdifficulty <easy|normal|hard>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        switch (args[0].toLowerCase()) {
            case "easy":
                currentDifficulty = Difficulty.EASY;
                notifyCommandListener(sender, this, "%s", "Creeper Difficulty set to EASY");
                break;
            case "normal":
                currentDifficulty = Difficulty.NORMAL;
                notifyCommandListener(sender, this, "%s", "Creeper Difficulty set to NORMAL");
                break;
            case "hard":
                currentDifficulty = Difficulty.HARD;
                notifyCommandListener(sender, this, "%s", "Creeper Difficulty set to HARD");
                break;
            default:
                throw new CommandException("Invalid mode. Use: easy, normal, hard");
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("easy", "normal", "hard"));
        }
        return Collections.emptyList();
    }
}
