package fr.sny1411.tepakap.utils;

import fr.sny1411.tepakap.utils.larguage.ClockEvents;
import org.bukkit.Bukkit;

import java.io.IOException;

public class CurlExecute {
    private static final String urlAdminInfo = "ntfy.sh/tepakapAdminInfo";
    private static final String urlDecoReco = "ntfy.sh/TepakapDecoReco";

    public static void sendAdminInfo(String message) {
        Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin, () -> {
            String command = ("curl -d '" + message + "' " + urlAdminInfo);
            try {
                Process process = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void sendDecoRecoInfi(String message) {
        Bukkit.getScheduler().runTaskAsynchronously(ClockEvents.plugin, () -> {
            String command = "curl -d '" + message + "' " + urlAdminInfo;
            try {
                Process process = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
