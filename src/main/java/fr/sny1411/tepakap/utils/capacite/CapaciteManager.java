package fr.sny1411.tepakap.utils.capacite;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static fr.sny1411.tepakap.utils.larguage.ClockEvents.plugin;

public class CapaciteManager {
    public static HashMap<UUID, Integer> hashMapCapacites = new HashMap<>();

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

    }
}
