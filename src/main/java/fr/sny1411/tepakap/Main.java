package fr.sny1411.tepakap;

import fr.sny1411.tepakap.commands.Admin;
import fr.sny1411.tepakap.commands.AdminCompleter;
import fr.sny1411.tepakap.commands.secureChest.Lock;
import fr.sny1411.tepakap.commands.secureChest.LockCompleter;
import fr.sny1411.tepakap.commands.secureChest.Unlock;
import fr.sny1411.tepakap.commands.secureChest.UnlockCompleter;
import fr.sny1411.tepakap.listenner.Listenner;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        initLockAuto();
        Objects.requireNonNull(getCommand("lock")).setExecutor(new Lock(bdd,this));
        Objects.requireNonNull(getCommand("lock")).setTabCompleter(new LockCompleter());
        Objects.requireNonNull(getCommand("unlock")).setExecutor(new Unlock(bdd,this));
        Objects.requireNonNull(getCommand("unlock")).setTabCompleter(new UnlockCompleter());

        // Larguage
        ClockEvents.plugin = this;
        ClockEvents.startEvent();

        // ADMIN
        Objects.requireNonNull(getCommand("admin")).setExecutor(new Admin(this,bdd));
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(new AdminCompleter());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initLockAuto() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Lock.lockAuto.put(player.getUniqueId(), false);
        }
    }

}
