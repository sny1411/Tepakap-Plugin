package fr.sny1411.tepakap.sql;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.concurrent.TimeUnit;

/***
 * @author sny1411
 * @since 30-11-2022
 */
public class MysqlDb {
    private final Plugin plugin;
    private final String USER;
    private final String PASS;
    private final String URL;
    private final int PORT;
    private final String NAMEDB;
    private Statement statement;
    private boolean connected;

    /***
     * @param plugin instance du plugin
     * @param USER nom d'utilisateur de la bdd
     * @param PASS mot de passe de la bdd
     * @param URL url de la bdd (ex : mabase.sny1411.com)
     * @param PORT port de la bdd
     * @param NAMEDB nom de la bdd
     */
    public MysqlDb(Plugin plugin, String USER, String PASS, String URL, int PORT, String NAMEDB) {
        this.USER = USER;
        this.PASS = PASS;
        this.URL = URL;
        this.PORT = PORT;
        this.NAMEDB = NAMEDB;
        this.plugin = plugin;

        connect();

    }

    private void connect() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    Connection connection = DriverManager.getConnection("jdbc:mysql://" + URL +":"+PORT+"/"+NAMEDB + "?autoReconnect=true", USER, PASS);
                    statement = connection.createStatement();
                    Bukkit.getServer().getConsoleSender().sendMessage("§a[INFO BDD] §2BDD CONNECTE EN " +
                            (System.currentTimeMillis() - startTime) + " MS");
                    connected = true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    Bukkit.getServer().getConsoleSender().sendMessage("§4[ERREUR BDD] §cERREUR CONNECTION BDD ! (reload nécessaire)");
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
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    /***
     * @param request sql
     * @return ResultSet ou null si la requête échoue
     */
    public ResultSet search(String request) {
        try {
            if (connected) {
                return statement.executeQuery(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage("§4[ERREUR BDD] §cLA BDD N'EST PAS CONNECTE !");
            connected = false;
            connect();
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            return search(request);
        }
        return null;
    }

    /***
     * @param request sql
     */
    public void putNewItems(String request) {
        try {
            if (connected) {
                statement.executeUpdate(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage("§4[ERREUR BDD] §cLA BDD N'EST PAS CONNECTE !");
            connected = false;
            connect();
            putNewItems(request);
        }
    }

    /***
     * @param request sql
     */
    public void modifyItems(String request) {
        try {
            if (connected) {
                statement.executeUpdate(request);
            }
        } catch (SQLException e) {
            Bukkit.getServer().getConsoleSender().sendMessage("§4[ERREUR BDD] §cLA BDD N'EST PAS CONNECTE !");
            connected = false;
            connect();
            modifyItems(request);
        }
    }
}
