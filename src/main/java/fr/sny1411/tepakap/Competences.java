package fr.sny1411.tepakap;

import fr.sny1411.tepakap.sql.MysqlDb;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Competences implements CommandExecutor {
    private MysqlDb bdd;
    private Main plugin;

    public Competences(MysqlDb bdd, Main plugin) {
        this.bdd = bdd;
        this.plugin = plugin;
    }

    private Inventory baseGui(Inventory inv) {
        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta metaGlassPane = glassPane.getItemMeta();
        metaGlassPane.setDisplayName(" ");
        glassPane.setItemMeta(metaGlassPane);

        for (int i = 0; i < 54; i++) {
            if (i <= 9 || i >= 46) {
                inv.setItem(i, glassPane);
            } else if (i%9==0 || i%9 == 8) {
                inv.setItem(i, glassPane);
            }
        }

        return inv;
    }

    private void competGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Compétences");
        inv = baseGui(inv);

        inv.setItem(21,new ItemStack(Material.PLAYER_HEAD));
        inv.setItem(22,new ItemStack(Material.PLAYER_HEAD));
        inv.setItem(23,new ItemStack(Material.PLAYER_HEAD));

        inv.setItem(29,new ItemStack(Material.PLAYER_HEAD));
        inv.setItem(30,new ItemStack(Material.PLAYER_HEAD));
        inv.setItem(31,new ItemStack(Material.PLAYER_HEAD));
        inv.setItem(32,new ItemStack(Material.PLAYER_HEAD));
        inv.setItem(33,new ItemStack(Material.PLAYER_HEAD));

        player.openInventory(inv);
    }

    private void selecteurCompetences() {
        Inventory inv = Bukkit.createInventory(null, 54, "Vos compétences");
        inv = baseGui(inv);


    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            competGui((Player) commandSender);
        }
        return false;
    }
}
