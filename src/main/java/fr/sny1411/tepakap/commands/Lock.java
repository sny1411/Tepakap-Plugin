package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.secureChest.Lockable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class Lock implements CommandExecutor {
    private static MysqlDb bdd;
    private static Main main;

    public Lock(MysqlDb bdd,Main main) {
        Lock.bdd = bdd;
        Lock.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,@NotNull Command command,@NotNull String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Block targeBlock = player.getTargetBlock(null,5);
                if (args.length == 0) {
                    lock(targeBlock,player);
                } else {
                    switch (args[0]) {
                        case "info":
                            infoGui(player,targeBlock);
                            break;
                        case "add":
                            if (args.length == 2) {
                                addPlayerAcces(targeBlock,player,args[1]);
                            } else {
                                player.sendMessage("§4[SecureChest] §cUtilise /lock add <nomJoueur>");
                            }
                            break;
                        case "remove":
                            if (args.length == 2) {
                                removePlayerAcces(targeBlock,player,args[1]);
                            } else {
                                player.sendMessage("§4[SecureChest] §cUtilise /lock remove <nomJoueur>");
                            }
                            break;
                        case "auto":
                            // mode autolock
                    }
                }
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage("§4[SecureCHEST] §cCommande non executable par la console");
            }
        });

        return false;
    }

    public static void infoGui(Player player, Block block) {
        Location locBlock = block.getLocation();
        double X = locBlock.getX();
        double Y = locBlock.getY();
        double Z = locBlock.getZ();
        Bukkit.getConsoleSender().sendRawMessage(locBlock.toString());
        String world = Objects.requireNonNull(locBlock.getWorld()).getName();
        ResultSet result = searchCoffre(block,X,Y,Z,world, player);
        try {
            assert result != null;
            if (result.next()) {
                String UUIDowner = result.getString("UUID");
                int id_coffre = result.getInt("id_coffre");
                Inventory infoGui = Bukkit.createInventory(null, InventoryType.CHEST, "info");
                ItemStack itemGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta metaGlass = itemGlass.getItemMeta();
                assert metaGlass != null;
                metaGlass.setDisplayName(" ");
                itemGlass.setItemMeta(metaGlass);

                ResultSet resultListFriends = bdd.search("SELECT UUID FROM ACCEDE WHERE id_coffre=" + id_coffre);

                for (int i = 0; i < 27; i++) {
                    switch (i) {
                        case 4:
                            OfflinePlayer Owner = Bukkit.getOfflinePlayer(UUID.fromString(UUIDowner));
                            ItemStack OwnerHead = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta skullMetaOwner = (SkullMeta)OwnerHead.getItemMeta();
                            assert skullMetaOwner != null;
                            skullMetaOwner.setOwningPlayer(Owner);
                            OwnerHead.setItemMeta(skullMetaOwner);
                            infoGui.setItem(i, OwnerHead);
                            break;
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                            if (resultListFriends.next()) {
                                String UUIDFriend = resultListFriends.getString("UUID");
                                Bukkit.getConsoleSender().sendMessage(UUIDFriend);
                                OfflinePlayer friend = Bukkit.getOfflinePlayer(UUID.fromString(UUIDFriend));
                                ItemStack friendHead = new ItemStack(Material.PLAYER_HEAD);
                                SkullMeta skullMeta = (SkullMeta)friendHead.getItemMeta();
                                assert skullMeta != null;
                                skullMeta.setOwningPlayer(friend);
                                friendHead.setItemMeta(skullMeta);
                                infoGui.setItem(i, friendHead);
                            }
                            break;
                        default:
                            infoGui.setItem(i, itemGlass);
                            break;
                    }
                }
                Bukkit.getScheduler().runTask(main, () -> {
                    player.openInventory(infoGui);
                });
            } else {
                player.sendMessage("§4[SecureChest] §cBlock non protégé");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public static void removePlayerAcces(Block block,Player player,String addPlayer) {
        Location locBlock = block.getLocation();
        double X = locBlock.getX();
        double Y = locBlock.getY();
        double Z = locBlock.getZ();
        String world = Objects.requireNonNull(locBlock.getWorld()).getName();
        ResultSet result = searchCoffre(block,X,Y,Z,world, player);
        try {
            assert result != null;
            if (result.next()) {
                int idCoffre = result.getInt("id_coffre");
                String UUIDProprio = result.getString("UUID");
                if (UUIDProprio.equals(player.getUniqueId().toString())) {
                    ResultSet searchPlayerAdd = bdd.search("SELECT UUID FROM JOUEUR WHERE pseudo='" + addPlayer + "'");
                    if (searchPlayerAdd.next()) {
                        String UUIDFriend = searchPlayerAdd.getString("UUID");
                        ResultSet resultInBdd = bdd.search("SELECT * FROM ACCEDE WHERE UUID='" + UUIDFriend + "'");
                        if (resultInBdd.next()) {
                            bdd.modifyItems("DELETE FROM ACCEDE WHERE UUID='" + UUIDFriend + "' AND id_coffre=" + idCoffre);
                            player.sendMessage("§2[SecureChest] §aVotre ami(e) n'est plus votre ami(e) (pas sympa ça)");
                        } else {
                            player.sendMessage("§4[SecureChest] §cCette personne n'avais pas accès à ce block");
                        }
                    } else {
                        player.sendMessage("§4[SecureChest] §cVotre ami(e) n'a pas était trouvé :'(");
                    }
                } else {
                    player.setDisplayName("§4[SecureChest] §cVous n'êtes pas le propriétaire");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPlayerAcces(Block block,Player player,String addPlayer) {
        Location locBlock = block.getLocation();
        double X = locBlock.getX();
        double Y = locBlock.getY();
        double Z = locBlock.getZ();
        String world = Objects.requireNonNull(locBlock.getWorld()).getName();
        ResultSet result = searchCoffre(block,X,Y,Z,world, player);
        try {
            assert result != null;
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
    public static void lock(Block block,Player player) {
        Location locBlock = block.getLocation();
        double X = locBlock.getX();
        double Y = locBlock.getY();
        double Z = locBlock.getZ();
        String world = Objects.requireNonNull(locBlock.getWorld()).getName();
        ResultSet result = searchCoffre(block,X,Y,Z,world, player);
        try {
            assert result != null;
            if (!result.next()) {
                bdd.putNewItems("INSERT INTO COFFRE(coordX,coordY,coordZ,monde,UUID) VALUES(" + X + "," + Y+","+Z + ",'" + world + "','" + player.getUniqueId() + "')");
                player.sendMessage("§2[SecureChest] §aCoffre sécurisé");
            } else {
                player.sendMessage("§4 [SecureChest] §cCe coffre est déjà sécurisé");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet searchCoffre(Block block,double X,double Y,double Z,String world,Player player) {
        if (Lockable.inList(block.getType())) {
            return bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                    "WHERE coordX=" + X + " AND coordY=" + Y +
                    " AND coordZ=" + Z + " AND monde='" + world + "'");
        } else {
            player.sendMessage("§4[SecureChest] §cCe block n'est pas protégeable");
        }
        return null;
    }
}
