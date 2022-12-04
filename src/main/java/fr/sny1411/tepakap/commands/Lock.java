package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.secureChest.Lockable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class Lock implements CommandExecutor {
    private static MysqlDb bdd;

    public Lock(MysqlDb bdd) {
        Lock.bdd = bdd;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Block targeBlock = player.getTargetBlock(null,5);
            if (args.length == 0) {
                    lock(targeBlock,player);
            } else {
                switch (args[0]) {
                    case "info":
                        // open gui info
                        break;
                    case "add":
                        if (args.length == 2) {
                            addPlayerAcces(targeBlock,player,args[1]);
                        } else {
                            player.sendMessage("§4[SecureChest] §cUtilise /lock add <nomJoueur>");
                        }
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

    public static void addPlayerAcces(Block block,Player player,String addPlayer) {
        if (Lockable.inList(block.getType())) {
            Location locBlock = block.getLocation();
            double X = locBlock.getX();
            double Y = locBlock.getY();
            double Z = locBlock.getZ();
            String world = Objects.requireNonNull(locBlock.getWorld()).getName();
            ResultSet result = bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                    "WHERE coordX=" + X + " AND coordY=" + Y +
                    " AND coordZ=" + Z + " AND monde='"+ world + "'");

            try {
                if (result.next()) {
                    int idCoffre = result.getInt("id_coffre");
                    if (result.getString("UUID").equals(player.getUniqueId().toString())) {
                        ResultSet searchPlayerAdd = bdd.search("SELECT UUID FROM JOUEUR WHERE pseudo='" + addPlayer + "'");
                        if (searchPlayerAdd.next()) {
                            String UUIDFriend = searchPlayerAdd.getString("UUID");
                            ResultSet resultInBdd = bdd.search("SELECT * FROM ACCEDE WHERE UUID='" + UUIDFriend + "'");
                            if (!resultInBdd.next()) {
                                ResultSet resultNbreFriends = bdd.search("SELECT COUNT(*) AS 'nbre_friends' FROM ACCEDE WHERE id_coffre=" + idCoffre);
                                resultNbreFriends.next();
                                int nbreFriends = resultNbreFriends.getInt("nbre_friends");
                                if (nbreFriends < 7) {
                                    bdd.putNewItems("INSERT INTO ACCEDE VALUES ('" + UUIDFriend + "'," + idCoffre + ")");
                                    player.sendMessage("§2[SecureChest] §aVotre ami(e) à était ajouté(e)");
                                } else {
                                    player.sendMessage("§4[SecureChest] §cVous avez atteins la limite de personne ayant accès à votre block");
                                }

                            } else {
                                player.sendMessage("§4[SecureChest] §cCette personne à déjà accès à ce block");
                            }

                        } else {
                            player.sendMessage("§4[SecureChest] §cVotre ami(e) n'a pas était trouvé :'(");
                        }
                    } else {
                        player.sendMessage("§4[SecureChest] §cCe block ne vous appartient pas");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        }
    }
    public static void lock(Block block,Player player) {
        if (Lockable.inList(block.getType())) {
            Location locBlock = block.getLocation();
            double X = locBlock.getX();
            double Y = locBlock.getY();
            double Z = locBlock.getZ();
            String world = Objects.requireNonNull(locBlock.getWorld()).getName();
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
                bdd.putNewItems("INSERT INTO COFFRE(coordX,coordY,coordZ,monde,UUID) VALUES(" + X + "," + Y+","+Z + ",'" + world + "','" + player.getUniqueId() + "')");
                player.sendMessage("§2[SecureChest] §aCoffre sécurisé");
            } else {
                player.sendMessage("§4 [SecureChest] §cCe coffre est déjà sécurisé");
            }
        } else {
            player.sendMessage("§4[SecureChest] §cCe block n'est pas protégeable");
        }
    }
}
