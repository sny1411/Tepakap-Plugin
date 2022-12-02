package fr.sny1411.tepakap;

import fr.sny1411.tepakap.commands.Lock;
import fr.sny1411.tepakap.commands.LockCompleter;
import fr.sny1411.tepakap.listenner.Listenner;
import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    private MysqlDb bdd;
    @Override
    public void onEnable() {
        // Global
        this.bdd = new MysqlDb(this,
                                "minecraft_262975",
                                "123456",
                                 "minecraft1011.omgserv.com",
                                3306,
                             "minecraft_262975");

        Bukkit.getServer().getPluginManager().registerEvents(new Listenner(bdd,this),this);

        // Secure Chest
        Objects.requireNonNull(getCommand("lock")).setExecutor(new Lock(bdd));
        Objects.requireNonNull(getCommand("lock")).setTabCompleter(new LockCompleter());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
