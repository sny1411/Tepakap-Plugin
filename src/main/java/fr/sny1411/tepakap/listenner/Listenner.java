package fr.sny1411.tepakap.listenner;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.commands.Lock;
import fr.sny1411.tepakap.commands.Unlock;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.secureChest.Lockable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Listenner implements Listener {
    private final MysqlDb bdd;
    private final Main main;

    public Listenner(MysqlDb bdd, Main main) {
        this.bdd = bdd;
        this.main = main;
    }

    @EventHandler
    public void onPLayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage("§8[§a+§8] §e"+ e.getPlayer().getName());
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Player player = e.getPlayer();
            ResultSet result = bdd.search("SELECT COUNT(*) FROM JOUEUR WHERE UUID='" + player.getUniqueId() + "'");
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
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        e.setQuitMessage("§8[§c-§8] §e" + e.getPlayer().getName());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Player player = (Player) e.getPlayer();
            Location location = e.getInventory().getLocation();
            if (location != null) {
                try {
                    ResultSet result = bdd.search("SELECT id_coffre,UUID FROM COFFRE WHERE coordX=" + location.getX() +
                            " AND coordY=" + location.getY() +
                            " AND coordZ=" + location.getZ() +
                            " AND monde='" + Objects.requireNonNull(location.getWorld()).getName() + "'");
                    if (result.next()) {
                        String playerUUID = player.getUniqueId().toString();
                        if (!playerUUID.equals( result.getString("UUID"))) {
                            String coffreID = result.getString("id_coffre");
                            ResultSet resultAccede = bdd.search("SELECT UUID FROM ACCEDE WHERE id_coffre='" + coffreID + "' AND UUID='" + playerUUID + "'");
                            if (!resultAccede.next()) {
                                player.sendMessage("§4[SecureChest] §cVous n'avez pas les permissions requise pour acceder à cette inventaire");
                                e.setCancelled(true);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExplode1(EntityExplodeEvent e) {
        List<Block> blocksExplode = e.blockList();
        for (Block temp : blocksExplode) {
            if (Lockable.inList(temp.getType())) {
                ResultSet result = bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                        "WHERE coordX=" + temp.getX() + " AND coordY=" + temp.getY() +
                        " AND coordZ=" + temp.getZ() + " AND monde='" + temp.getWorld().getName() + "'");
                try {
                    if (result.next()) {
                        e.setCancelled(true);
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onExplode2(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (Lockable.inList(block.getType())) {
            ResultSet result = bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                    "WHERE coordX=" + block.getX() + " AND coordY=" + block.getY() +
                    " AND coordZ=" + block.getZ() + " AND monde='" + block.getWorld().getName() + "'");
            try {
                if (result.next()) {
                    int idCoffre = result.getInt("id_coffre");
                    if (!e.getPlayer().getUniqueId().toString().equals(result.getString("UUID"))) {
                        e.getPlayer().sendMessage("§4[SecureChest] §cCe coffre est protégé");
                        e.setCancelled(true);
                    } else {
                        bdd.modifyItems("DELETE FROM ACCEDE WHERE id_coffre=" + idCoffre);
                        bdd.modifyItems("DELETE FROM COFFRE WHERE id_coffre=" + idCoffre);
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void onHopperPickup(InventoryMoveItemEvent e) {
        Inventory inv = e.getSource();
        if (!inv.getType().equals(Material.COMPOSTER)) {
            Bukkit.getConsoleSender().sendMessage("test");
            Location invLoc = inv.getLocation();
            ResultSet result = bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                    "WHERE coordX=" + invLoc.getX() + " AND coordY=" + invLoc.getY() +
                    " AND coordZ=" + invLoc.getZ() + " AND monde='" + invLoc.getWorld().getName() + "'");
            try {
                if (result.next()) {
                    e.setCancelled(true);
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void blockPlaceVerif(BlockPlaceEvent e, int x, int y, int z, String world, Block block) {
        ResultSet result = bdd.search("SELECT UUID,id_coffre FROM COFFRE " +
                "WHERE coordX=" + x + " AND coordY=" + y +
                " AND coordZ=" + z + " AND monde='" + world + "'");

        try {
            if (result.next()) {
                if (e.getPlayer().getUniqueId().toString().equals(result.getString("UUID"))) {
                    Lock.lock(block,e.getPlayer());
                } else {
                    e.getPlayer().sendMessage("§4[SecureChest] §cUn coffre proche est sécurisé par une autre personne");
                    e.setCancelled(true);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    @EventHandler
    public void onBlockPlace (BlockPlaceEvent e) {
        Block block = e.getBlock();
        Material blockType = block.getType();
        if (blockType == Material.CHEST || blockType == Material.TRAPPED_CHEST) {
            String world = block.getWorld().getName();
            Location blockLoc = block.getLocation();
            int x = (int) blockLoc.getX();
            int y = (int) blockLoc.getY();
            int z = (int) blockLoc.getZ();

            if (Bukkit.getWorld(world).getBlockAt(x - 1, y, z).getType() == blockType) {
                blockPlaceVerif(e, x - 1, y, z, world, block);
            }
            if (Bukkit.getWorld(world).getBlockAt(x + 1, y, z).getType() == blockType) {
                blockPlaceVerif(e, x + 1, y, z, world, block);
            }
            if (Bukkit.getWorld(world).getBlockAt(x, y, z - 1).getType() == blockType) {
                blockPlaceVerif(e, x, y, z - 1, world, block);
            }
            if (Bukkit.getWorld(world).getBlockAt(x, y, z + 1).getType() == blockType) {
                blockPlaceVerif(e, x, y, z + 1, world, block);
            }
        }
    }

}
