package me.nathan.desmos.ingame.task.tasks;

import me.nathan.desmos.Desmos;
import me.nathan.desmos.ingame.query.Query;
import me.nathan.desmos.ingame.task.Task;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.Dimension;
import me.nathan.desmos.util.MathUtil;

public class BasicScanTask extends Task {

    private int currentIndex = -1;
    private int maxIndex;

    private ChunkPosition maxPos;
    private ChunkPosition minPos;

    public BasicScanTask(ChunkPosition startPos, ChunkPosition endPos, Dimension dimension,
                         Query.Priority priority, int chunkSkip, boolean repeat) {

        super(startPos, endPos, dimension, priority, chunkSkip, repeat);

        maxPos = new ChunkPosition(startPos.getX() / chunkSkip, startPos.getZ() / chunkSkip);
        minPos = new ChunkPosition(endPos.getX() / chunkSkip, endPos.getZ() / chunkSkip);

        maxIndex = (maxPos.getX() - minPos.getX()) * (maxPos.getZ() - minPos.getZ());
    }

    @Override
    public void run() {
        while(true) {
            if(isPaused()) continue;
            if(isStopped()) break;

            if(isComplete()) {
                onCompleted();
                break;
            }

            if(canQuery()) {
                currentIndex++;
                ChunkPosition currentPosition = getCurrentPosition();

                super.onQuery();

                Desmos.queryHandler.scheduleQuery(new Query(currentPosition, getDimension(),
                        getQueryPriority(), false) {
                    @Override
                    public void callback(boolean loaded) {
                        super.callback(loaded);
                        onCallback();

                        if(loaded) {
                            onLoaded(currentPosition);
                        }
                    }
                });
            }

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public ChunkPosition getCurrentPosition() {
        return new ChunkPosition(
                (currentIndex % (maxPos.getX() - minPos.getX()) + minPos.getX()) * getChunkSkip(),
                (Math.floorDiv(currentIndex, maxPos.getZ() - minPos.getZ()) + minPos.getZ()) * getChunkSkip()
        );
    }

    @Override
    public boolean isComplete() {
        return currentIndex >= maxIndex - 1;
    }

    @Override
    public float getCompletionPercentage() {
        return (float) Math.min(100f, MathUtil.round((float)currentIndex / maxIndex * 100f, 1));
    }

    @Override
    public void onLoaded(ChunkPosition position) {
        System.out.println(this.getClass().getName() + " found a loaded chunk at: " +
                position.getZ() + ", " + position.getZ());
    }
}
