package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.utils.Random;

public enum Rarete {
    COMMUN(1),RARE(5),EPIQUE(7),LEGENDAIRE(10);
    private final int rare;

    Rarete(int rare) {
        this.rare = rare;
    }

    public static Rarete choiceRare() {
        int rand = Random.random(0,10);
        if (rand >= LEGENDAIRE.rare) {
            return LEGENDAIRE;
        }
        if (rand >= EPIQUE.rare) {
            return EPIQUE;
        }
        if (rand >= RARE.rare) {
            return RARE;
        }
        if (rand >= COMMUN.rare) {
            return COMMUN;
        }
        return null;
    }
}
