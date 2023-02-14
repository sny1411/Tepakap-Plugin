package fr.sny1411.tepakap.utils.larguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EventsManager {
    public static List<Event> listEvent = new ArrayList<>();
    public static HashMap<UUID, Boolean> ChestAttack = new HashMap<>();
    public static HashMap<UUID, Boolean> EventFinish = new HashMap<>();
}
