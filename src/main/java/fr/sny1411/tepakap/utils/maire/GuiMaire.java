package fr.sny1411.tepakap.utils.maire;

import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.SkullCustoms;
import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class GuiMaire {
    public static Inventory invBaseSePresenter;
    public static Inventory invChoix;
    private static MysqlDb bdd;
    public static int nbElection;
    public static boolean presentationEnCour;
    public static boolean voteEnCour;
    public static HashMap<UUID, HashMap<Integer,String>> tempPresentation = new HashMap<>();
    public static HashMap<String,ItemStack> itemBonus = new HashMap<>();
    public static HashMap<String,Integer> hashIdBonus = new HashMap<>();
    public static Location presentoir; // à init

    public static void initIdBonus() {
        hashIdBonus.put("Pilleur de trésors",1);
        hashIdBonus.put("Energique",2);
        hashIdBonus.put("Guerrier",3);
        hashIdBonus.put("Chasseur de démons",4);
        hashIdBonus.put("Agriculteur",5);
        hashIdBonus.put("Bûcheron",6);
        hashIdBonus.put("Mineur",7);
        hashIdBonus.put("Négociateur",8);
        hashIdBonus.put("Horloger",9);
        hashIdBonus.put("Généreux",10);
        hashIdBonus.put("Arrache-Coeur",11);
    }

    public static void initItemStackBonus() {
        ItemStack pillerTresor = new ItemStack(Material.PAPER);
        ItemMeta metaPillier = pillerTresor.getItemMeta();
        metaPillier.setDisplayName("Pilleur de trésors");
        metaPillier.setLore(new ArrayList<>(Arrays.asList("Plus de chance d'avoir des", "objets rares dans les largages", "Requis : Avoir ouvert 8 largages")));
        metaPillier.setCustomModelData(1);
        pillerTresor.setItemMeta(metaPillier);
        itemBonus.put("Pilleur de trésors", pillerTresor);

        ItemStack haste = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta hasteMeta = haste.getItemMeta();
        hasteMeta.setDisplayName("Energique");
        hasteMeta.setLore(new ArrayList<>(Arrays.asList("Haste 1", "Requis : aucun")));
        haste.setItemMeta(hasteMeta);
        itemBonus.put("Energique", haste);

        ItemStack guerrier = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta metaGuerrier = guerrier.getItemMeta();
        metaGuerrier.setDisplayName("Guerrier");
        metaGuerrier.setLore(new ArrayList<>(Arrays.asList("Dégats augmentés","Requis : aucun")));
        guerrier.setItemMeta(metaGuerrier);
        itemBonus.put("Guerrier", guerrier);

        ItemStack chasseurDemons = new ItemStack(Material.WITHER_SKELETON_SKULL);
        ItemMeta metaChasseurDemons = chasseurDemons.getItemMeta();
        metaChasseurDemons.setDisplayName("Chasseur de démons");
        metaChasseurDemons.setLore(new ArrayList<>(Arrays.asList("Drop de wither skull augmenté", "Requis: Tuer 150 wither squelettes")));
        chasseurDemons.setItemMeta(metaChasseurDemons);
        itemBonus.put("Chasseur de démons", chasseurDemons);

        ItemStack agriculture = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta metaAgriculture = agriculture.getItemMeta();
        metaAgriculture.setDisplayName("Agriculteur");
        metaAgriculture.setLore(new ArrayList<>(Arrays.asList("Bonus de récolte", "Requis: aucun")));
        agriculture.setItemMeta(metaAgriculture);
        itemBonus.put("Agriculteur",agriculture);

        ItemStack bucheron = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDNjNzQxNDlkYmM0MTM0ZDhiNWUzYmJlMjk5N2JhMzZhOGE4MDJlMWFmOTI5NThhNDkzYjczNmYxZjQ2OGM4In19fQ");
        ItemMeta metaBucheron = bucheron.getItemMeta();
        metaBucheron.setDisplayName("Bûcheron");
        metaBucheron.setLore(new ArrayList<>(Arrays.asList("Casse jusqu'à 16 bûches en un","coup, si elles sont collées","Requis: Casser une hache en diamant")));
        bucheron.setItemMeta(metaBucheron);
        itemBonus.put("Bûcheron", bucheron);

        ItemStack mineur = new ItemStack(Material.IRON_ORE);
        ItemMeta metaMineur = mineur.getItemMeta();
        metaMineur.setDisplayName("Mineur");
        metaMineur.setLore(new ArrayList<>(Arrays.asList("Casse jusqu'à 16 blocks d'un ","filon de minerai en un coup, si ils","sont collés","Requis: Casser une pioche en diamant")));
        mineur.setItemMeta(metaMineur);
        itemBonus.put("Mineur", mineur);

        ItemStack negociateur = new ItemStack(Material.EMERALD);
        ItemMeta metaNegociateur = negociateur.getItemMeta();
        metaNegociateur.setDisplayName("Négociateur");
        metaNegociateur.setLore(new ArrayList<>(Arrays.asList("Les coûts des échanges avec","les villageois sont réduits","Requis: Posséder 10 blocs d'émeraude","(vous seront retirés)")));
        negociateur.setItemMeta(metaNegociateur);
        itemBonus.put("Négociateur", negociateur);

        ItemStack horloger = new ItemStack(Material.CLOCK);
        ItemMeta metaHorloger = horloger.getItemMeta();
        metaHorloger.setDisplayName("Horloger");
        metaHorloger.setLore(new ArrayList<>(Arrays.asList("Temps x1.5 pour tuer les mobs", "des largages", "Requis: aucun")));
        horloger.setItemMeta(metaHorloger);
        itemBonus.put("Horloger", horloger);

        ItemStack genereux = new ItemStack(Material.DIAMOND);
        ItemMeta metaGenereux = genereux.getItemMeta();
        metaGenereux.setDisplayName("Généreux");
        metaGenereux.setLore(new ArrayList<>(Arrays.asList("Permet de récupérer une ","récompense aléatoire tous les ","jours en se connectant","Requis : Posséder au moins une", "des différentes décorations")));
        genereux.setItemMeta(metaGenereux);
        itemBonus.put("Généreux", genereux);

        ItemStack arracheCoeur = new ItemStack(Material.BOOK); // FAUT FINIR
        ItemMeta metaArracheCoeur = arracheCoeur.getItemMeta();
        metaArracheCoeur.setCustomModelData(2); // ICI -- rep metsuu
        metaArracheCoeur.setDisplayName("Arrache-Coeur");
        metaArracheCoeur.setLore(new ArrayList<>(Arrays.asList("Le drop du Coeur de Warden","passe de 50% à 100%","Requis: Tuer 1 Warden")));
        arracheCoeur.setItemMeta(metaArracheCoeur);
        itemBonus.put("Arrache-Coeur", arracheCoeur);
    }

    public static void initGuiMaire(MysqlDb bdd,int nbElection) {
        // bdd init
        GuiMaire.bdd = bdd;
        //electionInit
        GuiMaire.nbElection = nbElection;
        // init gui se presenter
        invBaseSePresenter = Bukkit.createInventory(null, InventoryType.CHEST, "§8§lSe présenter maire");
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(" ");
        item.setItemMeta(itemMeta);
        for (int i = 0; i < invBaseSePresenter.getSize(); i++) {
            invBaseSePresenter.setItem(i, item);
        }
    }
    public static void initGuiChoix() {
        invChoix = Bukkit.createInventory(null, 54, "§8§lChoix Bonus");
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(" ");
        item.setItemMeta(itemMeta);
        int iItem = 0;
        for (int i = 0; i < 54; i++) {
            List<String> itemsKey = new ArrayList<>(itemBonus.keySet());
            if ((i >= 20 && i <= 24) || (i >= 29 && i <= 33) || i == 40) {
                invChoix.setItem(i,itemBonus.get(itemsKey.get(iItem)));
                iItem++;
            } else {
                invChoix.setItem(i,item);
            }
        }

    }

    public static void openGuiChoisir(Player player, int nbBonus) {
        switch (nbBonus) {
            case 1:
                ItemStack item = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdhNzk1ODVlMTEyN2RkMzU3ZmMyMDI4OTNmOWRlMjQxYmM5ZTUzMCJ9fX0=");
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName("1");
                item.setItemMeta(itemMeta);
                invChoix.setItem(4,item);
                break;
            case 2:
                item = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNkOWVlZWU4ODM0Njg4ODFkODM4NDhhNDZiZjMwMTI0ODVjMjNmNzU3NTNiOGZiZTg0ODczNDE0MTk4NDcifX19");
                itemMeta = item.getItemMeta();
                itemMeta.setDisplayName("2");
                item.setItemMeta(itemMeta);
                invChoix.setItem(4,item);
                break;
            case 3:
                item = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0ZWFlMTM5MzM4NjBhNmRmNWU4ZTk1NTY5M2I5NWE4YzNiMTVjMzZiOGI1ODc1MzJhYzA5OTZiYzM3ZTUifX19");
                itemMeta = item.getItemMeta();
                itemMeta.setDisplayName("3");
                item.setItemMeta(itemMeta);
                invChoix.setItem(4,item);
                break;
        }

        player.openInventory(invChoix);
    }

    public static void openGuiSePresenter(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin,() -> {
            try {
                ResultSet result = bdd.search("SELECT id_bonus_1,id_bonus_2,id_bonus_3 FROM PRESENTATION_MAIRE WHERE UUID='" + player.getUniqueId() + "' AND nbElection=" + nbElection);
                if (!result.next()) {
                    ItemStack balise = new ItemStack(Material.BEACON);
                    ItemMeta baliseMeta = balise.getItemMeta();

                    baliseMeta.setDisplayName("Choisir");
                    balise.setItemMeta(baliseMeta);
                    if (!tempPresentation.containsKey(player.getUniqueId())) {
                        tempPresentation.put(player.getUniqueId(), new HashMap<>());
                        invBaseSePresenter.setItem(11,balise);
                        invBaseSePresenter.setItem(13,balise);
                        invBaseSePresenter.setItem(15,balise);
                    } else {
                        HashMap<Integer,String> itemSelect = tempPresentation.get(player.getUniqueId());
                        if (itemSelect.containsKey(1)) {
                            // mettre item bonus
                            invBaseSePresenter.setItem(11,itemBonus.get(itemSelect.get(1)));
                        } else {
                            invBaseSePresenter.setItem(11,balise);
                        }
                        if (itemSelect.containsKey(2)) {
                            // mettre item bonus
                            invBaseSePresenter.setItem(13,itemBonus.get(itemSelect.get(2)));
                        } else {
                            invBaseSePresenter.setItem(13,balise);
                        }
                        if (itemSelect.containsKey(3)) {
                            // mettre item bonus
                            invBaseSePresenter.setItem(15,itemBonus.get(itemSelect.get(3)));
                        } else {
                            invBaseSePresenter.setItem(15,balise);
                        }
                    }

                    ItemStack valid = SkullCustoms.getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I3ODg5M2U1M2ZlN2U4NDQ3YmMwNzY2ODIyZjg1ZmUzNmZmYTkxNWFiZGJmNmNjOTc4MjY2YTA3ZDNlYWMifX19");
                    ItemMeta metaValid = valid.getItemMeta();
                    metaValid.setDisplayName("Valider");
                    valid.setItemMeta(metaValid);

                    invBaseSePresenter.setItem(22, valid);

                    Bukkit.getScheduler().runTask(ClockEvents.plugin, () -> {
                        player.openInventory(invBaseSePresenter);
                    });
                } else {
                    Bukkit.getConsoleSender().sendMessage("test1");
                    List<Integer> listIntBonus = new ArrayList<>(Arrays.asList(result.getInt("id_bonus_1"),result.getInt("id_bonus_2"),result.getInt("id_bonus_3")));
                    Bukkit.getConsoleSender().sendMessage("test2");
                    List<String> listBonus = new ArrayList<>();
                    for (String bonus : hashIdBonus.keySet()) {
                        if (listIntBonus.contains(hashIdBonus.get(bonus))) {
                            listBonus.add(bonus);
                        }
                    }
                    player.sendMessage("§aVotre présentation est enregistré avec les bonus suivant:");
                    for (String bonus : listBonus) {
                        player.sendMessage("§a- " + bonus);
                    }

                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
