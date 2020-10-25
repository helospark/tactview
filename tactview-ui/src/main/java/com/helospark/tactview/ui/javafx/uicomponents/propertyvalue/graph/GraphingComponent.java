package com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraph;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.EffectGraphAccessor;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.ConnectionIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.GraphIndex;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.GraphElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.InputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.OutputElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.StatelessEffectElement;
import com.helospark.tactview.core.timeline.effect.interpolation.graph.domain.types.VisualTimelineClipElement;
import com.helospark.tactview.core.timeline.effect.interpolation.provider.GraphProvider;
import com.helospark.tactview.core.timeline.message.AbstractKeyframeChangedMessage;
import com.helospark.tactview.core.util.messaging.MessagingService;
import com.helospark.tactview.ui.javafx.UiCommandInterpreterService;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command.GraphAddConnectionCommand;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.command.GraphAddNewNodeByUriCommand;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory.GraphingComponentFactory;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.graph.factory.GraphingMenuItemRequest;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.message.OpenClipPropertyPageMessage;
import com.helospark.tactview.ui.javafx.uicomponents.propertyvalue.message.OpenEffectPropertyPageMessage;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Window;

public class GraphingComponent extends BorderPane {
    private static final double GRAPH_HEADER_HEIGHT = 20;
    private static final double GRAPH_HEADER_WIDTH = 100;
    private static final double GRAPH_CONNECTION_POINT_HEIGHT = 20;
    private static final double GRAPH_CONNECTION_POINT_RADIUS = 10;
    private static final double TEXT_HEIGHT_OFFSET = 5;

    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private GraphProvider graphProvider = null;

    private GraphDragTarget graphDragTarget = null;

    private Map<ConnectionIndex, GraphCoordinate> connectionCoordinateCache = new HashMap<>();
    private Map<GraphIndex, GraphInformation> graphCoordinateCache = new HashMap<>();

    double cameraPositionX, cameraPositionY;
    double zoom = 1.0;

    private double mouseClickX, mouseClickY;

    Window parent;

    private MessagingService messagingService;
    private UiCommandInterpreterService commandInterpreter;

