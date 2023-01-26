package fr.sny1411.tepakap.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RessourcePack {
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

        player.openInventory(inv);
    }
}
