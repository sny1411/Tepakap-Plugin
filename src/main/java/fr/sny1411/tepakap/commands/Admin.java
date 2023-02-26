package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.BlocksUtils;
import fr.sny1411.tepakap.utils.RessourcePack;
import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import fr.sny1411.tepakap.utils.larguage.Event;
import fr.sny1411.tepakap.utils.larguage.EventsManager;
import fr.sny1411.tepakap.utils.larguage.Rarete;
import fr.sny1411.tepakap.utils.maire.GuiMaire;
import fr.sny1411.tepakap.utils.pioches.*;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Admin implements CommandExecutor {
    private Event largage;
    private Main plugin;
    private MysqlDb bdd;
    public static boolean canJoin = true;

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
                    largage = new Event(plugin,bdd);
                    largage.chestSpawn();
                    EventsManager.listEvent.add(largage);
                    break;
                case "chestDespawn":
                    largage.chestDespawn();
                    largage.mobsDespawn();
                    break;
                case "CustomsModels":
                    if (args.length > 1) {
                        assert commandSender instanceof Player;
                        RessourcePack.showCustomsRessources(Integer.parseInt(args[1]), (Player) commandSender);
                    }
                    break;
                case "testRarete":
                    for (int i = 0; i < 10;i++) {
                        commandSender.sendMessage(Rarete.choiceRare().name());
                    }
                    break;
                case "competence":
                    assert commandSender instanceof Player;
                    ((Player)commandSender).setStatistic(Statistic.KILL_ENTITY, EntityType.valueOf(args[1]), Integer.parseInt(args[2]));
                    break;
                case "changePresentation":
                    GuiMaire.presentationEnCour = !GuiMaire.presentationEnCour;
                    break;
                case "changeVoteEnCours":
                    GuiMaire.voteEnCour = !GuiMaire.voteEnCour;
                    break;
                case "setPresentoir":
                    assert commandSender instanceof Player;
                    Player player = (Player) commandSender;
                    Location target = player.getTargetBlock(null,5).getLocation();
                    GuiMaire.presentoir = target;
                    ClockEvents.plugin.getConfig().set("maire.xPres", target.getX());
                    ClockEvents.plugin.getConfig().set("maire.yPres", target.getY());
                    ClockEvents.plugin.getConfig().set("maire.zPres", target.getZ());
                    break;
                case "testPioche":
                    assert commandSender instanceof Player;
                    player = (Player) commandSender;
                    player.getInventory().addItem(Pioche3x3.get());
                    player.getInventory().addItem(PiocheBedrock.get());
                    player.getInventory().addItem(PiocheIncinerator.get());
                    player.getInventory().addItem(PiocheSpawner.get());
                    player.getInventory().addItem(PiocheMultiTool.get());
                    player.getInventory().addItem(BlocksUtils.braiseMiraculeuse);
                    break;
                case "info":
                    assert commandSender instanceof Player;
                    player = (Player) commandSender;
                    player.sendMessage("maire presentation:" + GuiMaire.presentationEnCour);
                    player.sendMessage("maire vote:" + GuiMaire.voteEnCour);
                    break;
                case "setConnect":
                    canJoin = !canJoin;
                    break;
                default:
                    commandSender.sendMessage("§cErreur: §fArgument inconnu.");
                    break;
            }

        }
        return false;
    }
}
