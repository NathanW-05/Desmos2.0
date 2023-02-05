package me.nathan.desmos.ingame.account;

import com.github.steveice10.packetlib.Session;
import me.nathan.desmos.ingame.connection.Connection;
import me.nathan.desmos.ingame.util.Dimension;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Ported from Desmos1.0
 */
public class AccountManager {

    public final ArrayList<Account> onlineAccounts = new ArrayList<>();
    public final CopyOnWriteArrayList<Account> allAccounts = new CopyOnWriteArrayList<>();
    public final CopyOnWriteArrayList<Account> loginQueue = new CopyOnWriteArrayList<>();

    public AccountManager() {
        startLoginThread();
    }

    private void startLoginThread() {
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(50);

                    if(loginQueue.isEmpty()) continue;
                    for(Account account : loginQueue) {
                        if(System.currentTimeMillis() - Connection.timeOfLastLogin > account.getLoginDelay()) {
                            Connection.timeOfLastLogin = System.currentTimeMillis();
                            account.setLoginDelay(Math.min(account.getLoginDelay() * 2, 300000));

                            System.out.println("Attempting to connect " + (account.getUsername() != null ?
                                    account.getUsername() : account.getEmail()) + "...");

                            Connection.login(account, "constantiam.net", 25565);
                            loginQueue.remove(account);

                            Thread.sleep(8000);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void queueLogin(Account account) {
        allAccounts.add(account);
        try {
            loginQueue.add(account);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Account getOnlineAccount(String name) {
        for(Account account : onlineAccounts) {
            if(Objects.equals(account.getUsername(), name)) return account;
        }
        return null;
    }

    public Account getAccount(String name) {
        for(Account account : allAccounts) {
            if(Objects.equals(account.getUsername(), name)) return account;
        }
        return null;
    }

    public Account getOnlineAccount(Session session) {
        for(Account account : onlineAccounts) {
            if(Objects.equals(account.getSession(), session)) return account;
        }
        return null;
    }

    public ArrayList<Account> getOnlineAccountsForDimension(Dimension dimension) {
        ArrayList<Account> list = new ArrayList<>();
        for(Account account : onlineAccounts) {
            if(account.getDimension() == dimension) {
                list.add(account);
            }
        }
        return list;
    }

    public void disconnectAccount(Account account, String reason) {
        account.getSession().disconnect(reason);
    }
}
