package fr.sny1411.tepakap.utils.secureChest;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;

public class Lockable {
    public static ArrayList<Material> listBlocks = new ArrayList<>(Arrays.asList(Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.SHULKER_BOX));
    public static ArrayList<Material> getList() {
        return listBlocks;
    }

    public static boolean inList(Material material) {
        for (Material element : getList()) {
            if (element.equals(material)) {
                return true;
            }
        }
        return false;
    }
}
