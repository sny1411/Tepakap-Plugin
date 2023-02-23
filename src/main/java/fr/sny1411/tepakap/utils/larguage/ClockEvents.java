package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import fr.sny1411.tepakap.utils.Random;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class ClockEvents {
    private static int timeSecond = 0;
    private static final int MAX_TIME_SECOND = 14400; // 4h
    private static final int MIN_TIME_SECOND = 7200; // 2h
    public static Main plugin;
    public static MysqlDb bdd;

    private static void startCountEvent(int TickStop) throws InterruptedException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                while (timeSecond < TickStop) {
                    timeSecond++;
                    TimeUnit.SECONDS.sleep(1);
                }
                Event event = new Event(plugin,bdd);
                event.chestSpawn();
                EventsManager.listEvent.add(event);
                startEvent();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void startEvent() {
        java.time.LocalTime time = java.time.LocalTime.now();
        int nextEventSeconds = Random.random(MIN_TIME_SECOND,MAX_TIME_SECOND);
        if (((((nextEventSeconds/3600) +time.getHour())%24) > 2)) {
            return;
        }
        try {
            Bukkit.getConsoleSender().sendMessage("Event dans : " + (float)(nextEventSeconds / 60) + " min");
            startCountEvent(nextEventSeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
