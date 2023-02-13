package fr.sny1411.tepakap.utils.pioches;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PiocheIncinerator {
    public static ItemStack get() {
        ItemStack pioche = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta piocheMeta = pioche.getItemMeta();
        piocheMeta.setCustomModelData(1);
        piocheMeta.setDisplayName("§bIncinerator");
        pioche.setItemMeta(piocheMeta);
        return pioche;
    }
}
