package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.utils.maire.GuiMaire;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Maire implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (GuiMaire.presentationEnCour && commandSender instanceof Player) {
            GuiMaire.openGuiSePresenter((Player) commandSender);
        }
        return false;
    }
}
