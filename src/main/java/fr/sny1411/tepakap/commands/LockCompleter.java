package fr.sny1411.tepakap.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LockCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@Nullable CommandSender sender,@Nullable Command command,@Nullable String label,@Nullable String[] args) {
        if (sender instanceof Player) {
            assert args != null;
            switch (args.length) {
                case 1 :
                    return new ArrayList<String>(Arrays.asList("add", "remove", "info", "auto"));
                case 2:
                    return null;
                default :
                    return Collections.singletonList("");
            }
        }
        return null;
    }
}
