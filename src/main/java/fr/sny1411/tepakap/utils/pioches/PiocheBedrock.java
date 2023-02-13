package fr.sny1411.tepakap.utils.pioches;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class PiocheBedrock {
    public static ItemStack get() {
        ItemStack pioche = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta piocheMeta = pioche.getItemMeta();
        piocheMeta.setCustomModelData(2);
        piocheMeta.setDisplayName("§bBriseuse des Profondeurs");
        piocheMeta.setLore(new ArrayList<>(Arrays.asList("Utilisation restante: 10")));
        pioche.setItemMeta(piocheMeta);
        return pioche;
    }
}
