package fr.sny1411.tepakap.listenner;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        Bukkit.getConsoleSender().sendMessage("1");
        Player player = (Player) e.getPlayer();
        Location location = e.getInventory().getLocation();
        Bukkit.getConsoleSender().sendMessage("2");
        try {
            assert location != null;
            ResultSet result = bdd.search("SELECT id_coffre,UUID FROM COFFRE WHERE coordX=" + location.getX() +
                                                                          " AND coordY=" + location.getY() +
                                                                          " AND coordZ=" + location.getZ() +
                                                                          " AND monde='" + Objects.requireNonNull(location.getWorld()).getName() + "'");
            Bukkit.getConsoleSender().sendMessage("3");
            if (result.next()) {
                String playerUUID = player.getUniqueId().toString();
                Bukkit.getConsoleSender().sendMessage("4");
                if (!playerUUID.equals( result.getString("UUID"))) {
                    Bukkit.getConsoleSender().sendMessage("5");
                    String coffreID = result.getString("id_coffre");
                    ResultSet resultAccede = bdd.search("SELECT UUID FROM ACCEDE WHERE id_coffre='" + coffreID + "' AND UUID='" + playerUUID + "'");
                    if (!resultAccede.next()) {
                        Bukkit.getConsoleSender().sendMessage("6");
                        player.sendMessage("§4[SecureChest] §cVous n'avez pas les permissions requise pour acceder à cette inventaire");
                        Bukkit.getConsoleSender().sendMessage("[][][][][][][][]");
                        e.setCancelled(true);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
