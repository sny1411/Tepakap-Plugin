package fr.sny1411.tepakap.utils.larguage;

import fr.sny1411.tepakap.utils.Random;

public enum Rarete {
    COMMUN(1),RARE(5),EPIQUE(7),LEGENDAIRE(10);
    private final int rarete;

    Rarete(int rare) {
        this.rarete = rare;
    }

    public static Rarete choiceRare() {
        int rand = Random.random(0,10);
        if (rand >= LEGENDAIRE.rarete) {
            return LEGENDAIRE;
        }
        if (rand >= EPIQUE.rarete) {
            return EPIQUE;
        }
        if (rand >= RARE.rarete) {
            return RARE;
        } else {
            return COMMUN;
        }
    }
}
