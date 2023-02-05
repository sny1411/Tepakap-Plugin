package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.utils.capacite.CapaciteManager;
import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Fly implements CommandExecutor {

    private static HashMap<UUID,Boolean> cooldown = new HashMap<>();
    public static HashMap<UUID,Boolean> actif = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            return true;
        }
        Player player = (Player) commandSender;
        UUID playerID = player.getUniqueId();
        if (CapaciteManager.isInCapacite("Superman",playerID)) {
            int levelComp = CapaciteManager.getLevelCapacite("Superman", playerID);
            switch (levelComp) {
                case 1:
                    if (!actif.containsKey(playerID)) {
                        actif.put(playerID,false);
                    }
                    if (!cooldown.containsKey(playerID)) {
                        cooldown.put(playerID,false);
                    }

                    if (!actif.get(playerID) && !cooldown.get(playerID)) {
                        player.sendMessage("§7Vous pouvez désormais voler");
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        actif.put(playerID,true);
                        Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin, () -> {
                            try {
                                TimeUnit.SECONDS.sleep(20);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            player.sendMessage("§7Vous ne pouvez plus voler");
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            actif.put(playerID,false);
                            cooldown.put(playerID,true);

                            try {
                                TimeUnit.MINUTES.sleep(5);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            cooldown.put(playerID,false);
                        });
                    } else {
                        player.sendMessage("§7Cooldown encore actif");
                    }
                    break;
                case 2:
                    if (!actif.containsKey(playerID)) {
                        actif.put(playerID,false);
                    }
                    if (!cooldown.containsKey(playerID)) {
                        cooldown.put(playerID,false);
                    }

                    if (!actif.get(playerID) && !cooldown.get(playerID)) {
                        player.sendMessage("§7Vous pouvez désormais voler");
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        actif.put(playerID,true);
                        Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin, () -> {
                            try {
                                TimeUnit.MINUTES.sleep(1);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            player.sendMessage("§7Vous ne pouvez plus voler");
                            player.setAllowFlight(false);
                            player.setFlying(false);
                            actif.put(playerID,false);
                            cooldown.put(playerID,true);

                            try {
                                TimeUnit.SECONDS.sleep(150);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            cooldown.put(playerID,false);
                        });
                    } else {
                        player.sendMessage("§7Cooldown encore actif");
                    }
                    break;
                case 3:
                    if (!actif.containsKey(playerID)) {
                        actif.put(playerID,false);
                    }
                    if (!actif.get(playerID)) {
                        player.sendMessage("§7Vous pouvez désormais voler");
                        player.setAllowFlight(true);
                        player.setFlying(true);
                        actif.put(playerID,true);
                    } else {
                        player.sendMessage("§7Vous ne pouvez plus voler");
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        actif.put(playerID,false);
                    }
                    break;
            }
        }
        return false;
    }
}
