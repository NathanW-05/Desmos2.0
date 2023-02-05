package me.nathan.desmos.ingame.query;

import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.world.ClientTeleportConfirmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.window.ServerCloseWindowPacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import me.nathan.desmos.Desmos;
import me.nathan.desmos.ingame.account.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from desmos1.0
 */
public class QueryHandler {

    private final List<Query> queryQueue = new ArrayList<>();
    private final List<Query> processingQueries = new ArrayList<>();
    private final List<Long> dispatchTimes = new ArrayList<>();

    public QueryHandler() {
        runQueryDispatchThread();
    }

    private long time;

    public void runQueryDispatchThread() {
        new Thread(() -> {
            while(true) {

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Query query = null;
                Account account = null;

                if(queryQueue.isEmpty()) continue;

                for(int i = queryQueue.size() - 1; i > -1; --i) {
                    query = queryQueue.get(i);

                    account = getBestAccount(query);
                    if(account != null) break;
                }

                if(query == null || account == null) continue;

                query.setAccount(account);
                query.getAccount().setLastSendTeleportTime(System.currentTimeMillis());

                queryChunk(query);
                synchronized (queryQueue) {
                    queryQueue.remove(query);
                }
                dispatchTimes.add(System.currentTimeMillis());
                if(dispatchTimes.size() > 20) dispatchTimes.remove(0);
            }
        }).start();
    }

    public void packetReceived(PacketReceivedEvent event) {
        Account account = null;

        for(Account acc : Desmos.accountManager.onlineAccounts) {
            if(acc.getSession() == event.getSession()) account = acc;
        }

        if(account == null) return;

        if(event.getPacket() instanceof ServerCloseWindowPacket) {
            ServerCloseWindowPacket packet = event.getPacket();
            account.setReceiving(true);

        } else if(event.getPacket() instanceof ServerPlayerPositionRotationPacket) {
            ServerPlayerPositionRotationPacket packet = event.getPacket();

            boolean wasReceiving = account.isReceiving();

            for(Query query : new ArrayList<>(processingQueries)) {
                if(query == null) continue;
                if(query.getAccount() != account) continue;

                if(wasReceiving) {
                    account.setReceiving(false);
                    if(query.getExpectedTPID() == packet.getTeleportId()) { // loaded query
                        query.callback(true);
                        processingQueries.remove(query);
                    } else {
                        query.incrementPriority();
                        processingQueries.remove(query);
                        Query newQuery = new Query(query.getChunkPosition(),
                                query.getDimension(), query.getPriority(), query.willVisualize()) {
                            @Override
                            public void callback(boolean result) {
                                query.callback(result);
                            }
                            @Override
                            public boolean cancelCondition() {
                                return query.cancelCondition();
                            }
                        };
                        scheduleQuery(newQuery);
                    }
                } else if(packet.getTeleportId() >= query.getExpectedTPID()) { // unloaded query
                    query.callback(false);
                    processingQueries.remove(query);
                }
            }
        }
    }

    private void queryChunk(Query query) {
        query.getAccount().getSession().send(new ClientPlayerPositionPacket(false,
                query.getChunkPosition().getX() * 16, 5000, query.getChunkPosition().getZ() * 16));
        query.getAccount().getSession().send(new ClientTeleportConfirmPacket(query.getAccount().getCurrentTPID() + 1));
        query.getAccount().setCurrentTPID(query.getAccount().getCurrentTPID() + 1);
        query.setExpectedTPID(query.getAccount().getCurrentTPID());
        processingQueries.add(query);
    }

    private Account getBestAccount(Query query) {
        for(Account account : Desmos.accountManager.onlineAccounts) {
            if(account.isInitialized() && !account.isLagging() && account.hasConfirmedChest() &&
                    account.getDimension() ==
                            query.getDimension() &&
                    System.currentTimeMillis() -
                    account.getLastSendTeleportTime() > 50 / 2) {
                return account;
            }
        }
        return null;
    }

    public float calculateQueryRate(boolean perAccount) {
        int timeDifferences = 0;

        synchronized (dispatchTimes) {
            if(dispatchTimes.isEmpty()) return 0;
            for(int i = 1; i < dispatchTimes.size(); ++i) {
                timeDifferences += dispatchTimes.get(i) - dispatchTimes.get(i - 1);
            }
            if(timeDifferences != 0) {
                return 50f / ((float)timeDifferences / dispatchTimes.size());
            }
        }
        return 0;
    }

    public void scheduleQuery(Query query) {
        int priority = query.getPriority().ordinal();
        for (int index = 0; index < queryQueue.size(); ++index) {
            if (queryQueue.get(index).getPriority().ordinal() <= priority) {
                queryQueue.add(index, query);
                return;
            }
        }
        queryQueue.add(queryQueue.size(), query);
    }
}
