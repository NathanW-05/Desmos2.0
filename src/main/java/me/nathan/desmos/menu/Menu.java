package me.nathan.desmos.menu;

import io.qt.QtMetaType;
import io.qt.core.QMetaObject;
import io.qt.core.QObject;
import io.qt.core.QTimer;
import io.qt.core.Qt;
import io.qt.gui.QIcon;
import io.qt.widgets.*;
import me.nathan.desmos.Desmos;
import me.nathan.desmos.menu.rendering.GridRenderer;
import me.nathan.desmos.menu.tabs.GridTab;

import java.awt.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.concurrent.CopyOnWriteArrayList;

public class Menu {

    private QMainWindow mainWindow;
    private QTabWidget tabWidget;

    public static GridTab gridTab;

    private static final CopyOnWriteArrayList<Runnable> qTasks = new CopyOnWriteArrayList<>();

    public Menu(String[] args) {
        QApplication.initialize(args);
        QApplication.setStyle("Fusion");

        initMainWindow();
        initTabs();

        QMetaObject.invokeMethod(this::tick);

        QApplication.exec();
    }

    private void tick() throws InterruptedException {
        Thread.sleep(1);
        synchronized (qTasks) {
            if(qTasks.size() != 0) {
                Runnable task = qTasks.get(0);

                if (task != null) {
                    task.run();
                    qTasks.remove(task);
                }
            }
        }
        QMetaObject.invokeMethod(this::tick, Qt.ConnectionType.QueuedConnection);
    }

    public static void qRunLater(Runnable runnable) {
        synchronized (qTasks) {
            qTasks.add(runnable);
        }
    }

    /* ------------------------- Initialization ------------------------- */

    private void initMainWindow() {
        mainWindow = new QMainWindow();
        mainWindow.resize((int) (QApplication.screens().get(0).size().width() / 2.1),
                (int) (QApplication.screens().get(0).size().height() / 2.3));
        mainWindow.setWindowTitle("Desmos - Visualizer & Exploit");

        try {
            mainWindow.setWindowIcon(new QIcon(Paths.get(
                    Desmos.class.getResource("icon.png").toURI()).toFile().getPath()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        mainWindow.show();
    }

    private void initTabs() {
        tabWidget = new QTabWidget();
        mainWindow.setCentralWidget(tabWidget);

        gridTab = new GridTab();
        tabWidget.addTab(gridTab, "Grid");
    }

    /* ------------------------- Getters and Setters ------------------------- */

    public QMainWindow getMainWindow() {
        return mainWindow;
    }

    public QTabWidget getTabWidget() {
        return tabWidget;
    }
}
