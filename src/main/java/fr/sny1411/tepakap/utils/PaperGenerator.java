package fr.sny1411.tepakap.utils;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public class PaperGenerator {
    public static CompletableFuture<Chunk> generateAsyncAt(World world,int x,int z) {
        return world.getChunkAtAsyncUrgently(x,z);
    }
}
