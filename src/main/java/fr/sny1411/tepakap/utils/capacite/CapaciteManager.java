package fr.sny1411.tepakap.utils.capacite;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static fr.sny1411.tepakap.utils.larguage.ClockEvents.bdd;
import static fr.sny1411.tepakap.utils.larguage.ClockEvents.plugin;

public class CapaciteManager {
    public static HashMap<UUID, Integer> hashMapCapacites = new HashMap<>();
    public static HashMap<UUID, HashMap<Integer, List<String>>> playerCompetences = new HashMap<>(); // list[0] => level; list[1] => name

    public static void initCapacite(MysqlDb bdd) {
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
            chargePlayerCompetences(player.getUniqueId());
        }
    }

    public static void chargePlayerCompetences(UUID playerId) {
        ResultSet result = bdd.search("SELECT emplacement,id_capacite,capacite_level FROM EQUIPE WHERE UUID='" + playerId + "'");
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

                ResultSet nameCapa = bdd.search("SELECT nom FROM CAPACITE WHERE id_capacite=" + id_capacite);
                nameCapa.next();
                Bukkit.getConsoleSender().sendMessage(String.valueOf(nameCapa.isClosed()));
                String nom = nameCapa.getString("nom");
                List<String> listCapa = new ArrayList<>(Arrays.asList(Integer.toString(capacite_level),nom));
                Bukkit.getConsoleSender().sendMessage(listCapa.toString());
                playerHashMap.put(emplacement,listCapa);
                Bukkit.getConsoleSender().sendMessage(playerHashMap.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        playerCompetences.put(playerId, playerHashMap);
        Bukkit.getConsoleSender().sendMessage(playerCompetences.toString());
    }
}
