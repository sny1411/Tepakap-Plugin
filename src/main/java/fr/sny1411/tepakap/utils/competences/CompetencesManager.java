package fr.sny1411.tepakap.utils.competences;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class CompetencesManager {
    public static HashMap<UUID, HashMap<Integer,String>> playerCompetences = new HashMap<>();

    public CompetencesManager() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerCompetences.put(player.getUniqueId(), new HashMap<>());
        }
    }

    private void chargePlayerCompetences(UUID PlayerId) {

    }

}
