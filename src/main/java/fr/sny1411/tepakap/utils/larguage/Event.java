package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.PaperGenerator;
import fr.sny1411.tepakap.utils.Random;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Event {
    private static final int COORD_MIN = -10000;
    private static final int COORD_MAX = 10000;
    private static MysqlDb bdd;
    private static Main plugin;

    private int coordX;
    private int coordZ;
    private Location locChest;
    public Rarete rarete;

    public Event(Main plugin, MysqlDb bdd) {
        this.plugin = plugin;
        this.bdd = bdd;
    }

    public ArmorStand armorStand;
    public void chestSpawn() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            AtomicBoolean canSpawn = new AtomicBoolean(false);
            do {
                coordX = Random.random(COORD_MIN,COORD_MAX);
                coordZ = Random.random(COORD_MIN,COORD_MAX);
                double debut = System.currentTimeMillis();
                World world = Bukkit.getWorlds().get(0); // get overworld
                double debut2 = System.currentTimeMillis();

                CompletableFuture<Chunk> chunkLoad = PaperGenerator.generateAsyncAt(world,coordX,coordZ);
                chunkLoad.thenRun(() -> {
                    Block highestBlock = world.getHighestBlockAt(coordX, coordZ);
                    Bukkit.getConsoleSender().sendMessage("getHighestBlock : " + (System.currentTimeMillis() - debut2));
                    locChest = (new Location(world, coordX + 0.5, highestBlock.getY() + 1, coordZ + 0.5));
                    Bukkit.getConsoleSender().sendMessage("iteration");
                    if (highestBlock.getType().isSolid()) canSpawn.set(true);
                    Bukkit.getConsoleSender().sendMessage("locationArmorStand : " + (System.currentTimeMillis() - debut));
                });
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (!canSpawn.get());

            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            rarete = Rarete.choiceRare();
            bdd.putNewItems("INSERT INTO LARGUAGE(type_larguage,date_larguage,larguage_obtenu,UUID) VALUES('" + rarete.name() + "','"
                                                                    + datetime + "',"
                                                                    + "FALSE,"
                                                                    + "NULL)");
            coordApproximate(coordX,coordZ);
            armorStandSpawn(locChest);
        });
    }

    private void armorStandSpawn(Location loc) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            double debut = System.currentTimeMillis();
            Bukkit.getConsoleSender().sendMessage("Chest spawn in : " + loc.getX() + " " + loc.getZ());
            armorStand = loc.getWorld().spawn(loc, ArmorStand.class);
            armorStand.setArms(true);
            armorStand.setBasePlate(false);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setInvulnerable(true);
            ItemStack chest = new ItemStack(Material.PAPER);
            ItemMeta chestMeta = chest.getItemMeta();
            chestMeta.setCustomModelData(1);
            chest.setItemMeta(chestMeta);
            armorStand.getEquipment().setItemInMainHand(chest);
            armorStand.setRightArmPose(armorStand.getRightArmPose().setX(0.0));
            armorStand.setRightArmPose(armorStand.getRightArmPose().setZ(0.0));
            Bukkit.getConsoleSender().sendMessage("armorStandSpawn : " + (System.currentTimeMillis() - debut));
        });
    }

    public void chestDespawn() {
        Bukkit.getConsoleSender().sendMessage("ArmorStand DELETE");
        armorStand.setInvulnerable(false);
        armorStand.setInvisible(false);
        UUID asUUID = armorStand.getUniqueId();
        Bukkit.getServer().getEntity(asUUID).remove();
    }

    private void coordApproximate(int x, int z) {
        int xChoice  = Random.random(0,1);
        int zChoice  = Random.random(0,1);

        if (xChoice == 0) {
            x -= Random.random(10,50);
        } else {
            x += Random.random(10,50);
        }

        if (zChoice == 0) {
            z -= Random.random(10,50);
        } else {
            z += Random.random(10,50);
        }

        Bukkit.broadcastMessage("§a[Larguage] Un larguage viens d'apparaitre aux  alentours de " + x + " " + z);
    }


}
