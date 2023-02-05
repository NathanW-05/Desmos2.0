package me.nathan.desmos.util;

public class IDManager {

    private static int lastUsedID = -1;

    public static int getNewID() {
        return ++lastUsedID;
    }
}
