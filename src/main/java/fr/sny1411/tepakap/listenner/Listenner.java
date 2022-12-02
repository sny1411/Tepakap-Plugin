package fr.sny1411.tepakap.listenner;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Listenner implements Listener {
    private MysqlDb bdd;
    private Main main;

    public Listenner(MysqlDb bdd, Main main) {
        this.bdd = bdd;
        this.main = main;
    }

    @EventHandler
    public void onPLayerJoin(PlayerJoinEvent e) {

        Bukkit.getScheduler().runTaskAsynchronously(main, new Runnable() {
            @Override
            public void run() {
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
                if (nbreBddPlayer == 0) {
                    player.sendMessage("bienvenue"); // message à changer (new player)
                    bdd.putNewItems("INSERT INTO JOUEUR VALUES ('"+ player.getUniqueId() + "','" + player.getName() + "','" + datetime+ "','" +datetime + "')");
                } else if (nbreBddPlayer > 0) {
                    player.sendMessage("salut"); // message à changer (player pas nouveau co)
                    bdd.modifyItems("UPDATE JOUEUR SET derniere_co='" + datetime + "' WHERE UUID='" + player.getUniqueId() + "'");
                }
            }
        });
    }
}
