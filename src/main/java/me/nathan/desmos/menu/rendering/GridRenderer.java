package me.nathan.desmos.menu.rendering;

import io.qt.core.QPoint;
import io.qt.core.QPointF;
import io.qt.core.QRectF;
import io.qt.core.Qt;
import io.qt.gui.*;
import io.qt.widgets.*;

import me.nathan.desmos.Desmos;
import me.nathan.desmos.ingame.account.Account;
import me.nathan.desmos.ingame.util.ChunkPosition;
import me.nathan.desmos.ingame.util.ChunkState;
import me.nathan.desmos.ingame.util.Dimension;
import me.nathan.desmos.menu.util.Point;
import me.nathan.desmos.util.MathUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A custom widget, subclassed of QGraphicsView, which displays a coordinate grid.
 * This is used for exploit visualization and querying.
 *
 * Credit to Node3112 for help & code
 */
public class GridRenderer extends QGraphicsView {

    public final QGraphicsScene graphicsScene = new QGraphicsScene(this);

    private final float[] scale = {1, 1};

    private int[] previousMousePosition;

    private boolean selecting;
    private boolean gripping;

    private final List<QGraphicsLineItem> xGridLines = new ArrayList<>();
    private final List<QGraphicsLineItem> zGridLines = new ArrayList<>();

    private final ConcurrentHashMap<ChunkState, QGraphicsRectItem> selectedChunks = new ConcurrentHashMap<>();

    private final HashMap<ChunkState, QGraphicsRectItem> overworldChunks = new HashMap<>();
    private final HashMap<ChunkState, QGraphicsRectItem> netherChunks = new HashMap<>();
    private final HashMap<ChunkState, QGraphicsRectItem> endChunks = new HashMap<>();

    private final HashMap<Point, QGraphicsEllipseItem> overworldPoints = new HashMap<>();
    private final HashMap<Point, QGraphicsEllipseItem> netherPoints = new HashMap<>();
    private final HashMap<Point, QGraphicsEllipseItem> endPoints = new HashMap<>();

    private QGraphicsTextItem xPlusHighwayText;
    private QGraphicsTextItem xMinusHighwayText;
    private QGraphicsTextItem zPlusHighwayText;
    private QGraphicsTextItem zMinusHighwayText;

    private QGraphicsTextItem positionText;

    private Point hoveringPoint;

    private final QGraphicsRectItem tooltipBackground = new QGraphicsRectItem();
    private final QGraphicsPixmapItem tooltipAvatar = new QGraphicsPixmapItem();

    private final QGraphicsTextItem tooltipUpperText = new QGraphicsTextItem("");
    private final QGraphicsTextItem tooltipLowerText = new QGraphicsTextItem("");

    private Dimension currentDimension = Dimension.OVERWORLD;

    private final int HIGHWAY_LINES_Z = 6;
    private final int GRID_LINES_Z = 3;

    private final int HIGHWAY_TEXT_Z = 8;
    private final int POSITION_TEXT_Z = 8;

    private final int SELECTION_CHUNK_Z = 5;
    private final int LOADED_CHUNK_Z = 4;
    private final int UNLOADED_CHUNK_Z = 4;

    private final int POINT_Z = 7;

    private final int TOOLTIP_Z = 10;
    private final int TOOLTIP_BASE_Z = 9;

    public GridRenderer(QWidget parent) {
        super(parent);

        this.setScene(graphicsScene);

        this.setMouseTracking(true);

        this.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
        this.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);

        graphicsScene.setSceneRect(-30000000, -30000000, 60000000, 60000000);
        this.setSceneRect(-(width() / 2.0), -(height() / 2.0), width(), height());

        initHighwayLines();
        initHighwayText();
        initGridLines();
        initPositionText();

