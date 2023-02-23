package fr.sny1411.tepakap;

import fr.sny1411.tepakap.commands.*;
import fr.sny1411.tepakap.commands.secureChest.Lock;
import fr.sny1411.tepakap.commands.secureChest.LockCompleter;
import fr.sny1411.tepakap.commands.secureChest.Unlock;
import fr.sny1411.tepakap.commands.secureChest.UnlockCompleter;
import fr.sny1411.tepakap.listener.Listener;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.capacite.CapaciteManager;
import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import fr.sny1411.tepakap.utils.maire.GuiMaire;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
                "minecraft_390685", // minecraft_395992
                "123456",
                "minecraft1051.omgserv.com", // minecraft3145.omgserv.com
                3306,
                "minecraft_390685"); // minecraft_395992

        Bukkit.getServer().getPluginManager().registerEvents(new Listener(bdd,this),this);

        // Secure Chest
        initLockAuto();
        Objects.requireNonNull(getCommand("lock")).setExecutor(new Lock(bdd,this));
        Objects.requireNonNull(getCommand("lock")).setTabCompleter(new LockCompleter());
        Objects.requireNonNull(getCommand("unlock")).setExecutor(new Unlock(bdd,this));
        Objects.requireNonNull(getCommand("unlock")).setTabCompleter(new UnlockCompleter());

        // Largages
        ClockEvents.plugin = this;
        ClockEvents.bdd = bdd;
        //ClockEvents.startEvent();

        // ADMIN
        Objects.requireNonNull(getCommand("admin")).setExecutor(new Admin(this,bdd));
        Objects.requireNonNull(getCommand("admin")).setTabCompleter(new AdminCompleter());

        // Capacite
        CapaciteManager.initCapacite(bdd);
        Objects.requireNonNull(getCommand("competences")).setExecutor(new Competences(bdd,this));
        Objects.requireNonNull(getCommand("fly")).setExecutor(new Fly());

        // Notif

        // Election
        GuiMaire.initGuiMaire(bdd,customConfigData.getInt("maire.nbElection"));
        GuiMaire.initItemStackBonus();
        GuiMaire.initIdBonus();
        GuiMaire.initGuiChoix();

        if (!Objects.equals(ClockEvents.plugin.getConfig().get("maire.xPres"), "None")) {
            GuiMaire.presentoir = new Location(Bukkit.getWorld("world"),
                    customConfigData.getInt("maire.xPres"),
                    customConfigData.getInt("maire.yPres"),
                    customConfigData.getInt("maire.zPres"));
        }

        // Armure
        Objects.requireNonNull(getCommand("shop")).setExecutor(new Shop());
        Objects.requireNonNull(getCommand("shop")).setTabCompleter(new ShopCompleter());
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
