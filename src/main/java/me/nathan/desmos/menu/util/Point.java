package me.nathan.desmos.menu.util;

import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.Dimension;

public class Point {

    private ChunkPosition chunkPosition;
    private Dimension dimension;

    private PointType pointType;

    private Object parent;

    private final int id;

    public Point(ChunkPosition chunkPosition, Dimension dimension, int id, PointType pointType, Object parent) {
        this.chunkPosition = chunkPosition;
        this.dimension = dimension;
        this.id = id;
        this.pointType = pointType;
        this.parent = parent;
    }

    public enum PointType {
        ACCOUNT,
        PLAYER,
        POSSIBLE_BASE
    }

    /* ------------------------- Getters and Setters ------------------------- */

    public ChunkPosition getChunkPosition() {
        return chunkPosition;
    }

    public void setChunkPosition(ChunkPosition chunkPosition) {
        this.chunkPosition = chunkPosition;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public int getId() {
        return id;
    }

    public PointType getPointType() {
        return pointType;
    }

    public void setPointType(PointType pointType) {
        this.pointType = pointType;
    }

    public Object getParent() {
        return parent;
    }

    public void setParent(Object parent) {
        this.parent = parent;
    }
}
