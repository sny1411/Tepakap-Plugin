package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.secureChest.Lockable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.util.Objects;

public class Lock implements CommandExecutor {
    private static MysqlDb bdd;

    public Lock(MysqlDb bdd) {
        Lock.bdd = bdd;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block targeBlock = player.getTargetBlock(null,5);
            if (args.length == 0) {
                if (Lockable.inList(targeBlock.getType())) {
                    lock(targeBlock,player);
                }
            } else {
                switch (args[0]) {
                    case "info":
                        // open gui info
                        break;
                    case "add":
                        // ajouter user
                        break;
                    case "remove":
                        // delete user
                        break;
                    case "auto":
                        // mode autolock
                }
            }
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage("§4[CHEST] §cCommande non executable par la console");
        }
        return false;
    }

    public static void lock(Block block,Player player) {
        if (Lockable.inList(block.getType())) {
            Location locBlock = block.getLocation();
            player.sendMessage(Objects.requireNonNull(locBlock.getWorld()).getName());
            ResultSet result = bdd.search("SELECT COUNT(id_coffre) FROM COFFRE " +
                                                 "WHERE coordX=" + locBlock.getBlockX() + " AND coordY=" + locBlock.getBlockY() +
                                                 " AND coordZ=" + locBlock.getBlockZ() + " AND monde='"+locBlock.getWorld().getName() + "'");
        }
    }
}
