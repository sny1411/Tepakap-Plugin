package fr.sny1411.tepakap.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlocksUtils {
    public static ItemStack itemBarrier = new ItemStack(Material.BARRIER);
    public static ItemStack braiseMiraculeuse = new ItemStack(Material.BLAZE_POWDER);

    public static void init() {
        ItemMeta metaBarrier = itemBarrier.getItemMeta();
        metaBarrier.setDisplayName("§cAction impossible");
        itemBarrier.setItemMeta(metaBarrier);

        ItemMeta metaBraise = braiseMiraculeuse.getItemMeta();
        metaBraise.setDisplayName("§6Braise miraculeuse");
        metaBraise.setCustomModelData(1);
        braiseMiraculeuse.setItemMeta(metaBraise);

    }
}
