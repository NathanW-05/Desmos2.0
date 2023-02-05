package me.nathan.desmos.menu.tabs;

import io.qt.core.QPointF;
import io.qt.core.Qt;
import io.qt.gui.QIcon;
import io.qt.gui.QPixmap;
import io.qt.widgets.*;

import me.nathan.desmos.Desmos;
import me.nathan.desmos.ingame.query.Query;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.menu.Tab;
import me.nathan.desmos.menu.rendering.GridRenderer;
import me.nathan.desmos.ingame.util.ChunkState;
import me.nathan.desmos.ingame.util.Dimension;

import java.nio.file.Paths;
import java.util.Map;

public class GridTab extends Tab {

    private final QVBoxLayout layout;
    private final QHBoxLayout bottomLayout;

    private QComboBox dimensionCombo;

    private GridRenderer gridRenderer;

    private QLineEdit xEdit;
    private QLineEdit zEdit;

    public GridTab() {
        layout = new QVBoxLayout(this);
        initGrid();

        bottomLayout = new QHBoxLayout(this);
        bottomLayout.setAlignment(Qt.AlignmentFlag.AlignLeft);

        layout.addLayout(bottomLayout);

        initQueryButton();
        initClearButton();
        initGotoButton();
        initDimensionCombo();
    }

    /* ------------------------- Events ------------------------- */

    private void queryButtonClickedEvent() {
        for(Map.Entry<ChunkState, QGraphicsRectItem> entry : gridRenderer.getSelectedChunks().entrySet()) {
            if(entry.getKey().getDimension() == gridRenderer.getCurrentDimension()) {
                Query query = new Query(new ChunkPosition(entry.getKey().getX(), entry.getKey().getZ()),
                        entry.getKey().getDimension(), Query.Priority.USER, true) {
                    @Override
                    public void callback(boolean result) {
                        super.callback(result);
                    }
                };
                Desmos.queryHandler.scheduleQuery(query);

                gridRenderer.getSelectedChunks().remove(entry.getKey());
                gridRenderer.graphicsScene.removeItem(entry.getValue());
            }
        }
        gridRenderer.updateComponents();
    }

    private void clearButtonClickedEvent() {
        for(Map.Entry<ChunkState, QGraphicsRectItem> entry : gridRenderer.getSelectedChunks().entrySet()) {
            if(entry.getKey().getDimension() == gridRenderer.getCurrentDimension()) {
                gridRenderer.graphicsScene.removeItem(entry.getValue());
                gridRenderer.getSelectedChunks().remove(entry.getKey());
            }
        }

        gridRenderer.updateComponents();
    }

    private void gotoButtonClickedEvent() {
        QPointF center = gridRenderer.mapToScene(Math.floorDiv(gridRenderer.width(), 2),
                Math.floorDiv(gridRenderer.height(), 2));

        try {
            gridRenderer.setSceneRect(gridRenderer.sceneRect().translated(
                    Math.abs(center.x()) == center.x() ? -center.x() + Double.parseDouble(xEdit.text()) :
                            Math.abs(center.x()) + Double.parseDouble(xEdit.text()),
                    Math.abs(center.y()) == center.y() ? -center.y() - Double.parseDouble(zEdit.text()) :
                            Math.abs(center.y()) - Double.parseDouble(zEdit.text())));

            xEdit.clear();
            zEdit.clear();
            gridRenderer.updateComponents();

        } catch (Exception e) {
            QMessageBox.critical(this, "Goto Button", "Please enter valid coordinates!");
        }
    }

