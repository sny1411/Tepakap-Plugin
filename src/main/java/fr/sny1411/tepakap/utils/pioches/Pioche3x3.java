package fr.sny1411.tepakap.utils.pioches;

import fr.sny1411.tepakap.utils.secureChest.Lockable;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Pioche3x3 {
    private static void breakBlock(Block block){
        Material blockType = block.getType();
        if (!blockType.equals(Material.BEDROCK) && !Lockable.inList(blockType)){
            block.breakNaturally();
        }
    }

    public static ItemStack get() {
        ItemStack pioche = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta piocheMeta = pioche.getItemMeta();
        piocheMeta.setCustomModelData(2);
        piocheMeta.setDisplayName("§bPioche Surchargée");
        pioche.setItemMeta(piocheMeta);
        return pioche;
    }

    public static void casser(Block block, BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
            case SOUTH:
                Block block1 = block.getRelative(BlockFace.UP);
                Block block2 = block.getRelative(BlockFace.DOWN);
                Block block3 = block.getRelative(BlockFace.EAST);
                Block block4 = block.getRelative(BlockFace.WEST);
                Block block5 = block1.getRelative(BlockFace.EAST);
                Block block6 = block1.getRelative(BlockFace.WEST);
                Block block7 = block2.getRelative(BlockFace.EAST);
                Block block8 = block2.getRelative(BlockFace.WEST);

                breakBlock(block1);
                breakBlock(block2);
                breakBlock(block3);
                breakBlock(block4);
                breakBlock(block5);
                breakBlock(block6);
                breakBlock(block7);
                breakBlock(block8);
               break;

            case EAST:
            case WEST:
                block1 = block.getRelative(BlockFace.UP);
                block2 = block.getRelative(BlockFace.DOWN);
                block3 = block.getRelative(BlockFace.NORTH);
                block4 = block.getRelative(BlockFace.SOUTH);
                block5 = block1.getRelative(BlockFace.NORTH);
                block6 = block1.getRelative(BlockFace.SOUTH);
                block7 = block2.getRelative(BlockFace.NORTH);
                block8 = block2.getRelative(BlockFace.SOUTH);

                breakBlock(block1);
                breakBlock(block2);
                breakBlock(block3);
                breakBlock(block4);
                breakBlock(block5);
                breakBlock(block6);
                breakBlock(block7);
                breakBlock(block8);
                break;
            case UP:
            case DOWN:
                block1 = block.getRelative(BlockFace.EAST);
                block2 = block.getRelative(BlockFace.WEST);
                block3 = block.getRelative(BlockFace.NORTH);
                block4 = block.getRelative(BlockFace.SOUTH);
                block5 = block.getRelative(BlockFace.SOUTH_EAST);
                block6 = block.getRelative(BlockFace.SOUTH_WEST);
                block7 = block.getRelative(BlockFace.NORTH_EAST);
                block8 = block.getRelative(BlockFace.NORTH_WEST);

                breakBlock(block1);
                breakBlock(block2);
                breakBlock(block3);
                breakBlock(block4);
                breakBlock(block5);
                breakBlock(block6);
                breakBlock(block7);
                breakBlock(block8);
                break;
        }
    }
}
