package fr.sny1411.tepakap.utils;

public class Random {
    public static int random(int min,int max) {
        return (int) (Math.random() * ((max - min) + 1) + min);
    }
}
