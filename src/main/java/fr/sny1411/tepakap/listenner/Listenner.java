package fr.sny1411.tepakap.listenner;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.commands.Competences;
import fr.sny1411.tepakap.commands.Fly;
import fr.sny1411.tepakap.commands.secureChest.Lock;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.Random;
import fr.sny1411.tepakap.utils.capacite.CapaciteManager;
import fr.sny1411.tepakap.utils.larguage.Event;
import fr.sny1411.tepakap.utils.larguage.EventsManager;
import fr.sny1411.tepakap.utils.pioches.Pioche3x3;
import fr.sny1411.tepakap.utils.secureChest.Lockable;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Listenner implements Listener {
    private final MysqlDb bdd;
    private final Main main;

    public Listenner(MysqlDb bdd, Main main) {
        this.bdd = bdd;
        this.main = main;
    }

    @EventHandler
    private void onPLayerJoin(PlayerJoinEvent e) {
        e.setJoinMessage("§8[§a+§8] §e" + e.getPlayer().getName());
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            Player player = e.getPlayer();
            ResultSet result = bdd.search("SELECT COUNT(*) FROM JOUEUR WHERE UUID='" + player.getUniqueId() + "'");
            if (result == null) {
                Bukkit.getScheduler().runTask(main, () -> player.kickPlayer("Base de donnée en cours de connexion, veuillez réessayer dans quelques secondes"));
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
                        "§r ☀ " + ChatColor.of("#5CB2E5") + "Bienvenue sur le serveur " + ChatColor.of("#17539C") + player.getName() + " §f☀\n \n" +
                        "§f§m-----------------------------------\n");
                bdd.putNewItems("INSERT INTO JOUEUR VALUES ('" + player.getUniqueId() + "','" + player.getName() + "','" + datetime + "','" + datetime + "',1)");
            } else if (nbreBddPlayer > 0) {
                bdd.modifyItems("UPDATE JOUEUR SET derniere_co='" + datetime + "' WHERE UUID='" + player.getUniqueId() + "'");
            }
            if (player.isOp()) {
                player.sendMessage("§c§l[ADMIN] §r§fBonsoir Chef !");
            }
            if (!Lock.lockAuto.containsKey(player.getUniqueId())) {
                Lock.lockAuto.put(player.getUniqueId(), false);
            }
            CapaciteManager.chargePlayerCompetences(player.getUniqueId());
        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage("§8[§c-§8] §e" + player.getName());
        Lock.lockAuto.remove(player.getUniqueId());
        Fly.actif.put(player.getUniqueId(),false);
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent e) {
        Location location = e.getInventory().getLocation();
        if (location == null) return;
        {
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
        if (Lockable.inList(block.getType())) {
            return;
        }
        ItemStack itemBreak = e.getPlayer().getInventory().getItemInMainHand();
        if (itemBreak.getType() == Material.AIR || !itemBreak.getItemMeta().hasCustomModelData()) {
            return;
        }
        switch (itemBreak.getType()) {
            case WOODEN_PICKAXE:
                Bukkit.getConsoleSender().sendMessage("here");
                if (itemBreak.getItemMeta().getCustomModelData() == 1) {
                    if (block.getType() == Material.SPAWNER) {
                        e.setDropItems(true);
                    }
                }
                break;
            case DIAMOND_PICKAXE:
                if (itemBreak.getItemMeta().getCustomModelData() == 2) {
                    BlockFace blockFace = e.getPlayer().getTargetBlockFace(6);
                    if (blockFace != null) {
                        Pioche3x3.casser(block, blockFace);
                    }
                }
                break;
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
        if (ChestInBdd(Objects.requireNonNull(inv.getLocation()))) {
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
            if (ChestInBddOtherPlayer(xChest - 1, yChest, zChest, world, player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
            if (ChestInBddOtherPlayer(xChest + 1, yChest, zChest, world, player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
            if (ChestInBddOtherPlayer(xChest, yChest, zChest - 1, world, player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
            if (ChestInBddOtherPlayer(xChest, yChest, zChest + 1, world, player)) {
                player.sendMessage("§4[SecureChest] §cIl y a un coffre verrouillé à proximité par un autre joueur !");
                e.setCancelled(true);
                return;
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            if (Lock.lockAuto.get(player.getUniqueId())) {
                Lock.lock(block, player);
            }
        });
    }

    private boolean ChestInBddOtherPlayer(int x, int y, int z, String world, Player player) {
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
        InventoryType typeGui = e.getInventory().getType();
        if (e.getCurrentItem() == null) {
            return;
        }
        if (invName.equalsIgnoreCase("§linfo")) {
            e.setCancelled(true);
        } else if (invName.equalsIgnoreCase("§lVos compétences")) {
            Material typeItem = e.getCurrentItem().getType();
            Player player = (Player) e.getWhoClicked();
            switch (e.getSlot()) {
                case 22:
                    if (typeItem == Material.BARRIER && e.getCurrentItem().getLore() != null) {
                        // verif items compet 2
                        Inventory playerInventory = player.getInventory();
                        if (playerInventory.contains(Material.LAPIS_BLOCK, 16) && playerInventory.contains(Material.GOLD_BLOCK, 11) && player.getLevel() >= 30) {
                            int lapis = 16;
                            int gold = 11;
                            int i = 0;
                            for (ItemStack itemInv : playerInventory.getContents()) {
                                if (itemInv != null) {
                                    switch (itemInv.getType()) {
                                        case LAPIS_BLOCK:
                                            int numberOfItem = itemInv.getAmount();
                                            lapis = removeToInv(playerInventory, lapis, i, itemInv, numberOfItem);
                                            break;
                                        case GOLD_BLOCK:
                                            numberOfItem = itemInv.getAmount();
                                            gold = removeToInv(playerInventory, gold, i, itemInv, numberOfItem);
                                            break;
                                    }
                                }
                                i++;
                            }
                            player.setLevel(player.getLevel() - 30);
                            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                                bdd.modifyItems("UPDATE JOUEUR SET nbre_capacite=2 WHERE UUID='" + player.getUniqueId() + "'");
                                CapaciteManager.hashMapCapacites.put(player.getUniqueId(), 2);
                                Competences.selecteurCompetences(player);
                            });
                        } else {
                            player.sendMessage("§cVous n'avez pas les ressources requises");
                        }
                        e.setCancelled(true);
                        return;
                    }

                    if (CapaciteManager.hashMapCapacites.get(player.getUniqueId()) >= 2) {
                        Competences.competGui(player, 2);
                        e.setCancelled(true);
                    }
                    break;
                case 29:
                    Competences.competGui(player, 1);
                    break;
                case 33:
                    if (typeItem == Material.BARRIER && e.getCurrentItem().getLore() != null) {
                        // verif items compet 3
                        Inventory playerInventory = player.getInventory();
                        if (playerInventory.contains(Material.LAPIS_BLOCK, 32) && playerInventory.contains(Material.GOLD_BLOCK, 22) && player.getLevel() >= 40 && playerInventory.contains(Material.TOTEM_OF_UNDYING, 1)) {
                            int lapis = 32;
                            int gold = 22;
                            int totem = 1;
                            int i = 0;
                            for (ItemStack itemInv : playerInventory.getContents()) {
                                if (itemInv != null) {
                                    switch (itemInv.getType()) {
                                        case LAPIS_BLOCK:
                                            int numberOfItem = itemInv.getAmount();
                                            lapis = removeToInv(playerInventory, lapis, i, itemInv, numberOfItem);
                                            break;
                                        case GOLD_BLOCK:
                                            numberOfItem = itemInv.getAmount();
                                            gold = removeToInv(playerInventory, gold, i, itemInv, numberOfItem);
                                            break;
                                        case TOTEM_OF_UNDYING:
                                            numberOfItem = itemInv.getAmount();
                                            totem = removeToInv(playerInventory, totem, i, itemInv, numberOfItem);
                                            break;
                                    }
                                }
                                i++;
                            }
                            player.setLevel(player.getLevel() - 40);
                            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                                bdd.modifyItems("UPDATE JOUEUR SET nbre_capacite=3 WHERE UUID='" + player.getUniqueId() + "'");
                                CapaciteManager.hashMapCapacites.put(player.getUniqueId(), 3);
                                Competences.selecteurCompetences(player);
                            });
                        } else {
                            player.sendMessage("§cVous n'avez pas les ressources requises");
                        }
                        e.setCancelled(true);
                        return;
                    }
                    if (CapaciteManager.hashMapCapacites.get(player.getUniqueId()) == 3) {
                        Competences.competGui(player, 3);
                    }
            }
            e.setCancelled(true);
        } else if (invName.equalsIgnoreCase("§lCompétences")) {
            if (e.getSlot() == 45) {
                Competences.selecteurCompetences((Player) e.getWhoClicked());
                e.setCancelled(true);
                return;
            } else if (e.getSlot() == 53) {
                Player player = (Player) e.getWhoClicked();
                int emplacement = Integer.parseInt(Objects.requireNonNull(e.getInventory().getItem(4)).getItemMeta().getDisplayName());
                bdd.modifyItems("DELETE FROM EQUIPE WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                Competences.selecteurCompetences(player);
            }
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                ItemStack clickItem = e.getCurrentItem();
                String nameClickItem = clickItem.getItemMeta().getDisplayName();
                Player player = (Player) e.getWhoClicked();
                int emplacement = Integer.parseInt(Objects.requireNonNull(e.getInventory().getItem(4)).getItemMeta().getDisplayName());
                ResultSet resultEmpOcc = bdd.search("SELECT id_capacite FROM EQUIPE WHERE emplacement=" + emplacement + " AND UUID='" + player.getUniqueId() + "'");
                boolean isEmpOccupe;
                try {
                    isEmpOccupe = resultEmpOcc.next();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                switch (nameClickItem) {
                    case "Yamakasi":
                        int nbrePoule = player.getStatistic(Statistic.KILL_ENTITY, EntityType.CHICKEN);
                        if (nbrePoule >= 1500) {
                            // capa niveau 3
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=1,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',1," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbrePoule >= 900) {
                            // capa niveau 2
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=1,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',1," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbrePoule >= 450) {
                            // capa niveau 1
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=1,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',1," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                    case "Carquois Amélioré":
                        int nbreSquelette = player.getStatistic(Statistic.KILL_ENTITY, EntityType.SKELETON);
                        if (nbreSquelette >= 800) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=2,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',2," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreSquelette >= 400) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=2,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',2," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreSquelette >= 150) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=2,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',2," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                    case "La Torche":
                        int nbreBlaze = player.getStatistic(Statistic.KILL_ENTITY, EntityType.BLAZE);
                        if (nbreBlaze >= 750) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=3,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',3," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreBlaze >= 350) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=3,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',3," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreBlaze >= 150) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=3,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',3," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                    case "Flash":
                        int nbreCheval = player.getStatistic(Statistic.KILL_ENTITY, EntityType.HORSE);
                        if (nbreCheval >= 350) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=4,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',4," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreCheval >= 200) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=4,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',4," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreCheval >= 100) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=4,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',4," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                    case "Balle rebondissante":
                        int nbreLapins = player.getStatistic(Statistic.KILL_ENTITY, EntityType.RABBIT);
                        if (nbreLapins >= 1500) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=5,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',5," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreLapins >= 900) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=5,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',5," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreLapins >= 400) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=5,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',5," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                    case "Inédien":
                        int nbreChampimeuh = player.getStatistic(Statistic.KILL_ENTITY, EntityType.MUSHROOM_COW);
                        if (nbreChampimeuh >= 200) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=6,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',6," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreChampimeuh >= 150) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=6,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',6," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreChampimeuh >= 100) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=6,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',6," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                    case "Poison Ivy":
                        int nbreWitherSkull = player.getStatistic(Statistic.KILL_ENTITY, EntityType.WITHER_SKELETON);
                        if (nbreWitherSkull >= 500) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=7,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',7," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreWitherSkull >= 250) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=7,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',7," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreWitherSkull >= 100) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=7,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',7," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                    case "Superman":
                        int nbreDragon = player.getStatistic(Statistic.KILL_ENTITY, EntityType.ENDER_DRAGON);
                        if (nbreDragon >= 50) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=8,capacite_level=3 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',8," + emplacement + ",3)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreDragon >= 20) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=8,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',8," + emplacement + ",2)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreDragon >= 5) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=8,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',8," + emplacement + ",1)");
                                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                }
            });
            e.setCancelled(true);
        } else if (typeGui == InventoryType.ANVIL || typeGui == InventoryType.SMITHING) {
            if (e.getCurrentItem().getType() == Material.BARRIER) {
                e.setCancelled(true);
            }
        }
    }

    private int removeToInv(Inventory playerInventory, int numberOfDelete, int i, ItemStack itemInv, int numberOfItem) {
        if (numberOfItem > numberOfDelete) {
            itemInv.setAmount(numberOfItem - numberOfDelete);
            playerInventory.setItem(i, itemInv);
            numberOfDelete = 0;
        } else if (numberOfItem < numberOfDelete) {
            playerInventory.setItem(i, null);
            numberOfDelete -= numberOfItem;
        } else {
            playerInventory.setItem(i, null);
            numberOfDelete = 0;
        }
        return numberOfDelete;
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent e) {
        UUID idDeathMob = e.getEntity().getUniqueId();
        Iterator<Event> itrEvent = EventsManager.listEvent.iterator();
        boolean find = false;
        UUID aSupprimer = null;
        Event eventConcerne = null;
        while (itrEvent.hasNext() && !find) {
            Event event = itrEvent.next();
            Iterator<UUID> itrListMobs = event.listMobsId.iterator();
            if (event.listMobsId.contains(idDeathMob)) {
                while (itrListMobs.hasNext() && !find) {
                    UUID idMob = itrListMobs.next();
                    if (idMob.equals(idDeathMob)) {
                        aSupprimer = idMob;
                        eventConcerne = event;
                        event.slayer.sendMessage("Reste : " + (event.listMobsId.size() - 1));
                        find = true;
                    }
                }
            }

            if (find) {
                eventConcerne.listMobsId.remove(aSupprimer);
                if (event.listMobsId.isEmpty()) {
                    bdd.modifyItems("UPDATE LARGUAGE SET UUID='" + event.slayer.getUniqueId() + "',larguage_obtenu=TRUE WHERE UUID_CHEST='" + event.armorStand.getUniqueId() + "'");
                    event.slayer.sendMessage("GG !");
                    ItemStack key = new ItemStack(Material.PAPER);
                    ItemMeta keyMeta = key.getItemMeta();
                    keyMeta.setCustomModelData(2);
                    key.setItemMeta(keyMeta);
                    event.slayer.getInventory().addItem(key);
                    EventsManager.ChestAttack.put(event.armorStand.getUniqueId(), false);
                }
            }
        }
    }

    @EventHandler
    private void onPlayerClickArmorStand(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (entity.getType().equals(EntityType.ARMOR_STAND)) {
            e.getPlayer().sendMessage(String.valueOf(EventsManager.ChestAttack));
            ArmorStand armorStand = (ArmorStand) entity;
            UUID idArmorStand = armorStand.getUniqueId();
            if (EventsManager.ChestAttack.containsKey(idArmorStand)) {
                e.getPlayer().sendMessage(String.valueOf(!EventsManager.ChestAttack.get(idArmorStand)));
                if (!EventsManager.ChestAttack.get(idArmorStand)) {
                    for (Event event : EventsManager.listEvent) {
                        if (event.armorStand.getUniqueId().equals(idArmorStand)) {
                            event.attack();
                            event.slayer = e.getPlayer();
                        }
                    }
                } else {
                    ItemStack itemHand = e.getPlayer().getItemOnCursor();
                    if (itemHand.getItemMeta().getCustomModelData() == 2 && itemHand.getType() == Material.PAPER) {
                        for (Event event : EventsManager.listEvent) {
                            if (event.armorStand.getUniqueId().equals(idArmorStand)) {
                                event.chestDespawn();
                                // Recompenses
                            }
                        }
                    }
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onPrepareAnvilCraft(PrepareAnvilEvent e) {
        ItemStack itemResult = e.getInventory().getResult();
        if (itemResult != null && itemResult.getItemMeta().hasCustomModelData()) {
            Map<Enchantment,Integer> enchats = itemResult.getEnchantments();
            if (enchats.containsKey(Enchantment.MENDING) || enchats.containsKey(Enchantment.SILK_TOUCH) ||  enchats.containsKey(Enchantment.LOOT_BONUS_BLOCKS) || Objects.requireNonNull(e.getInventory().getSecondItem()).getType() == Material.DIAMOND) {
                e.setResult(new ItemStack(Material.BARRIER));
            }
        }
    }

    @EventHandler
    private void onPrepareEnchant(PrepareItemEnchantEvent e) {
        if (e.getItem().getItemMeta().hasCustomModelData()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onPrepareUpgrade(PrepareSmithingEvent e) {
        ItemStack itemResult = e.getResult();
        if (itemResult == null) {
            return;
        }
        if (itemResult.getItemMeta().hasCustomModelData()) {
            e.setResult(new ItemStack(Material.BARRIER));
        }
    }

    @EventHandler
    private void onEntityKill(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            Player killer = e.getEntity().getKiller();
            Collection<Player> playerList = entity.getLocation().getNearbyPlayers(150);
            for (Player player : playerList) {
                if (player != killer) {
                    player.setStatistic(Statistic.KILL_ENTITY, EntityType.ENDER_DRAGON, player.getStatistic(Statistic.KILL_ENTITY, EntityType.ENDER_DRAGON) + 1);
                }
            }
        }
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            UUID uuid = player.getUniqueId();
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (CapaciteManager.isInCapacite("Yamakasi", uuid)) {
                    int levelComp = CapaciteManager.getLevelCapacite("Yamakasi", uuid);
                    int rand = Random.random(1, 100);
                    switch (levelComp) {
                        case 1:
                            if (rand <= 15) {
                                e.setCancelled(true);
                            }
                            break;
                        case 2:
                            if (rand <= 40) {
                                e.setCancelled(true);
                            }
                            break;
                        case 3:
                            e.setCancelled(true);
                            break;
                    }
                }
            } else if (e.getCause() == EntityDamageEvent.DamageCause.LAVA || e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                if (CapaciteManager.isInCapacite("La Torche", uuid)) {
                    int levelComp = CapaciteManager.getLevelCapacite("La Torche", uuid);
                    int rand = Random.random(1, 100);
                    if (e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                        switch (levelComp) {
                            case 1:
                                if (rand <= 75) {
                                    e.setCancelled(true);
                                }
                                break;
                            case 2:
                                if (rand <= 90) {
                                    e.setCancelled(true);
                                }
                                break;
                            case 3:
                                e.setCancelled(true);
                                break;
                        }
                    } else if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                        switch (levelComp) {
                            case 1:
                                if (rand <= 30) {
                                    e.setCancelled(true);
                                }
                                break;
                            case 2:
                                if (rand <= 60) {
                                    e.setCancelled(true);
                                }
                                break;
                            case 3:
                                e.setCancelled(true);
                                break;
                        }
                    }
                }
            } else if (e.getCause() == EntityDamageEvent.DamageCause.WITHER) {
                if (CapaciteManager.isInCapacite("Poison Ivy", uuid)) {
                    int levelComp = CapaciteManager.getLevelCapacite("Poison Ivy", uuid);
                    int rand = Random.random(1, 100);
                    switch (levelComp) {
                        case 1:
                            if (rand <= 30) {
                                e.setCancelled(true);
                            }
                            break;
                        case 2:
                            if (rand <= 60) {
                                e.setCancelled(true);
                            }
                            break;
                        case 3:
                            e.setCancelled(true);
                            break;
                    }
                }
            }
        }
    }

    @EventHandler
    private void entityAttack(ProjectileHitEvent e) {
        if (e.getHitEntity() == null) {
            return;
        }
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();
            UUID playerID = player.getUniqueId();
            if (player.getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.ARROW_INFINITE)) {
                Bukkit.getConsoleSender().sendMessage("arrow ARROW_INFINITE");
                return;
            }
            if (player.getInventory().getItemInOffHand().getEnchantments().containsKey(Enchantment.ARROW_INFINITE)) {
                Bukkit.getConsoleSender().sendMessage("arrow ARROW_INFINITE");
                return;
            }

            if (CapaciteManager.isInCapacite("Carquois Amélioré", playerID)) {
                int levelComp = CapaciteManager.getLevelCapacite("Carquois Amélioré", playerID);
                int rand = Random.random(1, 100);
                switch (levelComp) {
                    case 1:
                        if (rand <= 25) {
                            player.getInventory().addItem(new ItemStack(Material.ARROW));
                        }
                        break;
                    case 2:
                        if (rand <= 50) {
                            player.getInventory().addItem(new ItemStack(Material.ARROW));
                        }
                        break;
                    case 3:
                        player.getInventory().addItem(new ItemStack(Material.ARROW));
                        break;
                }
            }
        }
    }

    @EventHandler
    private void onEat(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        if (player.getFoodLevel() > e.getFoodLevel()) {
            UUID uuid = player.getUniqueId();
            if (CapaciteManager.isInCapacite("Inédien", uuid)) {
                int levelComp = CapaciteManager.getLevelCapacite("Inédien", uuid);
                int rand = Random.random(1, 100);
                switch (levelComp) {
                    case 1:
                        if (rand <= 15) {
                            e.setCancelled(true);
                        }
                        break;
                    case 2:
                        if (rand <= 40) {
                            e.setCancelled(true);
                        }
                        break;
                    case 3:
                        e.setCancelled(true);
                        break;
                }
            }
        }
    }
}
