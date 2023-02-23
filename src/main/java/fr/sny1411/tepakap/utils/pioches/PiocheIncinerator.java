package fr.sny1411.tepakap.utils.pioches;

import fr.sny1411.tepakap.utils.Random;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PiocheIncinerator {
    public static List<Material> listBlock = new ArrayList<>(Arrays.asList(Material.IRON_ORE,Material.DEEPSLATE_IRON_ORE,Material.GOLD_ORE,Material.DEEPSLATE_GOLD_ORE,Material.COPPER_ORE,Material.DEEPSLATE_COPPER_ORE,Material.STONE,Material.COBBLESTONE,Material.DEEPSLATE,Material.COBBLED_DEEPSLATE,Material.ANCIENT_DEBRIS));
    public static ItemStack get() {
        ItemStack pioche = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta piocheMeta = pioche.getItemMeta();
        piocheMeta.setCustomModelData(1);
        piocheMeta.setDisplayName("§bIncinerator");
        pioche.setItemMeta(piocheMeta);
        return pioche;
    }

    public static void blockBreak(Block block, Integer levelEnchant) {
        switch (block.getType()) {
            case IRON_ORE,DEEPSLATE_IRON_ORE:
                dropCuit(block,levelEnchant,Material.IRON_INGOT,1);
                break;
            case GOLD_ORE,DEEPSLATE_GOLD_ORE:
                dropCuit(block,levelEnchant,Material.GOLD_INGOT,1);
                break;
            case COPPER_ORE,DEEPSLATE_COPPER_ORE:
                dropCuit(block,levelEnchant,Material.COPPER_INGOT, Random.random(2,5));
                break;
            case STONE,COBBLESTONE:
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.STONE));
                break;
            case DEEPSLATE, COBBLED_DEEPSLATE:
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DEEPSLATE));
                break;
            case ANCIENT_DEBRIS:
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.NETHERITE_SCRAP));
                break;
            default:
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
        }
    }

    private static void dropCuit(Block block, Integer levelEnchant,Material loot, int nbre) {
        if (levelEnchant == null) {
            levelEnchant = 0;
        }
        switch (levelEnchant) {
            case 1:
                int rand = Random.random(1,100);
                if (rand <= 33) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(loot,nbre * 2));
                    return;
                }
                break;
            case 2:
                rand = Random.random(1,100);
                if (rand <= 25) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(loot,nbre * 2));
                    return;
                } else if (rand <= 50) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(loot,nbre * 3));
                    return;
                }
                break;
            case 3:
                rand = Random.random(1,100);
                if (rand <= 20) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(loot,nbre * 2));
                    return;
                } else if (rand <= 40) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(loot,nbre * 3));
                    return;
                } else if (rand <= 60) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(loot,nbre * 4));
                    return;
                }
                break;
        }
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(loot, nbre));
    }
}