    public GraphingComponent(double width, double height, EffectGraphAccessor effectGraphAccessor, MessagingService messagingService, List<GraphingComponentFactory> menuItemFactories,
            UiCommandInterpreterService commandInterpreter) {
        cameraPositionX = -width / 2.0;
        cameraPositionY = -height / 2.0;
        canvas = new Canvas(width, height);
        graphicsContext = canvas.getGraphicsContext2D();
        this.messagingService = messagingService;
        this.commandInterpreter = commandInterpreter;

        messagingService.register(AbstractKeyframeChangedMessage.class, message -> {
            this.redrawGraphProvider();
        });

        super.setCenter(canvas);

        canvas.setOnMousePressed(event -> {
            double mouseX = (event.getX() + cameraPositionX) / zoom;
            double mouseY = (event.getY() + cameraPositionY) / zoom;

            mouseClickX = mouseX;
            mouseClickY = mouseY;

            if (event.getButton().equals(MouseButton.PRIMARY)) {
                Optional<ConnectionIndex> connectionPointClicked = findConnectionPointAt(mouseX, mouseY);
                Optional<Entry<GraphIndex, GraphInformation>> graphPointClicked = findGraphNodeAt(mouseX, mouseY);

                if (connectionPointClicked.isPresent()) {
                    graphDragTarget = GraphDragTarget.fromConnectionPoint(connectionPointClicked.get());
                } else if (graphPointClicked.isPresent()) {
                    if (graphPointClicked.isPresent()) {
                        GraphIndex graphIndex = graphPointClicked.get().getKey();
                        GraphCoordinate graphCoordinate = graphPointClicked.get().getValue().coordinate;
                        graphDragTarget = GraphDragTarget.fromGraphPoint(graphIndex, mouseX - graphCoordinate.x, mouseY - graphCoordinate.y);
                    }
                } else {
                    graphDragTarget = null;
                }
            }
        });
        canvas.setOnContextMenuRequested(event -> {
            double mouseX = (event.getX() + cameraPositionX) / zoom;
            double mouseY = (event.getY() + cameraPositionY) / zoom;

            Map<ConnectionIndex, List<ConnectionIndex>> connections = graphProvider.getEffectGraph().getConnections();
            Map<GraphIndex, GraphElement> graphElements = graphProvider.getEffectGraph().getGraphElements();

            ContextMenu contextMenu = new javafx.scene.control.ContextMenu();

            Optional<ConnectionIndex> optionalConnectionPoint = findConnectionPointAt(mouseX, mouseY);
            Optional<Entry<GraphIndex, GraphInformation>> optionalGraphNode = findGraphNodeAt(mouseX, mouseY);

            if (optionalConnectionPoint.isPresent()) {
                MenuItem item = new MenuItem("Disconnect");
                item.setOnAction(e -> {
                    if (!isInput(optionalConnectionPoint.get())) {
                        connections.remove(optionalConnectionPoint.get());
                    } else {
                        for (var entry : connections.values()) {
                            entry.remove(optionalConnectionPoint.get());
                        }
                    }
                    redrawGraphProvider();
                });
                contextMenu.getItems().add(item);
            }

            if (optionalGraphNode.isPresent() && !(graphElements.get(optionalGraphNode.get().getKey()) instanceof OutputElement)) {
                MenuItem item = new MenuItem("Delete node");
                item.setOnAction(e -> {
                    GraphIndex graphKey = optionalGraphNode.get().getKey();
                    GraphElement removedElement = graphElements.remove(graphKey);

                    removedElement.inputs.keySet()
                            .stream()
                            .forEach(inputKey -> connections.values().forEach(a -> a.remove(inputKey)));
                    removedElement.outputs.keySet()
                            .stream()
                            .forEach(outputKey -> connections.remove(outputKey));
                    redrawGraphProvider();
                });
                contextMenu.getItems().add(item);
            }

            Map<String, List<MenuItem>> categoryToMenuItem = new LinkedHashMap<>();
            menuItemFactories.stream()
                    .forEach(factory -> {
                        String category = factory.getCategory();
                        MenuItem menuItem = factory.createMenuItem(new GraphingMenuItemRequest(graphProvider, mouseX, mouseY, () -> redrawGraphProvider()));

                        List<MenuItem> menuItems = categoryToMenuItem.get(category);
                        if (menuItems == null) {
                            menuItems = new ArrayList<>();
                        }
                        menuItems.add(menuItem);
                        categoryToMenuItem.put(category, menuItems);
                    });

            Menu insertMenu = new Menu("Insert");
            for (var entry : categoryToMenuItem.entrySet()) {
                Menu menu = new Menu(entry.getKey());

                for (var menuItem : entry.getValue()) {
                    menu.getItems().add(menuItem);
                }
                insertMenu.getItems().add(menu);
            }
            contextMenu.getItems().add(insertMenu);

            contextMenu.show(parent, event.getScreenX(), event.getScreenY());
        });

        canvas.setOnMouseReleased(event -> {
            double mouseX = (event.getX() + cameraPositionX) / zoom;
            double mouseY = (event.getY() + cameraPositionY) / zoom;

            Optional<ConnectionIndex> optionalConnectionPoint = findConnectionPointAt(mouseX, mouseY);

            if (optionalConnectionPoint.isPresent() && graphDragTarget != null && graphDragTarget.isDraggingConnectionPoint()) {
                ConnectionIndex endPointConnectionIndex = optionalConnectionPoint.get();

                boolean isStartPointInput = isInput(graphDragTarget.connectionIndex);
                boolean isEndPointInput = isInput(endPointConnectionIndex);

                if (isEndPointInput ^ isStartPointInput) {
                    ConnectionIndex endIndex = isStartPointInput ? graphDragTarget.connectionIndex : endPointConnectionIndex;
                    ConnectionIndex startIndex = isStartPointInput ? endPointConnectionIndex : graphDragTarget.connectionIndex;

                    commandInterpreter.sendWithResult(new GraphAddConnectionCommand(graphProvider, effectGraphAccessor, startIndex, endIndex));
                }

            }
            if (graphDragTarget != null && !graphDragTarget.draggedBefore && graphDragTarget.isDraggingGraph()) {
                System.out.println("Highlight");
                GraphInformation graphInformation = graphCoordinateCache.get(graphDragTarget.graphIndex);
                graphInformation.highlightListener.run();
            }

            graphDragTarget = null;

            redrawGraphProvider();
        });
        canvas.setOnScroll(event -> {
            double scrollDelta = event.getDeltaY();
            zoom += scrollDelta / 1000.0;

            if (zoom <= 0.1) {
                zoom = 0.1;
            }
            if (zoom >= 10) {
                zoom = 10;
            }

            redrawGraphProvider();
        });
        canvas.setOnMouseDragged(event -> {
            double mouseX = (event.getX() + cameraPositionX) / zoom;
            double mouseY = (event.getY() + cameraPositionY) / zoom;

            if (graphDragTarget != null) {
                onMouseDragged(mouseX, mouseY);
            } else {
                double rx = mouseX - mouseClickX;
                double ry = mouseY - mouseClickY;

                cameraPositionX -= rx * zoom;
                cameraPositionY -= ry * zoom;

                mouseClickX = (event.getX() + cameraPositionX) / zoom;
                mouseClickY = (event.getY() + cameraPositionY) / zoom;

                redrawGraphProvider();
            }

        });
        canvas.setOnDragEntered(e -> {
            String dragBoardContent = e.getDragboard().getString().strip();

            GraphAddNewNodeByUriCommand result = commandInterpreter.sendWithResult(new GraphAddNewNodeByUriCommand(graphProvider, effectGraphAccessor, dragBoardContent)).join();
            graphDragTarget = GraphDragTarget.fromGraphPoint(result.getGraphAddedNode(), 0, 0);
        });
        canvas.setOnDragOver(event -> {
            double mouseX = (event.getX() + cameraPositionX) / zoom;
            double mouseY = (event.getY() + cameraPositionY) / zoom;
            onMouseDragged(mouseX, mouseY);
        });

        redrawGraphProvider();
    }

