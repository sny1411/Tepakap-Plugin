package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.utils.Random;

public enum Rarete {
    COMMUN,RARE,EPIQUE,LEGENDAIRE;

    public static Rarete choiceRare() {
        int rand = Random.random(0,100);
        if (rand < 50) {
            return COMMUN;
        } else if (rand < 87) {
            return RARE;
        } else if (rand < 97) {
            return EPIQUE;
        } else {
            return LEGENDAIRE;
        }
    }

    /***
     *
     * @param rarete
     * @return temps en secondes
     */
    public static int tempRound(Rarete rarete) {
        switch (rarete) {
            case COMMUN:
            case RARE:
                return 300;
            case EPIQUE:
                return 180;
            case LEGENDAIRE:
                return 120;
        }
        return -1;
    }
}
