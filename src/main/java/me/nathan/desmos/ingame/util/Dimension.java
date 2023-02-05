package me.nathan.desmos.ingame.util;

public enum Dimension {
    NETHER(-1),
    OVERWORLD(0),
    END(1);

    public static Dimension fromMC(int mcDim) {
        return values()[Math.min(2, Math.max(0, mcDim + 1))];
    }

    private final int mcDim;

    Dimension(int mcDim) {
        this.mcDim = mcDim;
    }

    public int getMCDim() {
        return mcDim;
    }
}