    private void dimensionComboChangeEvent(String newText) {
        for(QGraphicsItem item : gridRenderer.graphicsScene.items()) {
            if(item instanceof QGraphicsRectItem || item instanceof QGraphicsEllipseItem) {
                gridRenderer.graphicsScene.removeItem(item);
            }
        }

       switch (newText) {
           case "Overworld": {
               gridRenderer.setCurrentDimension(Dimension.OVERWORLD);

               gridRenderer.getChunksForDimension(Dimension.OVERWORLD).values().forEach(item ->
                       gridRenderer.graphicsScene.addItem(item));
               gridRenderer.getPointsForDimension(Dimension.OVERWORLD).values().forEach(item ->
                       gridRenderer.graphicsScene.addItem(item));
               break;
           }
           case "Nether": {
               gridRenderer.setCurrentDimension(Dimension.NETHER);

               gridRenderer.getChunksForDimension(Dimension.NETHER).values().forEach(item ->
                       gridRenderer.graphicsScene.addItem(item));
               gridRenderer.getPointsForDimension(Dimension.NETHER).values().forEach(item ->
                       gridRenderer.graphicsScene.addItem(item));
               break;
           }
           case "End": {
               gridRenderer.setCurrentDimension(Dimension.END);

               gridRenderer.getChunksForDimension(Dimension.END).values().forEach(item ->
                       gridRenderer.graphicsScene.addItem(item));
               gridRenderer.getPointsForDimension(Dimension.END).values().forEach(item ->
                       gridRenderer.graphicsScene.addItem(item));
               break;
           }
           default: break;
       }

       for(Map.Entry<ChunkState, QGraphicsRectItem> entry : gridRenderer.getSelectedChunks().entrySet()) {
           if(entry.getKey().getDimension() == gridRenderer.getCurrentDimension()) {
               if(!gridRenderer.graphicsScene.items().contains(entry.getValue())) {
                   gridRenderer.graphicsScene.addItem(entry.getValue());
               }
           }
       }

        gridRenderer.updateComponents();
    }

    /* ------------------------- Initialization ------------------------- */

    private void initGrid() {
        gridRenderer = new GridRenderer(this);
        layout.addWidget(gridRenderer);
    }

    private void initQueryButton() {
        QPushButton queryButton = new QPushButton("Query Selection", this);
        queryButton.clicked.connect(this::queryButtonClickedEvent);
        bottomLayout.addWidget(queryButton);
    }

    private void initClearButton() {
        QPushButton clearButton = new QPushButton("Clear Selection", this);
        clearButton.clicked.connect(this::clearButtonClickedEvent);
        bottomLayout.addWidget(clearButton);
    }

    private void initGotoButton() {
        QPushButton gotoButton = new QPushButton("Goto", this);
        gotoButton.clicked.connect(this::gotoButtonClickedEvent);

        bottomLayout.addWidget(gotoButton);

        xEdit = new QLineEdit(this);
        zEdit = new QLineEdit(this);

        QLabel xLabel = new QLabel("X:", this);
        QLabel zLabel = new QLabel("Z:", this);

        xEdit.setMaximumWidth(80);
        zEdit.setMaximumWidth(80);

        bottomLayout.addWidget(xLabel);
        bottomLayout.addWidget(xEdit);
        bottomLayout.addWidget(zLabel);
        bottomLayout.addWidget(zEdit);

        QSpacerItem qSpacerItem = new QSpacerItem(50, 0, QSizePolicy.Policy.Expanding);
        bottomLayout.addItem(qSpacerItem);
    }

    private void initDimensionCombo() {
        QLabel dimensionLabel = new QLabel("Dimension:", this);
        dimensionCombo = new QComboBox(this);

        try {
            dimensionCombo.addItem(new QIcon(new QPixmap(Paths.get(
                    Desmos.class.getResource("grass.png").toURI()).toFile().getPath())), "Overworld");
            dimensionCombo.addItem(new QIcon(new QPixmap(Paths.get(
                    Desmos.class.getResource("netherrack.png").toURI()).toFile().getPath())), "Nether");
            dimensionCombo.addItem(new QIcon(new QPixmap(Paths.get(
                    Desmos.class.getResource("end_stone.png").toURI()).toFile().getPath())), "End");
        } catch (Exception e) {
            e.printStackTrace();
        }

        dimensionCombo.currentTextChanged.connect(this::dimensionComboChangeEvent);

        bottomLayout.addWidget(dimensionLabel);
        bottomLayout.addWidget(dimensionCombo);
    }

    /* ------------------------- Getters and Setters ------------------------- */

    public QComboBox getDimensionCombo() {
        return dimensionCombo;
    }

    public GridRenderer getGridRenderer() {
        return gridRenderer;
    }
}