    private boolean isInput(ConnectionIndex connectionIndex) {
        return graphProvider.getEffectGraph().getGraphElements().values()
                .stream()
                .filter(a -> a.inputs.containsKey(connectionIndex))
                .findFirst()
                .map(a -> true)
                .orElse(false);
    }

    private Optional<Entry<GraphIndex, GraphInformation>> findGraphNodeAt(double mouseX, double mouseY) {
        Optional<Entry<GraphIndex, GraphInformation>> graphPointClicked = graphCoordinateCache.entrySet()
                .stream()
                .filter(entry -> entry.getValue().coordinate.contains(mouseX, mouseY))
                .findFirst();
        return graphPointClicked;
    }

    private Optional<ConnectionIndex> findConnectionPointAt(double mouseX, double mouseY) {
        Optional<ConnectionIndex> connectionPointClicked = connectionCoordinateCache.entrySet()
                .stream()
                .filter(entry -> entry.getValue().contains(mouseX, mouseY))
                .map(a -> a.getKey())
                .findFirst();
        return connectionPointClicked;
    }

    private void onMouseDragged(double mouseX, double mouseY) {

        if (graphDragTarget != null) {
            graphDragTarget.currentX = mouseX;
            graphDragTarget.currentY = mouseY;
            graphDragTarget.draggedBefore = true;

            if (graphDragTarget.isDraggingGraph()) {
                GraphElement draggedNode = graphProvider.getEffectGraph().getGraphElements().get(graphDragTarget.graphIndex);
                draggedNode.x = mouseX - graphDragTarget.anchorX;
                draggedNode.y = mouseY - graphDragTarget.anchorY;
                redrawGraphProvider();
            } else if (graphDragTarget.isDraggingConnectionPoint()) {
                redrawGraphProvider();
            }
        }
    }

