package fr.sny1411.tepakap.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            assert args != null;
            switch (args.length) {
                case 1 :
                    return new ArrayList<>(Arrays.asList("chestLarguage","chestDespawn","CustomsModels","testRarete","competence"));
                case 2:
                    if (args[0].equals("competence")) {
                        List<String> tabMobs = new ArrayList<>();
                        for (EntityType mob : EntityType.values()) {
                            tabMobs.add(mob.toString());
                        }
                        return tabMobs;
                    }
                default :
                    return Collections.singletonList("");
            }
        }
        return null;
    }
}
