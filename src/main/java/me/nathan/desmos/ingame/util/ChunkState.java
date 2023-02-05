package me.nathan.desmos.ingame.util;

import java.util.Objects;

public class ChunkState{

    private int x;
    private int z;
    private Dimension dimension;

    public ChunkState(int x, int z, Dimension dimension) {
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

    /* ------------------------- Setters and Getters ------------------------- */

    public int[] getBlockPosition() {
        return new int[] {x * 16, z * 16};
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ChunkState newState = (ChunkState) other;
        return (newState.dimension == dimension && newState.x == x &&
                newState.z == z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
