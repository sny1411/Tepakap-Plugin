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
import java.sql.SQLException;
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
            double X = locBlock.getX();
            double Y = locBlock.getY();
            double Z = locBlock.getZ();
            String world = Objects.requireNonNull(locBlock.getWorld()).getName();
            player.sendMessage(Objects.requireNonNull(locBlock.getWorld()).getName());
            ResultSet result = bdd.search("SELECT COUNT(id_coffre) AS 'nb_coffre' FROM COFFRE " +
                                                 "WHERE coordX=" + X + " AND coordY=" + Y +
                                                 " AND coordZ=" + Z + " AND monde='"+ world + "'");
            int nb_coffre = -1;
            try {
                result.next();
                nb_coffre = result.getInt("nb_coffre");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (nb_coffre == 0) {
                bdd.putNewItems("INSERT INTO COFFRE(coordX,coordY,coordZ,monde,UUID) VALUES(" + X + "," + Y+","+Z + "," + world + "," + player.getUniqueId());
            } else {
                player.sendMessage("§4 [SecureChest] §cCe coffre est déjà sécurisé");
            }


        }
    }
}