        updateComponents();
    }

    /* ------------------------- Initialization ------------------------- */

    private void initPositionText() {
        QFont posFont = new QFont();
        posFont.setBold(true);

        positionText = graphicsScene.addText("Position: 0, 0 (0, 0)", posFont);
        positionText.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
        positionText.setZValue(POSITION_TEXT_Z);
    }

    private void initGridLines() {
        QPen gridPen = new QPen(new QColor(181, 181, 181));
        gridPen.setCosmetic(true);
        gridPen.setWidth(1);

        for(int i = -256; i < 256; ++i) {
            QGraphicsLineItem newXLine = graphicsScene.addLine(
                    i * 16, -4096, i * 16, 4096, gridPen);
            xGridLines.add(newXLine);

            QGraphicsLineItem newZLine = graphicsScene.addLine(
                    -4096, i * 16, 4096, i * 16, gridPen);
            zGridLines.add(newZLine);
        }
    }

    private void initHighwayLines() {
        QPen highwayPen = new QPen(new QColor(115, 115, 115));
        highwayPen.setCosmetic(true);
        highwayPen.setWidth(5);

        QGraphicsLineItem xHighway = graphicsScene.addLine(0, -30000000, 0, 30000000, highwayPen);
        QGraphicsLineItem zHighway = graphicsScene.addLine(-30000000, 0, 30000000, 0, highwayPen);

        xHighway.setZValue(HIGHWAY_LINES_Z);
        zHighway.setZValue(HIGHWAY_LINES_Z);
    }

    private void initHighwayText() {
        QFont highwayFont = new QFont();
        highwayFont.setPixelSize(11);
        highwayFont.setBold(true);

        xPlusHighwayText = graphicsScene.addText("+X", highwayFont);
        xPlusHighwayText.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
        xPlusHighwayText.setZValue(HIGHWAY_TEXT_Z);

        xMinusHighwayText = graphicsScene.addText("-X", highwayFont);
        xMinusHighwayText.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
        xMinusHighwayText.setZValue(HIGHWAY_TEXT_Z);

        zPlusHighwayText = graphicsScene.addText("+Z", highwayFont);
        zPlusHighwayText.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
        zPlusHighwayText.setZValue(HIGHWAY_TEXT_Z);

        zMinusHighwayText = graphicsScene.addText("-Z", highwayFont);
        zMinusHighwayText.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
        zMinusHighwayText.setZValue(HIGHWAY_TEXT_Z);
    }

    /* ------------------------- Updates ------------------------- */

    public void updateComponents() {
        updateHighwayText();
        updatePositionText();
        updateGridLines();
        updatePointPositions();
        updateTooltip();
    }

    private void updatePositionText() {
        positionText.setPos(mapToScene(new QPoint(4, height() - fontMetrics().height() - 8)));

        QPointF mousePos = mapToScene(mapFromGlobal(QCursor.pos()));

        StringBuilder posBuilder = new StringBuilder();
        posBuilder.append("Position: ");

        switch (currentDimension) {
            case OVERWORLD: {
                posBuilder.append(String.format("%.0f",mousePos.x()) + ", ");
                posBuilder.append(String.format("%.0f",-mousePos.y()) + " ");

                posBuilder.append("(" + String.format("%.0f", mousePos.x() / 8));
                posBuilder.append(" " + String.format("%.0f", -mousePos.y() / 8) + ")");
                break;
            }
            case NETHER: {
                posBuilder.append(String.format("%.0f",mousePos.x()) + ", ");
                posBuilder.append(String.format("%.0f",-mousePos.y()) + " ");

                posBuilder.append("(" + String.format("%.0f", mousePos.x() * 8));
                posBuilder.append(" " + String.format("%.0f", -mousePos.y() * 8) + ")");
                break;
            }
            case END: {
                posBuilder.append(String.format("%.0f",mousePos.x()) + ", ");
                posBuilder.append(String.format("%.0f",-mousePos.y()));
                break;
            }
        }

        positionText.setPlainText(posBuilder.toString());
    }

    private void updateHighwayText() {
        QPointF maxPos = mapToScene(width(), height());
        QPointF minPos = mapToScene(new QPoint(0, 0));

        xPlusHighwayText.setPos(maxPos.x() - xPlusHighwayText.boundingRect().width() / scale[0], 0);
        xPlusHighwayText.setVisible(maxPos.x() >= 0);

        xMinusHighwayText.setPos(minPos.x(), 0);
        xMinusHighwayText.setVisible(minPos.x() <= 0);

        zPlusHighwayText.setPos(0, minPos.y());
        zPlusHighwayText.setVisible(minPos.y() <= 0);

        zMinusHighwayText.setPos(0, maxPos.y() - zMinusHighwayText.boundingRect().height() / scale[1]);
        zMinusHighwayText.setVisible(maxPos.y() >= 0);
    }

    private void updateGridLines() {
        QPointF minPos = this.mapToScene(new QPoint(0, 0));
        QPointF maxPos = this.mapToScene(new QPoint(this.width(), this.height()));

        double[] gridSize = new double[] {
                Math.max(Math.pow(2, (Math.floor((2 - MathUtil.log(scale[0], 2)) / 2) * 2)), 1) * 16,
                Math.max(Math.pow(2, (Math.floor((2 - MathUtil.log(scale[1], 2)) / 2) * 2)), 1) * 16,
        };

        double xLines = Math.ceil(this.width() / (this.scale[0] * gridSize[0]) + 1);
        double zLines = Math.ceil(this.height() / (this.scale[1] * gridSize[1]) + 1);

        double xDelta = (minPos.x() % gridSize[0]) / gridSize[0];
        double zDelta = (minPos.y() % gridSize[1]) / gridSize[1];

        /* FIXME: Implement grid invisibility past a certain scale better.
                  The Current Implementation might not work on all screens.
         */

        for(int i = 0; i < xGridLines.size(); ++i) {
            if(i < xLines && scale[0] > 1) {
                xGridLines.get(i).setVisible(true);

                xGridLines.get(i).setLine(
                        minPos.x() + (i - xDelta) * gridSize[0], minPos.y(),
                        minPos.x() + (i - xDelta) * gridSize[0], maxPos.y()
                );
            } else {
                xGridLines.get(i).setVisible(false);
            }
        }

        for(int i = 0; i < zGridLines.size(); ++i) {
            if(i < zLines && scale[1] > 1) {
                zGridLines.get(i).setVisible(true);
                zGridLines.get(i).setLine(
                        minPos.x(), minPos.y() + (i - zDelta) * gridSize[1],
                        maxPos.x(), minPos.y() + (i - zDelta) * gridSize[1]
                );
            } else {
                zGridLines.get(i).setVisible(false);
            }
        }
    }

    private void updateSelection(int oldX, int oldZ, int newX, int newZ) {
        QPen selectionPen = new QPen(new QColor(0, 0, 255));
        selectionPen.setCosmetic(true);
        selectionPen.setWidth(4);

        for(int[] chunk : MathUtil.bresenham(oldX, oldZ, newX, newZ)) {
            ChunkState chunkState = weirdToExpected(new ChunkState(chunk[0], chunk[1], currentDimension));
            if(!selectedChunks.containsKey(chunkState)) {
                QGraphicsRectItem rect = graphicsScene.addRect(chunk[0] * 16, chunk[1] * 16,
                        16, 16, selectionPen);
                rect.setZValue(SELECTION_CHUNK_Z);

                selectedChunks.put(chunkState , rect);
            }
        }
    }

    public void updateChunk(ChunkState state, boolean loaded) {
        QPen loadedPen = new QPen(new QColor(45, 196, 60));
        loadedPen.setCosmetic(true);
        loadedPen.setWidth(4);

        QPen unloadedPen = new QPen(new QColor(191, 54, 54));
        unloadedPen.setCosmetic(true);
        unloadedPen.setWidth(4);

        QBrush loadedBrush = new QBrush(new QColor(87, 247, 103));
        QBrush unloadedBrush = new QBrush(new QColor(245, 78, 78));

        if(getChunksForDimension(state.getDimension()).containsKey(state)) {
            if(loaded) {
                getChunksForDimension(state.getDimension()).get(state).setBrush(loadedBrush);
                getChunksForDimension(state.getDimension()).get(state).setPen(loadedPen);
            } else {
                getChunksForDimension(state.getDimension()).get(state).setBrush(unloadedBrush);
                getChunksForDimension(state.getDimension()).get(state).setPen(unloadedPen);
            }
        } else {
            int renderX = expectedToGrid(state)[0];
            int renderZ = expectedToGrid(state)[1];

            QGraphicsRectItem rect = new QGraphicsRectItem(renderX, renderZ,16, 16);
            if(currentDimension == state.getDimension()) {
                graphicsScene.addItem(rect);
            }

            if(loaded) {
                rect.setBrush(loadedBrush);
                rect.setPen(loadedPen);
                rect.setZValue(LOADED_CHUNK_Z);
            } else {
                rect.setBrush(unloadedBrush);
                rect.setPen(unloadedPen);
                rect.setZValue(UNLOADED_CHUNK_Z);
            }

            getChunksForDimension(state.getDimension()).put(state, rect);
        }
    }

    public void updatePoint(Point point) {
        QPen pointPen = new QPen(new QColor(0, 0, 0));
        pointPen.setCosmetic(true);
        pointPen.setWidth(3);

        QBrush accountBrush = new QBrush(new QColor(193, 83, 237));
        QBrush playerBrush = new QBrush(new QColor(209, 15, 15));
        QBrush possibleBaseBrush = new QBrush(new QColor(245, 163, 49));

        QGraphicsEllipseItem item;

        for(Point p : getPointsForDimension(point.getDimension()).keySet()) {
            if(p.getId() == point.getId()) {
                item = getPointsForDimension(point.getDimension()).get(p);
                item.setPen(pointPen);
                switch(point.getPointType()) {
                    case ACCOUNT: {
                        item.setBrush(accountBrush);
                        break;
                    }
                    case PLAYER: {
                        item.setBrush(playerBrush);
                        break;
                    }
                    case POSSIBLE_BASE: {
                        item.setBrush(possibleBaseBrush);
                        break;
                    }
                }
                return;
            }
        }

        float width = (12 / scale[0]);
        float height = (12 / scale[1]);
        item = new QGraphicsEllipseItem(expectedToGridPoint(new ChunkState(point.getChunkPosition().getX(), 0, null))[0] - width / 2,
                expectedToGridPoint(new ChunkState(0, point.getChunkPosition().getZ(), null))[1] - height / 2,
                width, height);
        item.setPen(pointPen);
        switch(point.getPointType()) {
            case ACCOUNT: {
                item.setBrush(accountBrush);
                break;
            }
            case PLAYER: {
                item.setBrush(playerBrush);
                break;
            }
            case POSSIBLE_BASE: {
                item.setBrush(possibleBaseBrush);
                break;
            }
        }
        item.setZValue(POINT_Z);
        if(currentDimension == point.getDimension()) {
            graphicsScene.addItem(item);
        }
        getPointsForDimension(point.getDimension()).put(point, item);
    }

    public void removePoint(int id) {
        Point point = getPoint(id);
        if(point == null) return;

        graphicsScene.removeItem(getPointEllipse(point));
        getPointsForDimension(point.getDimension()).remove(point);
    }

    private void updatePointPositions() {
        Map<Point, QGraphicsEllipseItem> collectivePoints = new HashMap<>();
        collectivePoints.putAll(overworldPoints);
        collectivePoints.putAll(netherPoints);
        collectivePoints.putAll(endPoints);

        for(Map.Entry<Point, QGraphicsEllipseItem> entry : collectivePoints.entrySet()) {
            float width = (12 / scale[0]);
            float height = (12 / scale[1]);

            entry.getValue().setRect(expectedToGridPoint(new ChunkState(entry.getKey().getChunkPosition().getX(), 0, null))[0] - width / 2,
                    expectedToGridPoint(new ChunkState(0, entry.getKey().getChunkPosition().getZ(), null))[1] - height / 2,
                    width, height);
        }
    }

    private void updateTooltip() {
        if(hoveringPoint != null) {
            if(!graphicsScene.items().contains(tooltipBackground)) {
                QPen tooltipBackgroundPen = new QPen();
                tooltipBackgroundPen.setWidth(2);
                QBrush tooltipBackgroundBrush = new QBrush(new QColor(245, 245, 245));
                tooltipBackground.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
                tooltipBackground.setZValue(TOOLTIP_BASE_Z);
                tooltipBackground.setPen(tooltipBackgroundPen);
                tooltipBackground.setBrush(tooltipBackgroundBrush);

                tooltipAvatar.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
                tooltipAvatar.setZValue(TOOLTIP_Z);

                tooltipUpperText.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
                tooltipUpperText.setZValue(TOOLTIP_Z);

                tooltipLowerText.setFlag(QGraphicsItem.GraphicsItemFlag.ItemIgnoresTransformations);
                tooltipLowerText.setZValue(TOOLTIP_Z);

                switch(hoveringPoint.getPointType()) {
                    case ACCOUNT: {
                        Account account = (Account) hoveringPoint.getParent();
                        tooltipUpperText.setPlainText("Name: " + account.getUsername());
                        tooltipLowerText.setPlainText("ID: " + account.getId());

                        tooltipAvatar.setPixmap(new QPixmap(
                                Desmos.fileManger.getAvatarFromCache(account.getUsername()).getPath()));

                        break;
                    }

                    case PLAYER: {

                        break;
                    }

                    case POSSIBLE_BASE: {

                        break;
                    }
                }
                graphicsScene.addItem(tooltipBackground);
                graphicsScene.addItem(tooltipAvatar);
                graphicsScene.addItem(tooltipUpperText);
                graphicsScene.addItem(tooltipLowerText);
            }

            int tooltipAvatarHeight = (int) (180 * (height() * .0006));
            int tooltipAvatarWidth = (int) (180 * (height() * .0006));

            tooltipAvatar.setScale(height() * .0006);
            tooltipAvatar.setPos(mapToScene(5, ((int)((height() * 0.13) / 2) - tooltipAvatarHeight / 2) + 1));

            QFont font = new QFont();
            font.setPixelSize((int) (13 * (height() * 0.003)));

            tooltipUpperText.setFont(font);
            tooltipLowerText.setFont(font);

            tooltipUpperText.setPos(mapToScene(tooltipAvatarWidth + 5,
                    (int) (((int) ((height() * 0.13) / 4.5)) - new QFontMetricsF(tooltipUpperText.font()).height() / 2)));
            tooltipLowerText.setPos(mapToScene(tooltipAvatarWidth + 5,
                    (int) (((int) ((height() * 0.13) * 0.66)) - new QFontMetricsF(tooltipUpperText.font()).height() / 2)));

            tooltipBackground.setRect(1, 1,
                    tooltipAvatarWidth + 5 + Math.max(tooltipUpperText.boundingRect().width(),
                            tooltipLowerText.boundingRect().width()),
                    height() * 0.13);
            tooltipBackground.setPos(mapToScene(1, 1));
        } else {
            if(graphicsScene.items().contains(tooltipBackground)) {
                graphicsScene.removeItem(tooltipBackground);
                graphicsScene.removeItem(tooltipAvatar);
                graphicsScene.removeItem(tooltipUpperText);
                graphicsScene.removeItem(tooltipLowerText);
            }
        }
    }

    /* ------------------------- Events ------------------------- */

    @Override
    public void keyPressEvent(QKeyEvent event) {
        if(event.key() == Qt.Key.Key_Shift.value()) {
            selecting = true;
        }
    }

    @Override
    public void keyReleaseEvent(QKeyEvent event) {
        if(event.key() == Qt.Key.Key_Shift.value()) {
            selecting = false;
        }
    }

    @Override
    public void resizeEvent(QResizeEvent event) {
        previousMousePosition = null;

        updateComponents();
    }

    @Override
    public void mousePressEvent(QMouseEvent event) {
        previousMousePosition = null;

        if(!selecting) {
            QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.ClosedHandCursor));
        } else {
            QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.CrossCursor));
        }

        gripping = true;
    }

    public void mouseReleaseEvent(QMouseEvent event) {
        QApplication.restoreOverrideCursor();

        previousMousePosition = null;

        gripping = false;
    }

    @Override
    public void mouseMoveEvent(QMouseEvent event) {
        if(previousMousePosition != null && gripping) {
            if(!selecting) {
                this.setSceneRect(this.sceneRect().translated(
                        this.mapToScene(previousMousePosition[0], previousMousePosition[1])
                                .subtract(this.mapToScene((int) event.position().x(), (int) event.position().y()))));
            } else {
                QPointF oldPosition = mapToScene(previousMousePosition[0], previousMousePosition[1]);
                QPointF newPosition = mapToScene(event.pos());

                updateSelection(
                        (int) Math.floor(oldPosition.x() / 16), (int) Math.floor(oldPosition.y() / 16),
                        (int) Math.floor(newPosition.x() / 16), (int) Math.floor(newPosition.y() / 16)
                );
            }
        }
        hoveringPoint = null;

        Map<Point, QGraphicsEllipseItem> collectivePoints = new HashMap<>();
        collectivePoints.putAll(overworldPoints);
        collectivePoints.putAll(netherPoints);
        collectivePoints.putAll(endPoints);

        for(Map.Entry<Point, QGraphicsEllipseItem> entry : collectivePoints.entrySet()) {
            if(entry.getValue().isUnderMouse()) {
                hoveringPoint = entry.getKey();
            }
        }

        previousMousePosition = new int[] {
                (int) event.position().x(),
                (int) event.position().y()
        };
        updateComponents();
    }

    @Override
    public void wheelEvent(QWheelEvent event) {
       QPointF previousPosition = this.mapToScene(event.position().toPoint());
       float[] newScale = new float[] {
               (float) Math.exp(event.angleDelta().y() / 1000.0),
               (float) Math.exp(event.angleDelta().y() / 1000.0),
       };
       scale[0] = newScale[0] * scale[0];
       scale[1] = newScale[1] * scale[1];

       this.scale(newScale[0], newScale[1]);

       QPointF currentPosition = this.mapToScene(event.position().toPoint());

       float delta = (float) (previousPosition.y() - currentPosition.y());
       this.setSceneRect(this.sceneRect().translated(delta, delta));

       updateComponents();
    }

    /* ------------------------- Positional Utility ------------------------- */

    private int[] expectedToGrid(ChunkState pos) {
        return new int[] {
                (int) Math.floor(pos.getX() < 0 ? pos.getX() : pos.getX() - 1) * 16,
                (int) Math.floor(-pos.getZ() > 0 ? -pos.getZ() - 1 : -pos.getZ()) * 16
        };
    }

    private float[] expectedToGridPoint(ChunkState pos) {
        return new float[] {
                (float) ((pos.getX() < 0 ? pos.getX() + 0.5 : pos.getX() - 0.5) * 16f),
                (float) ((-pos.getZ() > 0 ? -pos.getZ() + 0.5: -pos.getZ() - 0.5) * 16f)
        };
    }

    private ChunkState weirdToExpected(ChunkState chunkState) {
        return new ChunkState(
                chunkState.getX() >= 0 ? chunkState.getX() + 1 : chunkState.getX(),
                chunkState.getZ() < 0 ? -chunkState.getZ() : -chunkState.getZ() - 1,
                chunkState.getDimension()
        );
    }

    /* ------------------------- Setters and Getters ------------------------- */

    public Dimension getCurrentDimension() {
        return currentDimension;
    }

    public void setCurrentDimension(Dimension dimension) {
        currentDimension = dimension;
    }

    public ConcurrentHashMap<ChunkState, QGraphicsRectItem> getSelectedChunks() {
        return selectedChunks;
    }

    public HashMap<ChunkState, QGraphicsRectItem> getChunksForDimension(Dimension dimension) {
        switch (dimension) {
            case OVERWORLD: return overworldChunks;
            case NETHER: return netherChunks;
            case END: return endChunks;
        }
        return null;
    }

    public Point getPoint(int id) {
        Map<Point, QGraphicsEllipseItem> collectivePoints = new HashMap<>();
        collectivePoints.putAll(overworldPoints);
        collectivePoints.putAll(netherPoints);
        collectivePoints.putAll(endPoints);

        for(Map.Entry<Point, QGraphicsEllipseItem> entry : collectivePoints.entrySet()) {
            if(id == entry.getKey().getId()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public QGraphicsEllipseItem getPointEllipse(Point point) {
        HashMap<Point, QGraphicsEllipseItem> collectivePoints = new HashMap<>();
        collectivePoints.putAll(overworldPoints);
        collectivePoints.putAll(netherPoints);
        collectivePoints.putAll(endPoints);

        for(Map.Entry<Point, QGraphicsEllipseItem> entry : collectivePoints.entrySet()) {
            if(point.getId() == entry.getKey().getId()) {
                return entry.getValue();
            }
        }

        return null;
    }

    public HashMap<Point, QGraphicsEllipseItem> getPointsForDimension(Dimension dimension) {
        switch (dimension) {
            case OVERWORLD: return overworldPoints;
            case NETHER: return netherPoints;
            case END: return endPoints;
        }
        return null;
    }

    public float[] getScale() {
        return scale;
    }

    public void setScale(float x, float y) {
        scale[0] = x;
        scale[1] = y;
    }
}
