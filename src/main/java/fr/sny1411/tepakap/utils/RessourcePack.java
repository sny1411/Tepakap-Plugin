package fr.sny1411.tepakap.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.JarURLConnection;
import java.util.HashMap;
import java.util.List;

public class RessourcePack {
    public static HashMap<Integer, List<String>> listItems = new HashMap<>();
    public static void showCustomsRessources(int nPage, Player player) {
        // 1 jusque 193
        Inventory inv = Bukkit.createInventory(null, 54, "Admin customs texture");
        player.sendMessage(String.valueOf((1 * (nPage-1)*45) + 1));
        player.sendMessage(String.valueOf(nPage*45 + 1));
        for (int i = (1 * (nPage-1)*45); i < nPage*45 && i <= 276; i++) {
            ItemStack customItem = new ItemStack(Material.PAPER);
            ItemMeta metaCustoms = customItem.getItemMeta();
            metaCustoms.setCustomModelData(i+1);
            customItem.setItemMeta(metaCustoms);
            inv.setItem(i%45,customItem);
        }
        ItemStack customItem = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta metaCustoms = customItem.getItemMeta();
        metaCustoms.setCustomModelData(1);
        customItem.setItemMeta(metaCustoms);
        inv.addItem(customItem);

        metaCustoms.setCustomModelData(2);
        customItem.setItemMeta(metaCustoms);
        inv.addItem(customItem);

        metaCustoms.setCustomModelData(2);

        customItem = new ItemStack(Material.WOODEN_PICKAXE);
        metaCustoms = customItem.getItemMeta();
        metaCustoms.setCustomModelData(1);
        customItem.setItemMeta(metaCustoms);
        inv.addItem(customItem);

        player.openInventory(inv);
    }
}
