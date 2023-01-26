package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.RessourcePack;
import fr.sny1411.tepakap.utils.larguage.Event;
import fr.sny1411.tepakap.utils.larguage.EventsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Admin implements CommandExecutor {
    private Event larguage;
    private Main plugin;
    private MysqlDb bdd;

    public Admin(Main plugin,MysqlDb bdd) {
        this.plugin = plugin;
        this.bdd = bdd;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (commandSender instanceof Player && !commandSender.isOp()) {
            commandSender.sendMessage("§cErreur: §fVous n'avez pas la permission d'utiliser cette commande.");
            return false;
        }
        if (args.length == 0) {
            commandSender.sendMessage("§cErreur: §fVeuillez spécifier un argument.");
            return false;
        } else {
            switch (args[0]) {
                case "chestLarguage":
                    larguage = new Event(plugin,bdd);
                    larguage.chestSpawn();
                    EventsManager.listEvent.add(larguage);
                    break;
                case "chestDespawn":
                    larguage.chestDespawn();
                    larguage.mobsDespawn();
                    break;
                case "CustomsModels":
                    if (args.length > 1) {
                        RessourcePack.showCustomsRessources(Integer.parseInt(args[1]), (Player) commandSender);
                    }
                    break;
                default:
                    commandSender.sendMessage("§cErreur: §fArgument inconnu.");
                    break;
            }

        }
        return false;
    }
}
