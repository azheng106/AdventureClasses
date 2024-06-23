package me.aquaponics.adventureclasses;

public enum AdventureClass {
    WARRIOR("RED"),
    ARCHER("GREEN"),
    ASSASSIN("DARK_GRAY"),
    HEALER("AQUA"),
    ENGINEER("GOLD");

    public final String color;

    AdventureClass(String color) {
        this.color = color;
    }
}
