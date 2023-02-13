package fr.sny1411.tepakap.utils.capacite;

import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static fr.sny1411.tepakap.utils.larguage.ClockEvents.bdd;
import static fr.sny1411.tepakap.utils.larguage.ClockEvents.plugin;

public class CapaciteManager {
    public static HashMap<UUID, Integer> hashMapCapacites = new HashMap<>();
    public static HashMap<UUID, HashMap<Integer, List<String>>> playerCompetences = new HashMap<>(); // list[0] => level; list[1] => name

    public static HashMap<Integer,String> dicCapa = new HashMap<>();

    private static void initDicCapa() {
        dicCapa.put(1,"Yamakasi");
        dicCapa.put(2,"Carquois Amélioré");
        dicCapa.put(3,"La Torche");
        dicCapa.put(4,"Flash");
        dicCapa.put(5,"Balle rebondissante");
        dicCapa.put(6,"Inédien");
        dicCapa.put(7,"Poison Ivy");
        dicCapa.put(8,"Superman");
    }

    public static void initCapacite(MysqlDb bdd) {
        initDicCapa();
        ResultSet result = bdd.search("SELECT UUID,nbre_capacite FROM JOUEUR");
        if (result == null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                initCapacite(bdd);
            });
            return;
        }
        try {
            while (result.next()) {
                UUID playerId = UUID.fromString(result.getString("UUID"));
                int nbreCapa = result.getInt("nbre_capacite");
                hashMapCapacites.put(playerId,nbreCapa);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerCompetences.put(player.getUniqueId(), new HashMap<>());
            try {
                chargePlayerCompetences(player.getUniqueId());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Integer getLevelCapacite(String nameComp, UUID playerID) {
        HashMap<Integer,List<String>> playerComp =  playerCompetences.get(playerID);
        for (int key : playerComp.keySet()) {
            List<String> listComp = playerComp.get(key);
            if (Objects.equals(listComp.get(1), nameComp)) {
                return Integer.valueOf(listComp.get(0));
            }
        }
        return 0;
    }

    public static boolean isInCapacite(String nameComp,UUID playerID) {
        HashMap<Integer,List<String>> playerComp =  playerCompetences.get(playerID);
        for (int key : playerComp.keySet()) {
            if (Objects.equals(playerComp.get(key).get(1), nameComp)) {
                return true;
            }
        }
        return false;
    }

    public static void chargePlayerCompetences(UUID playerId) throws InterruptedException {
        ResultSet result = null;
        try {
            result = bdd.search("SELECT emplacement,id_capacite,capacite_level FROM EQUIPE WHERE UUID='" + playerId + "'");
        } catch (Exception e) {
            e.printStackTrace();
            TimeUnit.SECONDS.sleep(2);
            chargePlayerCompetences(playerId);
        }
        HashMap<Integer, List<String>> playerHashMap = new HashMap<>();
        if (result == null) {
            playerCompetences.put(playerId, playerHashMap);
            return;
        }
        try {
            while (result.next()) {
                Bukkit.getConsoleSender().sendMessage(String.valueOf(result.isClosed()));
                int emplacement = result.getInt("emplacement");
                int id_capacite = result.getInt("id_capacite");
                int capacite_level = result.getInt("capacite_level");

                List<String> listCapa = new ArrayList<>(Arrays.asList(Integer.toString(capacite_level), dicCapa.get(id_capacite)));
                Bukkit.getConsoleSender().sendMessage(listCapa.toString());
                playerHashMap.put(emplacement, listCapa);
                Bukkit.getConsoleSender().sendMessage("ici => " + playerHashMap);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage("<< error >>");
        }
        playerCompetences.put(playerId, playerHashMap);
        Bukkit.getConsoleSender().sendMessage(playerCompetences.toString());

        Player player = Bukkit.getServer().getPlayer(playerId);
        assert player != null;
        if (isInCapacite("Flash", playerId)) {
            int levelComp = CapaciteManager.getLevelCapacite("Flash", playerId);
            switch (levelComp) {
                case 1:
                    player.setWalkSpeed(0.22F);
                    break;
                case 2:
                    player.setWalkSpeed(0.24F);
                    break;
                case 3:
                    player.setWalkSpeed(0.26F);
                    break;
            }
        } else {
            player.setWalkSpeed(0.2F);
        }

        if (isInCapacite("Balle rebondissante", playerId)) {
            int levelComp = CapaciteManager.getLevelCapacite("Balle rebondissante", playerId);
            switch (levelComp) {
                case 1:
                    Bukkit.getScheduler().runTask(plugin, () -> player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, 1, true, false, false)));
                    break;
                case 2:
                    Bukkit.getScheduler().runTask(plugin, () -> player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999, 2, true, false, false)));
                    break;
            }
        } else {
            player.removePotionEffect(PotionEffectType.JUMP);
        }
    }
}
