package fr.sny1411.tepakap.sql;

import org.bukkit.Bukkit;

import java.sql.*;

/***
 * @author sny1411
 * @since 30-11-2022
 */
public class MysqlDb {

    private final String USER;
    private final String PASS;
    private final String URL;
    private final int PORT;
    private final String NAMEDB;
    private Statement statement;
    private boolean connected;

    /***
     * @param USER nom d'utilisateur de la bdd
     * @param PASS mot de passe de la bdd
     * @param URL url de la bdd (ex : mabase.sny1411.com)
     * @param PORT port de la bdd
     * @param NAMEDB nom de la bdd
     */
    MysqlDb(String USER,String PASS,String URL,int PORT,String NAMEDB) {
        this.USER = USER;
        this.PASS = PASS;
        this.URL = URL;
        this.PORT = PORT;
        this.NAMEDB = NAMEDB;

        connect();
    }

    private void connect() {
        try {
            long startTime = System.currentTimeMillis();
            Connection connection = DriverManager.getConnection("jbdc:mysql://" + URL + ":" + PORT + "/" + NAMEDB, USER, PASS);
            statement = connection.createStatement();
            Bukkit.getServer().getConsoleSender().sendMessage("§a[INFO BDD] §2BDD CONNECTE EN " +
                    (System.currentTimeMillis() - startTime) + " MS");
            connected = true;
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage("§4[ERREUR BDD] §cERREUR CONNECTION BDD ! (reload nécessaire)");
        }
    }

    private ResultSet search(String requete) {
        try {
            statement.executeQuery(requete);
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage("§4[ERREUR BDD] §cLA BDD N'EST PAS CONNECTE !");
            connected = false;
            while (!connected) {
                Bukkit.getServer().getConsoleSender().sendMessage("§4[INFO BDD] §c TENTATIVE DE RECONECTION");
                connect();
                if (connected) {
                    Bukkit.getServer().getConsoleSender().sendMessage("§a[INFO BDD] §2TENTATIVE DE RECONNECTION EFFECTUE AVEC SUCCES");
                } else {
                    Bukkit.getServer().getConsoleSender().sendMessage("§4[INFO BDD] §cTENTATIVE DE RECONNECTION ECHOUE");
                }
                try {
                    wait(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

    }

}
