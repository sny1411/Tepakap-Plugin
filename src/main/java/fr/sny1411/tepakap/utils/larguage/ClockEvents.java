package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.Main;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class ClockEvents {
    private static int timeSecond = 0;
    private static final int MAX_TIME_SECOND = 0;
    private static Main plugin;

    public static void startCount(int TickStop) throws InterruptedException {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                while (timeSecond < TickStop) {
                    timeSecond++;
                    TimeUnit.SECONDS.sleep(1);
                }
                // Lancer l'event
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }


}
