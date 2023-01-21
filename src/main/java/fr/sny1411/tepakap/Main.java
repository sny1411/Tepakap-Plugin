package fr.sny1411.tepakap;

import fr.sny1411.tepakap.commands.Lock;
import fr.sny1411.tepakap.commands.LockCompleter;
import fr.sny1411.tepakap.commands.Unlock;
import fr.sny1411.tepakap.commands.UnlockCompleter;
import fr.sny1411.tepakap.listenner.Listenner;
import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        // Global
        MysqlDb bdd = new MysqlDb(this,
                "minecraft_390685",
                "123456",
                "minecraft1051.omgserv.com",
                3306,
                "minecraft_390685");

        Bukkit.getServer().getPluginManager().registerEvents(new Listenner(bdd,this),this);

        // Secure Chest
        Objects.requireNonNull(getCommand("lock")).setExecutor(new Lock(bdd,this));
        Objects.requireNonNull(getCommand("lock")).setTabCompleter(new LockCompleter());
        Objects.requireNonNull(getCommand("unlock")).setExecutor(new Unlock(bdd,this));
        Objects.requireNonNull(getCommand("unlock")).setTabCompleter(new UnlockCompleter());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
