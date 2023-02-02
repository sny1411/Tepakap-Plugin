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
import java.util.List;

public class Competences implements CommandExecutor {
    private static MysqlDb bdd;
    private static Main plugin;
    public static HashMap<String,ItemStack> blockCompetences = new HashMap<>();
    private static Inventory invCapa;

    public Competences(MysqlDb bdd, Main plugin) {
        Competences.bdd = bdd;
        this.plugin = plugin;
        initBlockCompetences();
        initGuiCapa();
    }

    private void initBlockCompetences() {
        ItemStack chicken = SkullCustoms.getCustomSkull("ca3582ce4889333dad329e4e24372a03a5daa2c34280c56256af5283edb043f8");
        ItemMeta chickenMeta = chicken.getItemMeta();
        chickenMeta.setDisplayName("Yamakasi");
        chicken.setItemMeta(chickenMeta);
        blockCompetences.put("Yamakasi", chicken);

        ItemStack skeleton = SkullCustoms.getCustomSkull("d5a5839f179798cd3e7b09c371057665fa26f9369e267ffd471f0e78d4a65624");
        ItemMeta skeletonMeta = skeleton.getItemMeta();
        skeletonMeta.setDisplayName("Carquois Amélioré");
        skeleton.setItemMeta(skeletonMeta);
        blockCompetences.put("Carquois Amélioré", skeleton);

        ItemStack blaze = SkullCustoms.getCustomSkull("b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0");
        ItemMeta blazeMeta = blaze.getItemMeta();
        blazeMeta.setDisplayName("La Torche");
        blaze.setItemMeta(blazeMeta);
        blockCompetences.put("La Torche", blaze);

        ItemStack rabbit = SkullCustoms.getCustomSkull("234cd6bbec5b4976f93e3f2415567248626877f17baf89a37220506f5957b97c");
        ItemMeta rabbitMeta = rabbit.getItemMeta();
        rabbitMeta.setDisplayName("Balle rebondissante");
        rabbit.setItemMeta(rabbitMeta);
        blockCompetences.put("Balle rebondissante", rabbit);

        ItemStack witherSkeleton = SkullCustoms.getCustomSkull("7953b6c68448e7e6b6bf8fb273d7203acd8e1be19e81481ead51f45de59a8");
        ItemMeta witherSkeletonMeta = witherSkeleton.getItemMeta();
        witherSkeletonMeta.setDisplayName("Poison Ivy");
        witherSkeleton.setItemMeta(witherSkeletonMeta);
        blockCompetences.put("Poison Ivy", witherSkeleton);

        ItemStack horse = SkullCustoms.getCustomSkull("5300fc691073e5cd9f41eaf36388dee7fa7233e7dea4e38c3b9fb52401a4aabb");
        ItemMeta horseMeta = horse.getItemMeta();
        horseMeta.setDisplayName("Flash");
        horse.setItemMeta(horseMeta);
        blockCompetences.put("Flash", horse);

        ItemStack mushroom = SkullCustoms.getCustomSkull("d3ebf38b4a708eb00745d1fb87a53cb81a7af6cc178d9a2c1116c2cbcff94fea");
        ItemMeta mushroomMeta = mushroom.getItemMeta();
        mushroomMeta.setDisplayName("Inédien");
        mushroom.setItemMeta(mushroomMeta);
        blockCompetences.put("Inédien", mushroom);

        ItemStack enderDragon = SkullCustoms.getCustomSkull("f68c1c079a7ffb36f48dd7150355e3e0b7f68dd605e6f8847313c360cf61e0c");
        ItemMeta enderDragonMeta = enderDragon.getItemMeta();
        enderDragonMeta.setDisplayName("Superman");
        enderDragon.setItemMeta(enderDragonMeta);
        blockCompetences.put("Superman", enderDragon);
    }

    private void initGuiCapa() {
        Inventory inv = Bukkit.createInventory(null, 54, "§lCompétences");
        inv = baseGui(inv);
        inv.setItem(21,blockCompetences.get("Yamakasi"));
        inv.setItem(22,blockCompetences.get("Carquois Amélioré"));
        inv.setItem(23,blockCompetences.get("La Torche"));
        inv.setItem(29,blockCompetences.get("Flash"));
        inv.setItem(30,blockCompetences.get("Balle rebondissante"));
        inv.setItem(31,blockCompetences.get("Inédien"));
        inv.setItem(32,blockCompetences.get("Poison Ivy"));
        inv.setItem(33,blockCompetences.get("Superman"));
        ItemStack btnRetour = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta btnRetourMeta = btnRetour.getItemMeta();
        btnRetourMeta.setDisplayName("Retour");
        btnRetour.setItemMeta(btnRetourMeta);
        inv.setItem(45,btnRetour);
        invCapa = inv;
    }

