package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.utils.maire.GuiMaire;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Maire implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (commandSender instanceof Player) {
            if (args.length > 0) {
                if (args[0].equals("presente")  && GuiMaire.presentationEnCour) {
                    GuiMaire.openGuiSePresenter((Player) commandSender);
                } else if (args[0].equals("vote") && GuiMaire.voteEnCour) {
                    // pas encore le gui
            }
            }
        }
        return false;
    }
}
