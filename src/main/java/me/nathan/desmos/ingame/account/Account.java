package me.nathan.desmos.ingame.account;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.packetlib.Session;
import me.nathan.desmos.ingame.connection.Connection;
import me.nathan.desmos.ingame.connection.ConnectionAdapter;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.Dimension;
import me.nathan.desmos.util.IDManager;

/**
 * Ported from Desmos1.0
 */
public class Account {

    private String username;
    private String email;
    private String password;

    private Session session;

    private ChunkPosition chunkPosition;

    private Dimension dimension;

    private Position blockPosition;

    private float health;

    private int id;
    private int hunger;
    private int currentTPID;
    private int lastReceivedTPID;

    private boolean initialized;
    private boolean confirmedChest;
    private boolean receiving;
    private boolean lagging;
    private boolean connected;
    private boolean autoReconnect = true;

    private long loginDelay = 9000;
    private long lastConnectAttemptTime;
    private long lastSendTeleportTime;
    private long lastConfirmChestTime = Long.MAX_VALUE;

    private ConnectionAdapter connectionAdapter;

    private final ChestHandler chestHandler;

    public Account(String email, String password) {
        this.email = email;
        this.password = password;

        chestHandler = new ChestHandler(this);
        id = IDManager.getNewID();
    }

    /* ------------------------- Getters and Setters ------------------------- */

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Session getSession() {
        return this.session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setChunkPosition(ChunkPosition chunkPosition) {
        this.chunkPosition = chunkPosition;
    }

    public ChunkPosition getChunkPosition() {
        return chunkPosition;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public Position getBlockPosition() {
        return blockPosition;
    }

    public void setBlockPosition(Position blockPosition) {
        this.blockPosition = blockPosition;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public int getCurrentTPID() {
        return currentTPID;
    }

    public void setCurrentTPID(int currentTPID) {
        this.currentTPID = currentTPID;
    }

    public int getLastReceivedTPID() {
        return lastReceivedTPID;
    }

    public void setLastReceivedTPID(int lastReceivedTPID) {
        this.lastReceivedTPID = lastReceivedTPID;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean hasConfirmedChest() {
        return confirmedChest;
    }

    public void setConfirmedChest(boolean confirmedChest) {
        this.confirmedChest = confirmedChest;
    }

    public boolean isReceiving() {
        return receiving;
    }

    public void setReceiving(boolean receiving) {
        this.receiving = receiving;
    }

    public boolean isLagging() {
        return lagging;
    }

    public void setLagging(boolean lagging) {
        this.lagging = lagging;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public long getLoginDelay() {
        return loginDelay;
    }

    public void setLoginDelay(long loginDelay) {
        this.loginDelay = loginDelay;
    }

    public long getLastSendTeleportTime() {
        return lastSendTeleportTime;
    }

    public void setLastSendTeleportTime(long lastSendTeleportTime) {
        this.lastSendTeleportTime = lastSendTeleportTime;
    }

    public long getLastConnectAttemptTime() {
        return lastConnectAttemptTime;
    }

    public void setLastConnectAttemptTime(long lastConnectAttemptTime) {
        this.lastConnectAttemptTime = lastConnectAttemptTime;
    }

    public long getLastConfirmChestTime() {
        return lastConfirmChestTime;
    }

    public void setLastConfirmChestTime(long lastConfirmChestTime) {
        this.lastConfirmChestTime = lastConfirmChestTime;
    }

    public boolean willAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public ChestHandler getChestHandler() {
        return chestHandler;
    }

    public int getId() {
        return id;
    }

    public void setConnectionAdapter(ConnectionAdapter connectionAdapter) {
        this.connectionAdapter = connectionAdapter;
    }

    public ConnectionAdapter getConnectionAdapter() {
        return connectionAdapter;
    }
}
