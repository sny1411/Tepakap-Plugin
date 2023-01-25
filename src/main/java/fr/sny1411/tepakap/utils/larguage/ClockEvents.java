package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.utils.Random;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClockEvents {
    private static int timeSecond = 0;
    private static final int MAX_TIME_SECOND = 14400; // 4h
    private static final int MIN_TIME_SECOND = 9000; // 2h30
    public static List<Event> listEvent = new ArrayList<>();
    public static Main plugin;

    private static void startCountEvent(int TickStop) throws InterruptedException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                while (timeSecond < TickStop) {
                    timeSecond++;
                    TimeUnit.SECONDS.sleep(1);
                }

                startEvent();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void startEvent() {
        java.time.LocalTime time = java.time.LocalTime.now();
        int nextEventSeconds = Random.random(MIN_TIME_SECOND,MAX_TIME_SECOND);
        if ((((((int)(nextEventSeconds/3600))+time.getHour())%24) < 3)) {
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
