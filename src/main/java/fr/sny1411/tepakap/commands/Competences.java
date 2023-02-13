package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.SkullCustoms;
import fr.sny1411.tepakap.utils.capacite.CapaciteManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Competences implements CommandExecutor {
    private static MysqlDb bdd;
    private static Main plugin;
    public static HashMap<String,ItemStack> blockCompetences = new HashMap<>();
    private static Inventory invCapa;

    public Competences(MysqlDb bdd, Main plugin) {
        Competences.bdd = bdd;
        Competences.plugin = plugin;
        initBlockCompetences();
        initGuiCapa();
    }

    private void initBlockCompetences() {
        ItemStack chicken = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTYzODQ2OWE1OTljZWVmNzIwNzUzNzYwMzI0OGE5YWIxMWZmNTkxZmQzNzhiZWE0NzM1YjM0NmE3ZmFlODkzIn19fQ");
        ItemMeta chickenMeta = chicken.getItemMeta();
        chickenMeta.setDisplayName("Yamakasi");
        chicken.setItemMeta(chickenMeta);
        blockCompetences.put("Yamakasi", chicken);

        ItemStack skeleton = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWNkNzEzYzVmNWU0NmRhNDM2YThmNTRiNTIzZDQzYWYyOWY3YWU4ZmIxODQ3OTJjY2E3M2IxNzE3ZmVhYTYxIn19fQ");
        ItemMeta skeletonMeta = skeleton.getItemMeta();
        skeletonMeta.setDisplayName("Carquois Amélioré");
        skeleton.setItemMeta(skeletonMeta);
        blockCompetences.put("Carquois Amélioré", skeleton);

        ItemStack blaze = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4ZWYyZTRjZjJjNDFhMmQxNGJmZGU5Y2FmZjEwMjE5ZjViMWJmNWIzNWE0OWViNTFjNjQ2Nzg4MmNiNWYwIn19fQ");
        ItemMeta blazeMeta = blaze.getItemMeta();
        blazeMeta.setDisplayName("La Torche");
        blaze.setItemMeta(blazeMeta);
        blockCompetences.put("La Torche", blaze);

        ItemStack rabbit = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmU1MGFmNTA0MzhhZmZjNjVjZTNiN2E4NWI4ZTgyY2ZiYzNjYWIxOTdjMjVkNGE2MTQyMzBhMmQxZTA2MDVkIn19fQ");
        ItemMeta rabbitMeta = rabbit.getItemMeta();
        rabbitMeta.setDisplayName("Balle rebondissante");
        rabbit.setItemMeta(rabbitMeta);
        blockCompetences.put("Balle rebondissante", rabbit);

        ItemStack witherSkeleton = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta witherSkeletonMeta = witherSkeleton.getItemMeta();
        witherSkeletonMeta.setDisplayName("Poison Ivy");
        witherSkeleton.setItemMeta(witherSkeletonMeta);
        blockCompetences.put("Poison Ivy", witherSkeleton);

        ItemStack horse = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVkYWVhMWZlM2YxMTBlZTcyMmYyYTU5YzFhZmU3YWVjMjMzYzk4YmE0ZGZlM2JhYTI2ZDYwM2ZiOGJiNWI4ZCJ9fX0");
        ItemMeta horseMeta = horse.getItemMeta();
        horseMeta.setDisplayName("Flash");
        horse.setItemMeta(horseMeta);
        blockCompetences.put("Flash", horse);

        ItemStack mushroom = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDBiYzYxYjk3NTdhN2I4M2UwM2NkMjUwN2EyMTU3OTEzYzJjZjAxNmU3YzA5NmE0ZDZjZjFmZTFiOGRiIn19fQ");
        ItemMeta mushroomMeta = mushroom.getItemMeta();
        mushroomMeta.setDisplayName("Inédien");
        mushroom.setItemMeta(mushroomMeta);
        blockCompetences.put("Inédien", mushroom);

        ItemStack enderDragon = new ItemStack(Material.DRAGON_HEAD);
        ItemMeta enderDragonMeta = enderDragon.getItemMeta();
        enderDragonMeta.setDisplayName("Superman");
        enderDragon.setItemMeta(enderDragonMeta);
        blockCompetences.put("Superman", enderDragon);
    }

    private void initGuiCapa() {
        Inventory inv = Bukkit.createInventory(null, 54, "§lCompétences");
        baseGui(inv);
        inv.setItem(21,blockCompetences.get("Yamakasi"));
        inv.setItem(22,blockCompetences.get("Carquois Amélioré"));
        inv.setItem(23,blockCompetences.get("La Torche"));
        inv.setItem(29,blockCompetences.get("Flash"));
        inv.setItem(30,blockCompetences.get("Balle rebondissante"));
        inv.setItem(31,blockCompetences.get("Inédien"));
        inv.setItem(32,blockCompetences.get("Poison Ivy"));
        inv.setItem(33,blockCompetences.get("Superman"));

        ItemStack btnRetour = new ItemStack(SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ViZjkwNzQ5NGE5MzVlOTU1YmZjYWRhYjgxYmVhZmI5MGZiOWJlNDljNzAyNmJhOTdkNzk4ZDVmMWEyMyJ9fX0"));
        ItemMeta btnRetourMeta = btnRetour.getItemMeta();
        btnRetourMeta.setDisplayName("Retour");
        btnRetour.setItemMeta(btnRetourMeta);
        inv.setItem(45,btnRetour);

        ItemStack btnResetCapa = new ItemStack(Material.STRUCTURE_VOID);
        ItemMeta btnResetMeta = btnResetCapa.getItemMeta();
        btnResetMeta.setDisplayName("Ne rien attribuer");
        btnResetCapa.setItemMeta(btnResetMeta);
        inv.setItem(53,btnResetCapa);

        invCapa = inv;
    }

    private static void baseGui(Inventory inv) {
        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta metaGlassPane = glassPane.getItemMeta();
        metaGlassPane.setDisplayName(" ");
        glassPane.setItemMeta(metaGlassPane);

        for (int i = 0; i < 54; i++) {
            if (i <= 9 || i >= 46) {
                inv.setItem(i, glassPane);
            } else if (i%9==0 || i%9 == 8) {
                inv.setItem(i, glassPane);
            }
        }

    }

    public static void competGui(Player player, int nbCompetence) {
        ItemStack nbCapa;
        if (nbCompetence == 1) {
            nbCapa = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0");
            ItemMeta nbCapaMeta = nbCapa.getItemMeta();
            nbCapaMeta.setDisplayName(String.valueOf(nbCompetence));
            nbCapa.setItemMeta(nbCapaMeta);
        } else if (nbCompetence == 2) {
            nbCapa = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19");
            ItemMeta nbCapaMeta = nbCapa.getItemMeta();
            nbCapaMeta.setDisplayName(String.valueOf(nbCompetence));
            nbCapa.setItemMeta(nbCapaMeta);
        } else {
            nbCapa = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19");
            ItemMeta nbCapaMeta = nbCapa.getItemMeta();
            nbCapaMeta.setDisplayName(String.valueOf(nbCompetence));
            nbCapa.setItemMeta(nbCapaMeta);
        }
        invCapa.setItem(4, nbCapa);
        player.openInventory(invCapa);
    }

    public static void selecteurCompetences(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Bukkit.getConsoleSender().sendMessage("ici ^^");
            Inventory inv = Bukkit.createInventory(null, 54, "§lVos compétences");
            baseGui(inv);
            ItemStack[] blockCapacite = new ItemStack[3];
            try {
                ResultSet result = bdd.search("SELECT id_capacite,emplacement FROM EQUIPE WHERE UUID='" + player.getUniqueId() + "'");
                Bukkit.getConsoleSender().sendMessage("ici 3^^");
                while (!result.isClosed() && result.next()) {
                    int id_capa = result.getInt("id_capacite");
                    int emplacement = result.getInt("emplacement");
                    blockCapacite[emplacement - 1] = blockCompetences.get(CapaciteManager.dicCapa.get(id_capa));
                }
                result.close();
            } catch (SQLException e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage("ici 3^^^");
            }

            ItemStack caseVide = new ItemStack(Material.BEACON);
            ItemMeta structureVoidMeta = caseVide.getItemMeta();
            structureVoidMeta.setDisplayName("Capacité vide");
            structureVoidMeta.setLore(new ArrayList<>(Arrays.asList("Cliquez pour ajouter", "une compétence")));
            caseVide.setItemMeta(structureVoidMeta);

            if (blockCapacite[0] != null) {
                inv.setItem(29, blockCapacite[0]);
            } else {
                inv.setItem(29, caseVide);
            }
            int nbreCapa = CapaciteManager.hashMapCapacites.get(player.getUniqueId());
            if (nbreCapa > 1) {
                if (blockCapacite[1] != null) {
                    inv.setItem(22, blockCapacite[1]);
                } else {
                    inv.setItem(22, caseVide);
                }
            } else {
                ItemStack barrier = new ItemStack(Material.BARRIER);
                ItemMeta barrierMeta = barrier.getItemMeta();
                barrierMeta.setDisplayName("Capacité bloquée");
                barrierMeta.setLore(new ArrayList<>(Arrays.asList("Cliquez pour la déverrouiller", "Liste des requis :", "- 16 blocs de lapis",
                        "- 11 blocs d'or",
                        "- 30 niveaux d'xp")));
                barrier.setItemMeta(barrierMeta);
                inv.setItem(22, barrier);
            }
            if (nbreCapa > 2) {
                if (blockCapacite[2] != null) {
                    inv.setItem(33, blockCapacite[2]);
                } else {
                    inv.setItem(33, caseVide);
                }
            } else {
                ItemStack barrier = new ItemStack(Material.BARRIER);
                ItemMeta barrierMeta = barrier.getItemMeta();
                barrierMeta.setDisplayName("Capacité bloquée");
                if (nbreCapa != 1) {
                    barrierMeta.setLore(new ArrayList<>(Arrays.asList("Cliquez pour la déverrouiller",
                            "Liste des requis :",
                            "- 32 blocs de lapis",
                            "- 22 blocs d'or",
                            "- 40 niveaux d'xp",
                            "- 1 totem d'immortalité")));
                }
                barrier.setItemMeta(barrierMeta);
                inv.setItem(33, barrier);
            }
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inv));
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            selecteurCompetences((Player) commandSender);
        }
        return false;
    }
}
