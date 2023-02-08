package fr.sny1411.tepakap;

import fr.sny1411.tepakap.commands.*;
import fr.sny1411.tepakap.commands.secureChest.Lock;
import fr.sny1411.tepakap.commands.secureChest.LockCompleter;
import fr.sny1411.tepakap.commands.secureChest.Unlock;
import fr.sny1411.tepakap.commands.secureChest.UnlockCompleter;
import fr.sny1411.tepakap.listener.Listener;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.CurlExecute;
import fr.sny1411.tepakap.utils.capacite.CapaciteManager;
import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import fr.sny1411.tepakap.utils.maire.GuiMaire;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class Main extends JavaPlugin {
    public FileConfiguration customConfigData;

    @Override
    public void onEnable() {
        // Global
        createConfigDate();
        MysqlDb bdd = new MysqlDb(this,
                "minecraft_390685",
                "123456",
                "minecraft1051.omgserv.com",
                3306,
                "minecraft_390685");

        Bukkit.getServer().getPluginManager().registerEvents(new Listener(bdd,this),this);

        // Secure Chest
        initLockAuto();
        getCommand("lock").setExecutor(new Lock(bdd,this));
        getCommand("lock").setTabCompleter(new LockCompleter());
        getCommand("unlock").setExecutor(new Unlock(bdd,this));
        getCommand("unlock").setTabCompleter(new UnlockCompleter());
        getCommand("maire").setExecutor(new Maire());

        // Largages
        ClockEvents.plugin = this;
        ClockEvents.bdd = bdd;
        ClockEvents.startEvent();

        // ADMIN
        Objects.requireNonNull(getCommand("admin")).setExecutor(new Admin(this,bdd));
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(new AdminCompleter());

        // Capacite
        CapaciteManager.initCapacite(bdd);
        getCommand("competences").setExecutor(new Competences(bdd,this));
        getCommand("fly").setExecutor(new Fly());

        // Notif
        CurlExecute.sendAdminInfo("Server start");

        // Election
        
        GuiMaire.initGuiMaire(bdd,0);
        GuiMaire.initItemStackBonus();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        CurlExecute.sendAdminInfo("Server stop");
    }

    private void initLockAuto() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Lock.lockAuto.put(player.getUniqueId(), false);
        }
    }

    private void createConfigDate() {
        File customConfigDataFile = new File(getDataFolder(), "config.yml");
        if (!customConfigDataFile.exists()) {
            customConfigDataFile.getParentFile().mkdirs();
            saveDefaultConfig();
        }
        customConfigData = new YamlConfiguration();
        try {
            customConfigData.load(customConfigDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
