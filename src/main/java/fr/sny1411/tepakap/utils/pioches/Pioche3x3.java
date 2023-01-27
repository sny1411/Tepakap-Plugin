package fr.sny1411.tepakap.utils.pioches;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class Pioche3x3 {
    private static void casser(Block block){
        if (block.getType().equals(Material.BEDROCK)){}
        else{
            block.breakNaturally();}
    }

    public static void casser3x3(Block block) {

    }
    if (blockface.equals(BlockFace.UP) || blockface.equals(BlockFace.DOWN)) {

        Block block1 = block.getRelative(BlockFace.EAST);
        Block block2 = block.getRelative(BlockFace.WEST);
        Block block3 = block.getRelative(BlockFace.NORTH);
        Block block4 = block.getRelative(BlockFace.SOUTH);
        Block block5 = block.getRelative(BlockFace.SOUTH_EAST);
        Block block6 = block.getRelative(BlockFace.SOUTH_WEST);
        Block block7 = block.getRelative(BlockFace.NORTH_EAST);
        Block block8 = block.getRelative(BlockFace.NORTH_WEST);

        casser(block1);
        casser(block2);
        casser(block3);
        casser(block4);
        casser(block5);
        casser(block6);
        casser(block7);
        casser(block8);
    }

            if (blockface.equals(BlockFace.EAST) || blockface.equals(BlockFace.WEST)){

        Block block1 = block.getRelative(BlockFace.UP);
        Block block2 = block.getRelative(BlockFace.DOWN);
        Block block3 = block.getRelative(BlockFace.NORTH);
        Block block4 = block.getRelative(BlockFace.SOUTH);
        Block block5 = block1.getRelative(BlockFace.NORTH);
        Block block6 = block1.getRelative(BlockFace.SOUTH);
        Block block7 = block2.getRelative(BlockFace.NORTH);
        Block block8 = block2.getRelative(BlockFace.SOUTH);

        casser(block1);
        casser(block2);
        casser(block3);
        casser(block4);
        casser(block5);
        casser(block6);
        casser(block7);
        casser(block8);
    }

            if (blockface.equals(BlockFace.NORTH) || blockface.equals(BlockFace.SOUTH)){

        Block block1 = block.getRelative(BlockFace.UP);
        Block block2 = block.getRelative(BlockFace.DOWN);
        Block block3 = block.getRelative(BlockFace.EAST);
        Block block4 = block.getRelative(BlockFace.WEST);
        Block block5 = block1.getRelative(BlockFace.EAST);
        Block block6 = block1.getRelative(BlockFace.WEST);
        Block block7 = block2.getRelative(BlockFace.EAST);
        Block block8 = block2.getRelative(BlockFace.WEST);

        casser(block1);
        casser(block2);
        casser(block3);
        casser(block4);
        casser(block5);
        casser(block6);
        casser(block7);
        casser(block8);
    }
}
