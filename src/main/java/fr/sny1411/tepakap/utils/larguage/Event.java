package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.PaperGenerator;
import fr.sny1411.tepakap.utils.Random;
import net.kyori.adventure.audience.Audience;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Event {
    private static final int COORD_MIN = -10000;
    private static final int COORD_MAX = 10000;
    private static MysqlDb bdd;
    public Player slayer;
    private static Main plugin;
    public List<UUID> listMobsId;

    private int coordX;
    private int coordZ;
    private Location locChest;
    public Rarete rarete;


    public Event(Main plugin, MysqlDb bdd) {
        this.plugin = plugin;
        this.bdd = bdd;
        this.listMobsId = new ArrayList<>();
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


            rarete = Rarete.choiceRare();
            coordApproximate(coordX,coordZ);
            armorStandSpawn(locChest);
            // clock 30 min
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
            EventsManager.ChestAttack.put(armorStand.getUniqueId(),false);
            String datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            bdd.putNewItems("INSERT INTO LARGUAGE(type_larguage,date_larguage,larguage_obtenu,UUID,UUID_CHEST) VALUES('" + rarete.name() + "','"
                    + datetime + "',"
                    + "FALSE,"
                    + "NULL,'"
                    + armorStand.getUniqueId() +"')");
        });
    }

    public void mobsDespawn() {
        for (UUID mobId : listMobsId) {
            Bukkit.getServer().getEntity(mobId).remove();
        }
        listMobsId.clear();
    }

    public void chestDespawn() {
        Bukkit.getConsoleSender().sendMessage("ArmorStand DELETE");
        armorStand.setInvulnerable(false);
        armorStand.setInvisible(false);
        UUID asUUID = armorStand.getUniqueId();
        Bukkit.getServer().getEntity(asUUID).remove();
        EventsManager.listEvent.remove(this);
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

    public void attack() {
        EventsManager.ChestAttack.put(armorStand.getUniqueId(),true);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            slayer.sendMessage("§a[Largage] §f Largage de type : " + rarete);
            slayer.sendMessage("Préparez vous !");
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (int i = 3; i > 1; i--) {
                slayer.sendMessage(String.valueOf(i));
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                spawnMobs();
            });
            int tempMax = Rarete.tempRound(rarete);
            while (tempMax!=0 && EventsManager.ChestAttack.get(armorStand.getUniqueId())) {
                try {
                    slayer.sendMessage(String.valueOf(tempMax));
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                tempMax--;
            }
            if (tempMax==0) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    slayer.sendMessage("Temps écoulé");
                    mobsDespawn();
                });
            }


        });
    }

private Location approximateSpawn() {
    int xChoice  = Random.random(0,1);
    int zChoice  = Random.random(0,1);
    int x = (int) locChest.getX();
    int z = (int) locChest.getZ();

    if (xChoice == 0) {
        x -= Random.random(0,3);
    } else {
        x += Random.random(0,3);
    }

    if (zChoice == 0) {
        z -= Random.random(0,3);
    } else {
        z += Random.random(0,3);
    }

    return new Location(locChest.getWorld(),x,locChest.getY()+3,z);
}

    private void spawnMobs() {
        switch (rarete) {
            case COMMUN:
                for (int i = 0; i < 10; i++) {
                    Location loc = approximateSpawn();
                    Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                    zombie.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                    zombie.setShouldBurnInDay(false);
                    zombie.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(zombie.getUniqueId());
                }
                for (int i = 0; i < 5; i++) {
                    Location loc = approximateSpawn();
                    Skeleton skeleton = (Skeleton) loc.getWorld().spawnEntity(loc, EntityType.SKELETON);
                    skeleton.setShouldBurnInDay(false);
                    skeleton.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(skeleton.getUniqueId());
                }
                for (int i = 0; i < 3; i++) {
                    Location loc = approximateSpawn();
                    Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                    zombie.setShouldBurnInDay(false);
                    zombie.setBaby();
                    zombie.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(zombie.getUniqueId());
                }
                break;
            case RARE:
                for (int i = 0; i < 10; i++) {
                    Location loc = approximateSpawn();
                    Spider spider = (Spider) loc.getWorld().spawnEntity(loc, EntityType.SPIDER);
                    spider.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 1));
                    spider.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(spider.getUniqueId());
                }
                for (int i = 0; i < 10; i++) {
                    Location loc = approximateSpawn();
                    Silverfish silverfish = (Silverfish) loc.getWorld().spawnEntity(loc, EntityType.SILVERFISH);
                    silverfish.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(silverfish.getUniqueId());
                }
                for (int i = 0; i < 5; i++) {
                    Location loc = approximateSpawn();
                    Stray stray = (Stray) loc.getWorld().spawnEntity(loc, EntityType.STRAY);
                    stray.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(stray.getUniqueId());
                }
                break;
            case EPIQUE:
                for (int i = 0; i < 10; i++) {
                    Location loc = approximateSpawn();
                    CaveSpider caveSpider = (CaveSpider) loc.getWorld().spawnEntity(loc, EntityType.CAVE_SPIDER);
                    caveSpider.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 1));
                    caveSpider.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(caveSpider.getUniqueId());
                }

                for (int i = 0; i < 5; i++) {
                    Location loc = approximateSpawn();
                    Spider spider = (Spider) loc.getWorld().spawnEntity(loc, EntityType.SPIDER);
                    spider.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 2));
                    spider.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(spider.getUniqueId());
                }

                for (int i = 0; i < 2; i++) {
                    Location loc = approximateSpawn();
                    Illusioner illusioner = (Illusioner) loc.getWorld().spawnEntity(loc, EntityType.ILLUSIONER);
                    illusioner.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(illusioner.getUniqueId());
                }
                break;
            case LEGENDAIRE:
                Location loc1 = approximateSpawn();
                Ravager ravager = (Ravager) loc1.getWorld().spawnEntity(loc1,EntityType.RAVAGER);
                ravager.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
                ravager.setLootTable(LootTables.EMPTY.getLootTable());
                listMobsId.add(ravager.getUniqueId());

                for (int i = 0; i < 3; i++) {
                    Location loc = approximateSpawn();
                    Evoker evoker = (Evoker) loc.getWorld().spawnEntity(loc,EntityType.EVOKER);
                    evoker.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(evoker.getUniqueId());
                }

                for (int i = 0; i < 5; i++) {
                    Location loc = approximateSpawn();
                    Vindicator vindicator = (Vindicator) loc.getWorld().spawnEntity(loc,EntityType.VINDICATOR);
                    vindicator.setLootTable(LootTables.EMPTY.getLootTable());
                    listMobsId.add(vindicator.getUniqueId());
                }
                break;
        }
    }

}
