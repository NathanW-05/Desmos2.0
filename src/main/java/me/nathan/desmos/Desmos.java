package me.nathan.desmos;

import me.nathan.desmos.ingame.account.Account;
import me.nathan.desmos.ingame.account.AccountManager;
import me.nathan.desmos.ingame.query.Query;
import me.nathan.desmos.ingame.query.QueryHandler;
import me.nathan.desmos.ingame.task.tasks.BasicScanTask;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.Dimension;
import me.nathan.desmos.menu.Menu;
import me.nathan.desmos.util.FileManager;

public class Desmos {

    static {
        /* We want to make sure we add QTJambi bindings as well as standard QT binaries to the classpath. */
        System.setProperty("java.library.path","C:\\Users\\Natha\\Projects\\Desmos2.0\\bin;" +
                "C:\\Qt\\6.3.1\\msvc2019_64\\bin");
    }

    public static final Desmos instance = new Desmos();

    private static Menu menu;

    public static final AccountManager accountManager = new AccountManager();
    public static final QueryHandler queryHandler = new QueryHandler();
    public static final FileManager fileManger = new FileManager();

    public static void main(String[] args) throws InterruptedException {
        /* get rid of dumb red warning */
        System.err.close();
        System.setErr(System.out);

        accountManager.queueLogin(new Account("email", "psswd"));

        BasicScanTask task = new BasicScanTask(new ChunkPosition(5000 / 16, 5000 / 16),
                new ChunkPosition(-5000 / 16, -5000 / 16),
                Dimension.OVERWORLD, Query.Priority.LOW, 12, false);
        task.start();

        menu = new Menu(args);
    }

    /* ------------------------- Getters and Setters ------------------------- */

    public Menu getMenu() {
        return menu;
    }
}
