package fr.sny1411.tepakap.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopCompleter implements @Nullable TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            assert args != null;
            switch (args.length) {
                case 1:
                    List<String> arg = new ArrayList<>(Arrays.asList("armures"));
                    return arg;
            }
        }
        return null;
    }
}