    public void redrawGraphProvider() {

        graphicsContext.setTransform(new Affine()); // reset transform
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        graphicsContext.translate(-cameraPositionX, -cameraPositionY);
        graphicsContext.scale(zoom, zoom);

        connectionCoordinateCache.clear();
        graphCoordinateCache.clear();

        if (graphProvider == null) {
            return;
        }

        EffectGraph effectGraph = graphProvider.getEffectGraph();

        for (var element : effectGraph.getGraphElements().entrySet()) {
            drawElement(element.getValue(), element.getKey());
        }
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.setStroke(Color.WHITE);
        for (var entry : effectGraph.getConnections().entrySet()) {
            GraphCoordinate startPosition = connectionCoordinateCache.get(entry.getKey());

            for (var endIndex : entry.getValue()) {
                GraphCoordinate endPosition = connectionCoordinateCache.get(endIndex);

                graphicsContext.strokeLine(startPosition.x + GRAPH_CONNECTION_POINT_RADIUS / 2, startPosition.y + GRAPH_CONNECTION_POINT_RADIUS / 2,
                        endPosition.x + GRAPH_CONNECTION_POINT_RADIUS / 2, endPosition.y + GRAPH_CONNECTION_POINT_RADIUS / 2);
            }
        }
        if (graphDragTarget != null && graphDragTarget.isDraggingConnectionPoint()) {
            GraphCoordinate draggedConnectionPoint = connectionCoordinateCache.get(graphDragTarget.connectionIndex);

            graphicsContext.strokeLine(draggedConnectionPoint.x + GRAPH_CONNECTION_POINT_RADIUS / 2, draggedConnectionPoint.y + GRAPH_CONNECTION_POINT_RADIUS / 2,
                    graphDragTarget.currentX, graphDragTarget.currentY);

        }
    }

    private void drawElement(GraphElement element, GraphIndex graphIndex) {
        double x = element.getX();
        double y = element.getY();
        List<ConnectionIndex> inputList = new ArrayList<>(element.getInputs().keySet());
        List<ConnectionIndex> outputList = new ArrayList<>(element.getOutputs().keySet());
        int maxConnections = Math.max(inputList.size(), outputList.size());
        double height = GRAPH_HEADER_HEIGHT + Math.max(inputList.size(), outputList.size()) * GRAPH_CONNECTION_POINT_HEIGHT;

        GraphElementUiInfo elementInfo = getElementInfo(element);

        graphicsContext.setStroke(Color.WHITE);
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.strokeRect(x, y, GRAPH_HEADER_WIDTH, height);
        graphicsContext.strokeRect(x, y, GRAPH_HEADER_WIDTH, GRAPH_HEADER_HEIGHT);
        graphicsContext.setFill(Color.GRAY);
        graphicsContext.fillText(elementInfo.name, x, y + GRAPH_HEADER_HEIGHT - TEXT_HEIGHT_OFFSET);

        graphCoordinateCache.put(graphIndex, new GraphInformation(new GraphCoordinate(x, y, GRAPH_HEADER_WIDTH, height), elementInfo.highlightListener));

        for (int i = 0; i < maxConnections; ++i) {
            double connectionTopY = y + GRAPH_HEADER_HEIGHT + i * GRAPH_CONNECTION_POINT_HEIGHT;
            graphicsContext.setFill(Color.WHITE);
            graphicsContext.strokeRect(x, connectionTopY, GRAPH_HEADER_WIDTH, GRAPH_CONNECTION_POINT_HEIGHT);

            if (i < inputList.size()) {
                ConnectionIndex connectionIndex = inputList.get(i);
                double topLeftX = x - GRAPH_CONNECTION_POINT_RADIUS / 2;
                double topLeftY = connectionTopY + GRAPH_CONNECTION_POINT_RADIUS / 2;
                graphicsContext.setFill(Color.WHITE);
                graphicsContext.fillOval(topLeftX, topLeftY, GRAPH_CONNECTION_POINT_RADIUS, GRAPH_CONNECTION_POINT_RADIUS);
                graphicsContext.setFill(Color.GRAY);
                graphicsContext.fillText(element.getInputs().get(connectionIndex).getName(), x + GRAPH_CONNECTION_POINT_RADIUS, connectionTopY + GRAPH_CONNECTION_POINT_HEIGHT - TEXT_HEIGHT_OFFSET);

                connectionCoordinateCache.put(connectionIndex,
                        new GraphCoordinate(topLeftX, topLeftY, GRAPH_CONNECTION_POINT_RADIUS, GRAPH_CONNECTION_POINT_RADIUS));
            }
            if (i < outputList.size()) {
                ConnectionIndex connectionIndex = outputList.get(i);
                double topLeftX = x + GRAPH_HEADER_WIDTH - GRAPH_CONNECTION_POINT_RADIUS / 2;
                double topLeftY = connectionTopY + GRAPH_CONNECTION_POINT_RADIUS / 2;
                graphicsContext.setFill(Color.WHITE);
                graphicsContext.fillOval(topLeftX, topLeftY, GRAPH_CONNECTION_POINT_RADIUS, GRAPH_CONNECTION_POINT_RADIUS);
                graphicsContext.setFill(Color.GRAY);
                graphicsContext.fillText(element.getOutputs().get(connectionIndex).getName(), x + GRAPH_HEADER_WIDTH - 50, connectionTopY + GRAPH_CONNECTION_POINT_HEIGHT - TEXT_HEIGHT_OFFSET);

                connectionCoordinateCache.put(connectionIndex,
                        new GraphCoordinate(topLeftX, topLeftY, GRAPH_CONNECTION_POINT_RADIUS, GRAPH_CONNECTION_POINT_RADIUS));
            }
        }
    }

