package me.nathan.desmos.ingame.connection;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerOpenWindowPacket;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

import me.nathan.desmos.Desmos;
import me.nathan.desmos.ingame.account.Account;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.Dimension;
import me.nathan.desmos.menu.Menu;
import me.nathan.desmos.menu.util.Point;

/**
 * Ported from Desmos1.0
 */
public class ConnectionAdapter extends SessionAdapter {

    private final Account account;

    private long timeSinceLastPacket;

    private boolean reSyncing;

    public ConnectionAdapter(Account account) {
        this.account = account;

        lagDetector();
    }

    private void lagDetector() {
        new Thread(() -> {
            while(true) {
                try {Thread.sleep(1);}catch(InterruptedException e){throw new RuntimeException(e);}

                if(System.currentTimeMillis() - timeSinceLastPacket >= 100) {
                    account.setLagging(true);
                }
                if(!account.hasConfirmedChest() && !reSyncing && System.currentTimeMillis()
                        - account.getLastConfirmChestTime() > 1000 && account.isInitialized()) {
                    reSyncing = true;
                    account.setCurrentTPID(account.getLastReceivedTPID());
                    account.getChestHandler().openChest();
                }
            }
        }).start();
    }

    @Override
    public void packetReceived(PacketReceivedEvent event) {
        account.getChestHandler().packetEvent(event);
        Desmos.queryHandler.packetReceived(event);

        if (event.getPacket() instanceof ServerPlayerPositionRotationPacket) {
            ServerPlayerPositionRotationPacket packet = event.getPacket();
            account.getSession().send(new ClientTeleportConfirmPacket(packet.getTeleportId()));

            if (!account.isInitialized()) {
                System.out.println(account.getUsername() + " has connected successfully at position: " +
                        String.format("%.1f", packet.getX()) + ", " + String.format("%.1f", packet.getZ()));
                account.setInitialized(true);

                account.setChunkPosition(new ChunkPosition((int) (packet.getX() / 16), (int) (packet.getZ() / 16)));
                account.setBlockPosition(new Position((int) packet.getX(), (int) packet.getY(), (int) packet.getZ()));

                Menu.qRunLater(() -> {
                    Menu.gridTab.getGridRenderer().updatePoint(new Point(account.getChunkPosition(),
                            account.getDimension(), account.getId(), Point.PointType.ACCOUNT,account));
                });

                account.getChestHandler().updateNearestChest();
                account.getChestHandler().openChest();

                Desmos.accountManager.onlineAccounts.add(account);
            }
            account.setLastReceivedTPID(packet.getTeleportId());
        }

        if (event.getPacket() instanceof ServerJoinGamePacket) {
            ServerJoinGamePacket packet = event.getPacket();

            account.setCurrentTPID(1);
            account.setDimension(Dimension.fromMC(packet.getDimension()));
        }

        if (event.getPacket() instanceof ServerCloseWindowPacket) {
            account.setConfirmedChest(false);
            account.getChestHandler().openChest();
            account.setLastConfirmChestTime(System.currentTimeMillis());
        }

        if(event.getPacket() instanceof ServerOpenWindowPacket) {
            if(reSyncing) reSyncing = false;
            account.setConfirmedChest(true);
            account.setLastConfirmChestTime(System.currentTimeMillis());
        }

        if(!(event.getPacket() instanceof ServerChatPacket)) {
            timeSinceLastPacket = System.currentTimeMillis();
            account.setLagging(false);
        }

        if(event.getPacket() instanceof ServerPlayerHealthPacket) {
            ServerPlayerHealthPacket packet = event.getPacket();

            account.setHealth(packet.getHealth());
            account.setHunger(packet.getFood());
        }
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        System.out.println(account.getUsername() + " was disconnected for: " + event.getReason() + ".");

        Desmos.accountManager.onlineAccounts.remove(account);
        account.setInitialized(false);
        account.setLagging(false);
        account.setLastConfirmChestTime(Long.MAX_VALUE);

        Menu.qRunLater(() -> {
            Menu.gridTab.getGridRenderer().removePoint(account.getId());
        });

        if(account.willAutoReconnect()) {
            account.setLastConnectAttemptTime(System.currentTimeMillis());
            Desmos.accountManager.queueLogin(account);
        }

        event.getCause().printStackTrace();
    }
}
