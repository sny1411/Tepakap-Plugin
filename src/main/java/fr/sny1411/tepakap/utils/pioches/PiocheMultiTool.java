package fr.sny1411.tepakap.utils.pioches;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class PiocheMultiTool {
    public static ItemStack get() {
        ItemStack pioche = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta piocheMeta = pioche.getItemMeta();
        piocheMeta.setCustomModelData(3);
        piocheMeta.setDisplayName("§bMultiTool");
        piocheMeta.setLore(new ArrayList<>(Arrays.asList("Utilisation restante: 4000")));
        piocheMeta.setUnbreakable(true);
        pioche.setItemMeta(piocheMeta);
        return pioche;
    }
}