    private GraphElementUiInfo getElementInfo(GraphElement element) {
        if (element instanceof OutputElement) {
            return new GraphElementUiInfo("Output", () -> {
            });
        } else if (element instanceof InputElement) {
            return new GraphElementUiInfo("Input", () -> {
            });
        } else if (element instanceof StatelessEffectElement) {
            return new GraphElementUiInfo("Effect", () -> {
                messagingService.sendMessage(new OpenEffectPropertyPageMessage(((StatelessEffectElement) element).getEffect().getId()));
            });
        } else if (element instanceof VisualTimelineClipElement) {
            return new GraphElementUiInfo("Clip", () -> {
                messagingService.sendMessage(new OpenClipPropertyPageMessage(((VisualTimelineClipElement) element).getClip().getId()));
            });
        }
        return null;
    }

    static class GraphElementUiInfo {
        String name;
        Runnable highlightListener;

        public GraphElementUiInfo(String name, Runnable highlightListener) {
            this.name = name;
            this.highlightListener = highlightListener;
        }

    }

    static class GraphInformation {
        GraphCoordinate coordinate;
        Runnable highlightListener;

        public GraphInformation(GraphCoordinate coordinate, Runnable highlightListener) {
            this.coordinate = coordinate;
            this.highlightListener = highlightListener;
        }

    }

    static class GraphCoordinate {
        double x, y;
        double w, h;

        public GraphCoordinate(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseY >= y && mouseX <= x + w && mouseY <= y + h;
        }

        @Override
        public String toString() {
            return "GraphCoordinate [x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + "]";
        }

    }

    static class GraphDragTarget {
        GraphIndex graphIndex;
        ConnectionIndex connectionIndex;
        double anchorX, anchorY;

        double currentX, currentY;

        boolean draggedBefore = false;

        public GraphDragTarget(ConnectionIndex connectionIndex) {
            this.connectionIndex = connectionIndex;
            this.draggedBefore = false;
        }

        public GraphDragTarget(GraphIndex graphIndex2, double x, double y) {
            this.graphIndex = graphIndex2;
            this.anchorX = x;
            this.anchorY = y;
            this.draggedBefore = false;
        }

        boolean isDraggingGraph() {
            return graphIndex != null;
        }

        boolean isDraggingConnectionPoint() {
            return connectionIndex != null;
        }

        public static GraphDragTarget fromGraphPoint(GraphIndex graphIndex, double x, double y) {
            return new GraphDragTarget(graphIndex, x, y);
        }

        static GraphDragTarget fromConnectionPoint(ConnectionIndex connection) {
            return new GraphDragTarget(connection);
        }
    }

    public void setCameraPositionX(double cameraPositionX) {
        this.cameraPositionX = cameraPositionX;
    }

    public void setCameraPositionY(double cameraPositionY) {
        this.cameraPositionY = cameraPositionY;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setGraphProvider(GraphProvider graphProvider) {
        this.graphProvider = graphProvider;
    }

    public void setParent(Window parent) {
        this.parent = parent;
    }
}
