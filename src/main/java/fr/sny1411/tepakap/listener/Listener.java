package fr.sny1411.tepakap.listener;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.commands.Competences;
import fr.sny1411.tepakap.commands.Fly;
import fr.sny1411.tepakap.commands.secureChest.Lock;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.BlocksUtils;
import fr.sny1411.tepakap.utils.Random;
import fr.sny1411.tepakap.utils.Teleporteur;
import fr.sny1411.tepakap.utils.capacite.CapaciteManager;
import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import fr.sny1411.tepakap.utils.larguage.Event;
import fr.sny1411.tepakap.utils.larguage.EventsManager;
import fr.sny1411.tepakap.utils.maire.GuiMaire;
import fr.sny1411.tepakap.utils.pioches.Pioche3x3;
import fr.sny1411.tepakap.utils.pioches.PiocheIncinerator;
import fr.sny1411.tepakap.utils.secureChest.Lockable;
import io.papermc.lib.PaperLib;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Listener implements org.bukkit.event.Listener {
    private final MysqlDb bdd;
    private final Main main;

    public Listener(MysqlDb bdd, Main main) {
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
            try {
                CapaciteManager.chargePlayerCompetences(player.getUniqueId());
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            player.setAllowFlight(false);
            player.setFlying(false);

            Teleporteur.coolDown.put(player.getUniqueId(),false);

        });
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage("§8[§c-§8] §e" + player.getName());
        Lock.lockAuto.remove(player.getUniqueId());
        Fly.actif.put(player.getUniqueId(), false);
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent e) {
        Location location = e.getInventory().getLocation();
        if (location == null) return;
        try {
            ResultSet chestBdd = bdd.search("SELECT id_coffre,UUID FROM COFFRE WHERE coordX=" + (int) location.getX() +
                    " AND coordY=" + location.getY() +
                    " AND coordZ=" + (int) location.getZ() +
                    " AND monde='" + location.getWorld().getName() + "';");

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
        Player player = e.getPlayer();

        if (block.getType() == Material.LODESTONE) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            try {
                ResultSet result = bdd.search("SELECT UUID FROM TELEPORTEUR WHERE (X1=" + x + " AND Y1=" + y + " AND Z1=" + z + ") OR (X2=" + x + " AND Y2=" + y + " AND Z2=" + z + ")");
                if (result.next()) {
                    if (result.getString("UUID").equals(player.getUniqueId().toString())) {
                        bdd.modifyItems("DELETE FROM TELEPORTEUR WHERE (X1=" + x + " AND Y1=" + y + " AND Z1=" + z + ") OR (X2=" + x + " AND Y2=" + y + " AND Z2=" + z + ")");
                        player.getInventory().addItem(new ItemStack(Material.DIAMOND_BLOCK,2));
                    } else {
                        player.sendMessage("§cVous ne pouvez pas casser ce téléporteur");
                        e.setCancelled(true);
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        ItemStack itemBreak = player.getInventory().getItemInMainHand();
        if (itemBreak.getType() == Material.AIR) {
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

        switch (itemBreak.getType()) {
            case WOODEN_PICKAXE:
                if (!itemBreak.getItemMeta().hasCustomModelData()) {
                    return;
                }
                if (itemBreak.getItemMeta().getCustomModelData() == 1) {
                    if (block.getType() == Material.SPAWNER) {
                        CreatureSpawner spawner = (CreatureSpawner) block.getState();
                        EntityType entitySpawner = spawner.getSpawnedType();
                        ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
                        ItemMeta metaSpawner = spawnerItem.getItemMeta();
                        metaSpawner.setLore(new ArrayList<>(Arrays.asList(entitySpawner.name())));
                        spawnerItem.setItemMeta(metaSpawner);

                        block.getWorld().dropItemNaturally(block.getLocation(), spawnerItem);
                    }
                }
                break;
            case DIAMOND_PICKAXE:
                if (!itemBreak.getItemMeta().hasCustomModelData()) {
                    return;
                }
                ItemMeta metaItem = itemBreak.getItemMeta();
                if (metaItem.getCustomModelData() == 2) {
                    BlockFace blockFace = player.getTargetBlockFace(6);
                    if (blockFace != null) {
                        Pioche3x3.casser(block, blockFace);
                    }
                } else if (metaItem.getCustomModelData() == 1) {
                    // pioche incinerator
                    if (PiocheIncinerator.listBlock.contains(block.getType())) {
                        e.setDropItems(false);
                        Map<Enchantment,Integer> enchantments = itemBreak.getEnchantments();
                        PiocheIncinerator.blockBreak(block, enchantments.get(Enchantment.LOOT_BONUS_BLOCKS));
                    }
                }
                break;
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
        if (e.getEntity().getType() == EntityType.CREEPER) {
            e.setCancelled(true);
        }
        List<Block> blocksExplode = e.blockList();
        for (Block temp : blocksExplode) {
            if (temp.getType() == Material.LODESTONE) {
                int x = temp.getX();
                int y = temp.getY();
                int z = temp.getZ();
                try {
                    ResultSet result = bdd.search("SELECT UUID FROM TELEPORTEUR WHERE (X1=" + x + " AND Y1=" + y + " AND Z1=" + z + ") OR (X2=" + x + " AND Y2=" + y + " AND Z2=" + z + ")");
                    if (result.next()) {
                        e.setCancelled(true);
                        return;
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (Lockable.inList(temp.getType())) {
                if (ChestInBdd(temp.getLocation())) {
                    e.setCancelled(true);
                    return;
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
        if (e.getCurrentItem() == null) {
            return;
        }
        if (invName.equalsIgnoreCase("§linfo")) {
            e.setCancelled(true);
        } else if (invName.equalsIgnoreCase("§8§lChoix Bonus")) {
            ItemStack itemClick = e.getCurrentItem();
            String nameItemClick = itemClick.getItemMeta().getDisplayName();
            String nbCase = Objects.requireNonNull(e.getInventory().getItem(4)).getItemMeta().getDisplayName();
            Player player = (Player) e.getWhoClicked();
            if (GuiMaire.itemBonus.containsKey(nameItemClick)) {
                switch (nameItemClick) {
                    case "Pilleur de trésors":
                        ResultSet result = bdd.search("SELECT COUNT(*) FROM LARGUAGE WHERE UUID='" + player.getUniqueId() + "'");
                        try {
                            if (result.next()) {
                                int n = result.getInt("COUNT(*)");
                                if (n < 8) {
                                    player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                                    e.setCancelled(true);
                                    return;
                                }
                            } else {
                                player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                                e.setCancelled(true);
                                return;
                            }
                        } catch (SQLException ex) {
                            e.setCancelled(true);
                            ex.printStackTrace();
                            return;
                        }
                        break;
                    case "Chasseur de démons":
                        int nbreWither = player.getStatistic(Statistic.KILL_ENTITY,EntityType.WITHER_SKELETON);
                        if (nbreWither < 150) {
                            player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                            e.setCancelled(true);
                            return;
                        }
                        break;
                    case "Bûcheron":
                        int nbreAxeBreak = player.getStatistic(Statistic.BREAK_ITEM,Material.DIAMOND_AXE);
                        if (nbreAxeBreak == 0) {
                            player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                            e.setCancelled(true);
                            return;
                        }
                        break;
                    case "Mineur":
                        int nbrePiocheBreak = player.getStatistic(Statistic.BREAK_ITEM,Material.DIAMOND_PICKAXE);
                        if (nbrePiocheBreak == 0) {
                            player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                            e.setCancelled(true);
                            return;
                        }
                        break;
                    case "Négociateur":
                        ResultSet result2 = bdd.search("SELECT id FROM PRESENTATION_MAIRE WHERE (id_bonus_1=8 OR id_bonus_2=8 OR id_bonus_3=8) AND UUID='" + player.getUniqueId() + "'");
                        try {
                            if (!result2.next()) {
                                player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                                e.setCancelled(true);
                                return;
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                            e.setCancelled(true);
                            return;
                        }
                        break;
                    case "Généreux":
                        ResultSet result3 = bdd.search("SELECT id FROM DECORATION WHERE UUID='" + player.getUniqueId() + "'");
                        try {
                            if (!result3.next()) {
                                player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                                e.setCancelled(true);
                                return;
                            }
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    case "Arrache-Coeur":
                        int nbreWarden = player.getStatistic(Statistic.KILL_ENTITY,EntityType.WARDEN);
                        if (nbreWarden == 0) {
                            player.sendMessage("§cVous ne remplissez pas les pré-requis pour ce bonus");
                            e.setCancelled(true);
                            return;
                        }
                        break;

                }
                GuiMaire.tempPresentation.get(player.getUniqueId()).put(Integer.valueOf(nbCase),nameItemClick);
                GuiMaire.openGuiSePresenter(player);
            }
            e.setCancelled(true);
        } else if (invName.equalsIgnoreCase("§8§lSe présenter maire")) {
            int slot = e.getSlot();
            Player player = (Player) e.getWhoClicked();
            switch (slot) {
                case 11:
                    // open gui choix
                    GuiMaire.openGuiChoisir(player,1);
                    break;
                case 13:
                    GuiMaire.openGuiChoisir(player,2);
                    // open gui choix
                    break;
                case 15:
                    GuiMaire.openGuiChoisir(player,3);
                    // open gui choix
                    break;
                case 22:
                    HashMap<Integer,String> hashTemp = GuiMaire.tempPresentation.get(player.getUniqueId());
                    if (hashTemp.keySet().size() == 3) {
                        Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin, () -> {
                            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                            bdd.putNewItems("INSERT INTO PRESENTATION_MAIRE(date_presentation,nbElection,UUID,id_bonus_1,id_bonus_2,id_bonus_3) VALUES ('" + datetime + "'," + GuiMaire.nbElection + ",'" + player.getUniqueId() + "'," + GuiMaire.hashIdBonus.get(hashTemp.get(1)) + "," + GuiMaire.hashIdBonus.get(hashTemp.get(2)) + "," + GuiMaire.hashIdBonus.get(hashTemp.get(3)) + ")");
                            Bukkit.getScheduler().runTask(ClockEvents.plugin, () -> {
                                player.closeInventory();
                            });
                            player.sendMessage("§aVotre candidature est enregistrée");
                            player.sendMessage("§aBonne chance !");
                        });
                    } else {
                        player.sendMessage("§cLe choix de vos bonus n'est pas complet");
                    }
                    break;
            }
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
                try {
                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',1," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbrePoule >= 900) {
                            // capa niveau 2
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=1,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',1," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbrePoule >= 450) {
                            // capa niveau 1
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=1,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',1," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',2," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreSquelette >= 400) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=2,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',2," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreSquelette >= 150) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=2,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',2," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',3," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreBlaze >= 350) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=3,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',3," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreBlaze >= 150) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=3,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',3," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',4," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreCheval >= 200) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=4,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',4," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreCheval >= 100) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=4,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',4," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',5," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreLapins >= 900) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=5,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',5," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreLapins >= 400) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=5,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',5," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',6," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreChampimeuh >= 150) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=6,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',6," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreChampimeuh >= 100) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=6,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',6," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',7," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreWitherSkull >= 250) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=7,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',7," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreWitherSkull >= 100) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=7,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',7," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
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
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',8," + emplacement + ",3)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreDragon >= 20) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=8,capacite_level=2 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',8," + emplacement + ",2)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else if (nbreDragon >= 5) {
                            if (isEmpOccupe) {
                                bdd.modifyItems("UPDATE EQUIPE SET id_capacite=8,capacite_level=1 WHERE UUID='" + player.getUniqueId() + "' AND emplacement=" + emplacement);
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            } else {
                                bdd.putNewItems("INSERT INTO EQUIPE VALUES ('" + player.getUniqueId() + "',8," + emplacement + ",1)");
                                try {
                                    CapaciteManager.chargePlayerCompetences(player.getUniqueId());
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            Competences.selecteurCompetences(player);
                        } else {
                            player.sendMessage("§cVous ne possédez pas cette capacité !");
                        }
                        break;
                }
            });
            e.setCancelled(true);
        } else if (e.getCurrentItem().getType() == Material.BARRIER) {
           e.setCancelled(true);
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
        if (e.getEntity().getType() == EntityType.WARDEN) {
            int rand = Random.random(0,100);
            if (rand >= 50) {
                ItemStack coeur = new ItemStack(Material.PRISMARINE_CRYSTALS);
                ItemMeta coeurMeta = coeur.getItemMeta();
                coeurMeta.setCustomModelData(1);
                coeurMeta.setDisplayName("§aCoeur de Warden");
                coeur.setItemMeta(coeurMeta);
                e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(),coeur);
            }
        }
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
                    EventsManager.ChestAttack.put(event.armorStand.getUniqueId(),true);
                    EventsManager.ChestAttack.put(event.armorStand.getUniqueId(), false);
                }
            }
        }
    }

    @EventHandler
    private void onPlayerClickArmorStand(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (entity.getType().equals(EntityType.ARMOR_STAND)) {
            ArmorStand armorStand = (ArmorStand) entity;
            UUID idArmorStand = armorStand.getUniqueId();

            if (EventsManager.EventFinish.containsKey(idArmorStand)) {
                ItemStack itemHand = e.getPlayer().getItemOnCursor();
                if (!itemHand.hasItemMeta() && !itemHand.getItemMeta().hasCustomModelData()) {
                    return;
                }
                if (itemHand.getItemMeta().getCustomModelData() == 2 && itemHand.getType() == Material.PAPER) {
                    for (Event event : EventsManager.listEvent) {
                        if (event.armorStand.getUniqueId().equals(idArmorStand)) {
                            event.chestDespawn();
                            switch (event.rarete) {
                                case COMMUN:
                                    int rand = Random.random(0,100);
                                    if (rand > 70) {
                                        ItemStack batterie = new ItemStack(Material.PAPER);
                                        ItemMeta batterieMeta = batterie.getItemMeta();
                                        batterieMeta.setCustomModelData(3);
                                        batterieMeta.setDisplayName("Batterie");
                                        batterieMeta.setLore(new ArrayList<>(Arrays.asList("Rend 90 de dura à la pioche surchargée")));
                                        batterie.setItemMeta(batterieMeta);
                                        event.slayer.getInventory().addItem(batterie);
                                    } else if (rand > 40) {
                                        int rand2 = Random.random(0,60);
                                        if (rand2 > 40) {
                                            event.slayer.getInventory().addItem(new ItemStack(Material.OCHRE_FROGLIGHT,8));
                                        } else if (rand2 > 20) {
                                            event.slayer.getInventory().addItem(new ItemStack(Material.VERDANT_FROGLIGHT, 8));
                                        } else {
                                            event.slayer.getInventory().addItem(new ItemStack(Material.PEARLESCENT_FROGLIGHT,8));
                                        }
                                    } else {
                                        ItemStack braise = new ItemStack(Material.BLAZE_POWDER);
                                        ItemMeta metaBraise = braise.getItemMeta();
                                        metaBraise.setDisplayName("§6Braise miraculeuse");
                                        metaBraise.setLore(new ArrayList<>(Arrays.asList("Rend 100 de dura à l'incinerator")));
                                        metaBraise.setCustomModelData(1);
                                        braise.setItemMeta(metaBraise);
                                        event.slayer.getInventory().addItem(braise);
                                    }
                                    break;
                                case RARE:
                                    break;
                                case EPIQUE:
                                    break;
                                case LEGENDAIRE:
                                    break;
                            }
                            // Recompenses
                        }
                    }
                }
            }

            if (EventsManager.ChestAttack.containsKey(idArmorStand)) {
                if (!EventsManager.ChestAttack.get(idArmorStand)) {
                    for (Event event : EventsManager.listEvent) {
                        if (event.armorStand.getUniqueId().equals(idArmorStand)) {
                            event.attack();
                            event.slayer = e.getPlayer();
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
        ItemStack secondItem = e.getInventory().getSecondItem();
        ItemStack firstItem = e.getInventory().getFirstItem();
        if (secondItem == null || firstItem == null) {
            return;
        }
        if (itemResult != null && itemResult.getItemMeta().hasCustomModelData()) {
            Map<Enchantment, Integer> enchants = itemResult.getEnchantments();
            int nData = itemResult.getItemMeta().getCustomModelData();

            if (itemResult.getType() == Material.WOODEN_PICKAXE) {
                e.setResult(BlocksUtils.itemBarrier);
                return;
            }
            if (secondItem.getType() == Material.DIAMOND || enchants.containsKey(Enchantment.MENDING) || enchants.containsKey(Enchantment.SILK_TOUCH) ) {
                e.setResult(BlocksUtils.itemBarrier);
                return;
            }

            if (!(itemResult.getType() == Material.DIAMOND_PICKAXE && nData == 1)) {
                if (enchants.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
                    e.setResult(BlocksUtils.itemBarrier);
                }
            }
        } else {
            if (firstItem.getType() == Material.DIAMOND_PICKAXE && firstItem.getItemMeta().hasCustomModelData() && firstItem.getItemMeta().getCustomModelData() == 1) {
                if (secondItem.getType() == Material.BLAZE_POWDER && secondItem.getItemMeta().hasCustomModelData()) {
                    ItemStack firstItemCopy = firstItem.clone();
                    Damageable damage = (Damageable) firstItemCopy.getItemMeta();
                    if ((damage.getDamage()-100) > 0) {
                        damage.setDamage(damage.getDamage()-100);
                    } else {
                        damage.setDamage(0);
                    }
                    firstItemCopy.setItemMeta(damage);
                    e.setResult(firstItemCopy);
                }
            }
        }
    }
/*
else {
                Bukkit.getConsoleSender().sendMessage("ici");
                if (secondItem.getType() == Material.BLAZE_POWDER && secondItem.getItemMeta().hasCustomModelData()) {
                    Bukkit.getConsoleSender().sendMessage("ici2");
                    Damageable damage = (Damageable) itemResult.getItemMeta();
                    if ((damage.getDamage()-100) <= itemResult.getMaxItemUseDuration()) {
                        damage.setDamage(damage.getDamage()-100);
                    } else {
                        damage.setDamage(0);
                    }
                    itemResult.setItemMeta(damage);
                    e.setResult(itemResult);
                }
            }
 */
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
            e.setResult(BlocksUtils.itemBarrier);
        }
    }

    @EventHandler
    private void onEntityKill(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        if (entity.getType() == EntityType.ENDER_DRAGON) {
            Player killer = e.getEntity().getKiller();
            Collection<Player> playerList = new Location(entity.getWorld(), 0.0,62.0,0.0).getNearbyPlayers(250);
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
                return;
            }
            if (player.getInventory().getItemInOffHand().getEnchantments().containsKey(Enchantment.ARROW_INFINITE)) {
                return;
            }

            if (e.getEntity().getType() != EntityType.ARROW) {
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

    /*@EventHandler (priority = EventPriority.HIGH) // BUG FLY INFINI
    private void setVelocity(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        UUID playerID = p.getUniqueId();
        if (Fly.actif.containsKey(playerID) && Fly.actif.get(playerID)) {
            return;
        }

        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isFlying()) {
            return;
        }

        if (CapaciteManager.isInCapacite("Balle rebondissante", playerID)) {
            if (CapaciteManager.getLevelCapacite("Balle rebondissante", playerID) != 3) {
                return;
            }
            p.setAllowFlight(false);
            p.setFlying(false);
            p.setVelocity(p.getLocation().getDirection().multiply(1).setY(0.9));
            p.playEffect(p.getLocation(), Effect.BLAZE_SHOOT, 15);
            e.setCancelled(true);
            Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin, () -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(2500);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                p.setAllowFlight(true);
            });
        }
        e.setCancelled(true);
    }*/

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if (block.getType() == Material.LECTERN) {
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                return;
            }
            if (GuiMaire.presentoir != null && block.getLocation().equals(GuiMaire.presentoir)) {
                if (GuiMaire.presentationEnCour) {
                    GuiMaire.openGuiSePresenter(e.getPlayer());
                } else if (GuiMaire.voteEnCour) {
                    // open vote
                }
            }
        } else if (block.getType() == Material.LODESTONE) {
            Player player = e.getPlayer();
            ItemStack itemPlayer = player.getInventory().getItemInMainHand();
            if (itemPlayer.getType() == Material.DIAMOND_BLOCK) {
                if (!player.getWorld().getName().equalsIgnoreCase("world")) {
                    player.sendMessage("§cLes téléporteur ne sont utilisable que dans l'overworld");
                    return;
                }
                if (Teleporteur.tempLoc.containsKey(player.getUniqueId())) {
                    itemPlayer.setAmount(itemPlayer.getAmount()-1);
                    player.getInventory().setItemInMainHand(itemPlayer);
                    Location loc1 = Teleporteur.tempLoc.get(player.getUniqueId());
                    Location loc2 = block.getLocation();
                    Teleporteur.tempLoc.remove(player.getUniqueId());
                    bdd.putNewItems("INSERT INTO TELEPORTEUR(X1,X2,Y1,Y2,Z1,Z2,UUID) VALUES (" + loc1.getX() + "," + loc2.getX() + "," + loc1.getY() + "," + loc2.getY() + "," + loc1.getZ() + "," + loc2.getZ() + ",'" + player.getUniqueId() + "')");
                    player.sendMessage("§aTéléporteur link");
                    Bukkit.getConsoleSender().sendMessage("§a[Teleporteur] " + player.getName() + " tp link");
                    e.setCancelled(true);
                } else {
                    player.getInventory().setItemInMainHand(null);
                    Teleporteur.tempLoc.put(player.getUniqueId(),block.getLocation());
                    player.sendMessage("§aTéléporteur enregistré");
                    player.sendMessage("§aCliquez avec un bloc de diamant sur l'autre pour l'associer");
                    Bukkit.getConsoleSender().sendMessage("§a[Teleporteur] " + player.getName() + " tp register");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onPortalCreateEvent(PortalCreateEvent e) {
        if (e.getWorld() == Bukkit.getWorld("world_nether")) {
            Location loc = e.getBlocks().get(0).getLocation();
            int x = (int) loc.getX();
            int z = (int) loc.getZ();
            if ((x > 1870 || x < -1870) || (z > 1870 || z < -1870)) {
                if (e.getEntity() instanceof Player) {
                    Player player = (Player) e.getEntity();
                    player.sendMessage("§cVotre portail se trouve trop loin");
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (player.isSneaking()) {
            Block block = player.getLocation().subtract(0, 1, 0).getBlock();
            if (block != null && block.getType() == Material.LODESTONE) {
                Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin,() -> {
                    Location loc = block.getLocation();
                    if (Teleporteur.coolDown.get(player.getUniqueId())) {
                        player.sendMessage("§cTéléporteur en surchauffe");
                        return;
                    }
                    try {
                        ResultSet result = bdd.search("SELECT X1,Y1,Z1 FROM TELEPORTEUR WHERE X2=" + loc.getX() + " AND Y2=" + loc.getY() + " AND Z2=" + loc.getZ());
                        if (result.next()) {
                            int x = result.getInt("X1");
                            int y = result.getInt("Y1");
                            int z = result.getInt("Z1");
                            PaperLib.teleportAsync(player,new Location(Bukkit.getWorld("world"),x+0.5,y+1,z+0.5));
                            Teleporteur.coolDown.put(player.getUniqueId(),true);
                            TimeUnit.SECONDS.sleep(3);
                            Teleporteur.coolDown.put(player.getUniqueId(),false);
                        } else {
                            ResultSet result2 = bdd.search("SELECT X2,Y2,Z2 FROM TELEPORTEUR WHERE X1=" + loc.getX() + " AND Y1=" + loc.getY() + " AND Z1=" + loc.getZ());
                            if (result2.next()) {
                                int x = result2.getInt("X2");
                                int y = result2.getInt("Y2");
                                int z = result2.getInt("Z2");
                                PaperLib.teleportAsync(player,new Location(Bukkit.getWorld("world"),x+0.5,y+1,z+0.5));
                                Teleporteur.coolDown.put(player.getUniqueId(),true);
                                TimeUnit.SECONDS.sleep(3);
                                Teleporteur.coolDown.put(player.getUniqueId(),false);
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
    }

    @EventHandler
    private void portalTravel(PlayerPortalEvent e) {
        Bukkit.getConsoleSender().sendMessage(e.getPlayer().getName() + " portail traverse en : " + e.getTo());
    }

}
