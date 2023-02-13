package fr.sny1411.tepakap.utils.pioches;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public class PiocheSpawner {
    public static ItemStack get() {
        ItemStack pioche = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta piocheMeta = pioche.getItemMeta();
        piocheMeta.setCustomModelData(1);
        piocheMeta.setDisplayName("§bSoul Breaker");
        piocheMeta.setLore(new ArrayList<>(Arrays.asList("Utilisation restante: 4")));
        pioche.setItemMeta(piocheMeta);
        return pioche;
    }
}
