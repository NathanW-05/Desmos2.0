package me.nathan.desmos.ingame.task;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import me.nathan.desmos.Desmos;
import me.nathan.desmos.ingame.query.Query;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.Dimension;

public abstract class Task extends Thread {

    private ChunkPosition startPos;
    private ChunkPosition endPos;

    private final Dimension dimension;
    private final Query.Priority priority;

    private int maxDistance;
    private int minDistance;

    private boolean repeat = true;
    private final int chunkSkip;

    private boolean paused;
    private boolean stop;

    private int queriesAwaitingResult;

    public Task(ChunkPosition startPos, ChunkPosition endPos, Dimension dimension,
                Query.Priority priority, int chunkSkip, boolean repeat) {

        this.startPos = startPos;
        this.endPos = endPos;
        this.dimension = dimension;
        this.priority = priority;
        this.repeat = repeat;
        this.chunkSkip = chunkSkip;
    }

    public Task(int maxDistance, int minDistance, Dimension dimension, Query.Priority priority, int chunkSkip) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.dimension = dimension;
        this.priority = priority;
        this.chunkSkip = chunkSkip;
    }

    public void onLoaded(ChunkPosition position) {

    }

    public void onCallback() {
        queriesAwaitingResult--;
    }

    public void onQuery() {
        queriesAwaitingResult++;
    }

    public void onCompleted() {
        System.out.println("Completed " + this.getClass().getName() + ".");
    }

    public boolean isComplete() {
        return false;
    }

    public ChunkPosition getCurrentPosition() {
        return null;
    }

    public float getCompletionPercentage() {
        return 0;
    }

    public boolean canQuery() {
        return queriesAwaitingResult <= 15 &&
                !Desmos.accountManager.getOnlineAccountsForDimension(dimension).isEmpty();
    }

    /* ------------------------- Threading ------------------------- */

    @Override
    public void run() {

    }

    public void taskPause() {
        this.paused = true;
    }

    public void taskResume() {
        this.paused = false;
    }

    public void taskStop() {
        this.stop = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isStopped() {
        return stop;
    }

    /* ------------------------- Getters and Setters ------------------------- */

    public ChunkPosition getStartPos() {
        return startPos;
    }

    public ChunkPosition getEndPos() {
        return endPos;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public Query.Priority getQueryPriority() {
        return priority;
    }

    public boolean shouldRepeat() {
        return repeat;
    }

    public int getChunkSkip() {
        return chunkSkip;
    }
}
