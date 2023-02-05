package me.nathan.desmos.ingame.query;

import io.qt.core.QTimer;
import me.nathan.desmos.Desmos;
import me.nathan.desmos.ingame.account.Account;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.ChunkState;
import me.nathan.desmos.ingame.util.Dimension;
import me.nathan.desmos.menu.Menu;
import me.nathan.desmos.menu.tabs.GridTab;

/**
 * Ported from desmos1.0
 */
public class Query {

    private ChunkPosition position;
    private Dimension dimension;
    private Priority priority;
    private Account account;

    private final boolean visualize;

    private int expectedTPID;

    public Query(ChunkPosition position, Dimension dimension, Priority priority, boolean visualize) {
        this.position = position;
        this.dimension = dimension;
        this.priority = priority;
        this.visualize = visualize;
    }

    public void callback(boolean result) {
        if(visualize) {
            Menu.qRunLater(() -> {
                Menu.gridTab.getGridRenderer().updateChunk(
                        new ChunkState(position.getX(), position.getZ(), dimension), result
                );
            });
        }
    }

    public boolean cancelCondition() {
        return false;
    }

    public void reschedule() {
    }

    public void incrementPriority() {
        switch(priority) {
            case LOW: priority = Priority.HIGH; break;
            case HIGH: priority = Priority.USER; break;
            default: priority = Priority.URGENT;
        }
    }

    public enum Priority {
        URGENT,
        USER,
        HIGH,
        LOW
    }

    /* ------------------------- Setters and Getters ------------------------- */

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public ChunkPosition getChunkPosition() {
        return position;
    }

    public void setChunkPosition(ChunkPosition position) {
        this.position = position;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public int getExpectedTPID() {
        return expectedTPID;
    }

    public void setExpectedTPID(int expectedTPID) {
        this.expectedTPID = expectedTPID;
    }

    public Priority getPriority() {
        return  priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean willVisualize() {
        return visualize;
    }
}
