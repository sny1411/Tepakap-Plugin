package fr.sny1411.tepakap;

import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    private MysqlDb bdd;
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