    private static Inventory baseGui(Inventory inv) {
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

        return inv;
    }

    public static void competGui(Player player, int nbCompetence) {
        ItemStack nbCapa;
        if (nbCompetence == 1) {
            nbCapa = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta nbCapaMeta = nbCapa.getItemMeta();
            nbCapaMeta.setDisplayName(String.valueOf(nbCompetence));
            nbCapa.setItemMeta(nbCapaMeta);
        } else if (nbCompetence == 2) {
            nbCapa = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta nbCapaMeta = nbCapa.getItemMeta();
            nbCapaMeta.setDisplayName(String.valueOf(nbCompetence));
            nbCapa.setItemMeta(nbCapaMeta);
        } else {
            nbCapa = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta nbCapaMeta = nbCapa.getItemMeta();
            nbCapaMeta.setDisplayName(String.valueOf(nbCompetence));
            nbCapa.setItemMeta(nbCapaMeta);
        }
        invCapa.setItem(4, nbCapa);
        player.openInventory(invCapa);
    }



    public static void selecteurCompetences(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Inventory inv = Bukkit.createInventory(null, 54, "§lVos compétences");
            inv = baseGui(inv);
            ResultSet result = bdd.search("SELECT id_capacite,emplacement FROM EQUIPE WHERE UUID='" + player.getUniqueId() + "'");
            ItemStack[] blockCapacite = new ItemStack[3];
            try {
                while (result.next()) {
                    int id_capa = result.getInt("id_capacite");
                    int emplacement = result.getInt("emplacement");
                    ResultSet nameCapacite = bdd.search("SELECT nom FROM CAPACITE WHERE id_capacite=" + id_capa);
                    nameCapacite.next();
                    blockCapacite[emplacement - 1] = blockCompetences.get(nameCapacite.getString("nom"));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            ItemStack structureVoid = new ItemStack(Material.STRUCTURE_VOID);
            ItemMeta structureVoidMeta = structureVoid.getItemMeta();
            structureVoidMeta.setDisplayName("Capacité vide !");
            structureVoidMeta.setLore(new ArrayList<>(Arrays.asList("Cliquez pour ajouter", "une compétence")));
            structureVoid.setItemMeta(structureVoidMeta);

            if (blockCapacite[0] != null) {
                inv.setItem(29,blockCapacite[0]);
            } else {
                inv.setItem(29, structureVoid);
            }
            int nbreCapa = CapaciteManager.hashMapCapacites.get(player.getUniqueId());
            if (nbreCapa > 1) {
                if (blockCapacite[1] != null)  {
                    inv.setItem(22,blockCapacite[0]);
                } else {
                    inv.setItem(22, structureVoid);
                }
            } else {
                ItemStack barrier = new ItemStack(Material.BARRIER);
                ItemMeta barrierMeta = barrier.getItemMeta();
                barrierMeta.setDisplayName("Capacité bloquée !");
                barrierMeta.setLore(new ArrayList<>(Arrays.asList("Cliquez pour la déverrouiller.", "Liste des requis :", "- 16 blocs de lapis",
                        "- 11 blocs d'or",
                        "- 30 niveaux d'xp")));
                barrier.setItemMeta(barrierMeta);
                inv.setItem(22, barrier);
            }
            if (nbreCapa > 2) {
                if (blockCapacite[2] != null) {
                    inv.setItem(33, blockCapacite[2]);
                } else {
                    inv.setItem(33, structureVoid);
                }
            } else {
                ItemStack barrier = new ItemStack(Material.BARRIER);
                ItemMeta barrierMeta = barrier.getItemMeta();
                barrierMeta.setDisplayName("Capacité bloquée !");
                if (nbreCapa != 1) {
                    barrierMeta.setLore(new ArrayList<>(Arrays.asList("Cliquez pour la déverrouiller.",
                            "Liste des requis :",
                            "- 32 blocs de lapis",
                            "- 22 blocs d'or",
                            "- 40 niveaux d'xp",
                            "- 1 totem d'immortalité")));
                }
                barrier.setItemMeta(barrierMeta);
                inv.setItem(33, barrier);
            }
            Inventory finalInv = inv;
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.openInventory(finalInv);
            });
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
