package fr.sny1411.tepakap.utils;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.UUID;

public class Teleporteur {
    public static HashMap<UUID, Location> tempLoc = new HashMap<>();
    public static HashMap<UUID, Boolean> coolDown = new HashMap<>();
}
