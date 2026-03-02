package net.dumbcode.projectnublar.api.dinosaur;

public enum Diet {
    HERBIVORE("herbivore",0),
    OMNIVORE("omnivore",1),
    CARNIVORE("carnivore",2);

    private final String name;
    private final int value;

    Diet(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
