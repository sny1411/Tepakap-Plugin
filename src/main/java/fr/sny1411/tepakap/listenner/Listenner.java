package fr.sny1411.tepakap.listenner;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.commands.secureChest.Lock;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.larguage.EventsManager;
import fr.sny1411.tepakap.utils.secureChest.Lockable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Listenner implements Listener {
    private final MysqlDb bdd;
    private final Main main;

    public Listenner(MysqlDb bdd, Main main) {
        this.bdd = bdd;
        this.main = main;
    }

    @EventHandler
    private void onPLayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage("§8[§a+§8] §e"+ e.getPlayer().getName());
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Player player = e.getPlayer();
            ResultSet result = bdd.search("SELECT COUNT(*) FROM JOUEUR WHERE UUID='" + player.getUniqueId() + "'");
            if (result == null) {
                Bukkit.getScheduler().runTask(main, () -> {
                    player.kickPlayer("Base de donnée en cours de connexion, veuillez réessayer dans quelques secondes");
                });
                return;
            }
            int nbreBddPlayer = 1;
            try {
                result.next();
                nbreBddPlayer = result.getInt("COUNT(*)");
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            if (nbreBddPlayer == 0) { // nouveau joueur
                player.sendMessage("\n§m--------------" + ChatColor.of("#E6C1F3") + "§lTepakap§r§f§m-------------\n§r \n" +
                                      "§r \u2600 "+ ChatColor.of("#5CB2E5") +"Bienvenue sur le serveur "+ ChatColor.of("#17539C") + player.getName() + " §f\u2600\n \n" +
                                      "§f§m-----------------------------------\n");
                bdd.putNewItems("INSERT INTO JOUEUR VALUES ('"+ player.getUniqueId() + "','" + player.getName() + "','" + datetime+ "','" +datetime + "')");
            } else if (nbreBddPlayer > 0) {
                bdd.modifyItems("UPDATE JOUEUR SET derniere_co='" + datetime + "' WHERE UUID='" + player.getUniqueId() + "'");
            }
            if (player.isOp()) {
                player.sendMessage("§c§l[ADMIN] §r§fBonsoir Chef !");
            }
            if (!Lock.lockAuto.containsKey(player.getUniqueId())) {
                Lock.lockAuto.put(player.getUniqueId(), false);
            }
        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage("§8[§c-§8] §e" + player.getName());
        Lock.lockAuto.remove(player.getUniqueId());
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent e) {
        Location location = e.getInventory().getLocation();
        if (location == null) return;{
        }
        try {
            ResultSet chestBdd = bdd.search("SELECT id_coffre,UUID FROM COFFRE WHERE coordX=" + (int) location.getX() +
                    " AND coordY=" + location.getY() +
                    " AND coordZ=" + (int) location.getZ() +
                    " AND monde='" + location.getWorld().getName() + "'");

            if (chestBdd.next()) { // Si le coffre est dans la bdd
                Player openerPlayer = (Player) e.getPlayer();
                if (openerPlayer.getUniqueId().toString().equalsIgnoreCase(chestBdd.getString("UUID"))) { // Si le joueur n'est pas le propriétaire du coffre
                    return;
                }

                int idChest = chestBdd.getInt("id_coffre");
                ResultSet AccedeBdd = bdd.search("SELECT UUID FROM ACCEDE WHERE id_coffre=" + idChest);
                while (AccedeBdd.next()) {
                    if (openerPlayer.getUniqueId().toString().equalsIgnoreCase(AccedeBdd.getString("UUID"))) {
                        return;
                    }
                }

                openerPlayer.sendMessage("§4[SecureChest] §c Ce coffre est verrouillé !");
                e.setCancelled(true);
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    private void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!Lockable.inList(block.getType())) {
            return;
        }

        Location location = block.getLocation();
        try {
            ResultSet chestBdd = bdd.search("SELECT id_coffre,UUID FROM COFFRE WHERE coordX=" + (int) location.getX() +
                    " AND coordY=" + location.getY() +
                    " AND coordZ=" + (int) location.getZ() +
                    " AND monde='" + location.getWorld().getName() + "'");

            if (chestBdd.next()) { // Si le coffre est dans la bdd
                Player breakerPlayer = e.getPlayer();
                if (breakerPlayer.getUniqueId().toString().equalsIgnoreCase(chestBdd.getString("UUID"))) { // Si le joueur n'est pas le propriétaire du coffre
                    bdd.modifyItems("DELETE FROM COFFRE WHERE id_coffre=" + chestBdd.getInt("id_coffre"));
                } else {
                    breakerPlayer.sendMessage("§4[SecureChest] §c Ce coffre est verrouillé !");
                    e.setCancelled(true); // On annule la destruction du coffre
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @EventHandler
    private void hopperMove(InventoryMoveItemEvent e) {
        Inventory inv = e.getSource();
        if (inv.getType().equals(InventoryType.COMPOSTER)) {
            return;
        }
        if (ChestInBdd(inv.getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExplode1(EntityExplodeEvent e) {
        List<Block> blocksExplode = e.blockList();
        for (Block temp : blocksExplode) {
            if (Lockable.inList(temp.getType())) {
                if (ChestInBdd(temp.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (!Lockable.inList(block.getType())) {
            return;
        }

        Player player = e.getPlayer();
        int xChest = block.getX();
        int yChest = block.getY();
        int zChest = block.getZ();
        String world = block.getWorld().getName();

        if (block.getType().equals(Material.CHEST) || block.getType().equals(Material.TRAPPED_CHEST)) {
            if (ChestInBddOtherPlayer(xChest - 1,yChest,zChest,world,player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
            if (ChestInBddOtherPlayer(xChest + 1,yChest,zChest,world,player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
            if (ChestInBddOtherPlayer(xChest,yChest,zChest - 1,world,player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
            if (ChestInBddOtherPlayer(xChest,yChest,zChest + 1,world,player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            if (Lock.lockAuto.get(player.getUniqueId())) {
                Lock.lock(block,player);
            }
        });
    }

    private boolean ChestInBddOtherPlayer(int x,int y,int z,String world, Player player) {
        ResultSet result = bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                "WHERE coordX=" + x + " AND coordY=" + y +
                " AND coordZ=" + z + " AND monde='" + world + "'");
        try {
            if (result.next()) {
                if (!result.getString("UUID").equals(player.getUniqueId().toString())) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    private boolean ChestInBdd(Location loc) {
        ResultSet result = bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                "WHERE coordX=" + (int) loc.getX() + " AND coordY=" + loc.getY() +
                " AND coordZ=" + (int) loc.getZ() + " AND monde='" + loc.getWorld().getName() + "'");
        try {
            if (result.next()) {
               return true;
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return false;
    }

    @EventHandler
    private void onPlayerClickInv(InventoryClickEvent e) {
        String invName = e.getView().getTitle();
        if (invName.equalsIgnoreCase("§linfo")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerClickArmorStand(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (entity.getType().equals(EntityType.ARMOR_STAND)) {
            e.setCancelled(true);
        }
    }

 }
