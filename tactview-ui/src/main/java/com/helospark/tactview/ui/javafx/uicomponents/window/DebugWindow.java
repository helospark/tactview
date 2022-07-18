package com.helospark.tactview.ui.javafx.uicomponents.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helospark.lightdi.annotation.Component;
import com.helospark.lightdi.annotation.Qualifier;
import com.helospark.tactview.core.decoder.framecache.MediaCache;
import com.helospark.tactview.core.decoder.framecache.MediaCache.CacheData;
import com.helospark.tactview.core.decoder.framecache.MemoryManagerImpl;
import com.helospark.tactview.ui.javafx.tabs.dockabletab.DockableTabRepository;
import com.helospark.tactview.ui.javafx.tabs.listener.TabCloseListener;
import com.helospark.tactview.ui.javafx.tabs.listener.TabOpenListener;
import com.helospark.tactview.ui.javafx.tiwulfx.com.panemu.tiwulfx.control.DetachableTab;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
public class DebugWindow extends DetachableTab implements TabOpenListener, TabCloseListener {
    public static final int DEFAULT_WIDTH = 700;
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugWindow.class);
    public static final String ID = "debug-window";
    private ScheduledExecutorService scheduledExecutorService;
    boolean isTabOpen = false;
    ScheduledFuture<?> job;
    MediaCache mediaCache;
    MemoryManagerImpl memoryManager;

    ObservableList<PieChart.Data> mediaCacheSizeData = FXCollections.observableArrayList(
            new PieChart.Data("Used", 0),
            new PieChart.Data("Free", 0));

    ObservableList<PieChart.Data> memoryManagerSizeData = FXCollections.observableArrayList(
            new PieChart.Data("MediaCache", 0),
            new PieChart.Data("Other", 0),
            new PieChart.Data("Free", 0));

    ObservableList<PieChart.Data> jvmMemoryData = FXCollections.observableArrayList(
            new PieChart.Data("Used", 0),
            new PieChart.Data("Free", 0));

    ObservableList<PieChart.Data> mediaCacheDistributionData = FXCollections.observableArrayList();
    ObservableList<CacheData> mediaCacheDistributionDataForTable = FXCollections.observableArrayList();

    XYChart.Series<Number, Number> mediaCacheUsedSeries = new XYChart.Series<Number, Number>();
    XYChart.Series<Number, Number> memoryManagerUsedSeries = new XYChart.Series<Number, Number>();
    XYChart.Series<Number, Number> jvmUsedSeries = new XYChart.Series<Number, Number>();

    Map<LineChart<Number, Number>, XYChart.Series<Number, Number>> lineChartToData = new HashMap<>();

    public DebugWindow(DockableTabRepository dockableTabRepository, @Qualifier("generalTaskScheduledService") ScheduledExecutorService scheduledExecutorService,
            MediaCache mediaCache, MemoryManagerImpl memoryManager) {
        super(ID);
        this.scheduledExecutorService = scheduledExecutorService;
        this.mediaCache = mediaCache;
        this.memoryManager = memoryManager;
    }

    protected void openTab() {
        ScrollPane scrollPane = new ScrollPane();

        VBox vbox = new VBox();
        vbox.setFillWidth(true);

        vbox.getChildren().add(new HBox(createPieChart(mediaCacheSizeData, "Media Cache"), createLineChart(mediaCacheUsedSeries, "MB")));
        vbox.getChildren().add(new HBox(createPieChart(memoryManagerSizeData, "Memory Manager"), createLineChart(memoryManagerUsedSeries, "MB")));
        vbox.getChildren().add(new HBox(createPieChart(jvmMemoryData, "JVM memory"), createLineChart(jvmUsedSeries, "MB")));
        PieChart cacheDistriubtion = createPieChart(mediaCacheDistributionData, "Cache distribution");
        cacheDistriubtion.setLegendVisible(false);
        vbox.getChildren().add(new HBox(cacheDistriubtion, createDistributionTableView()));

        scrollPane.setContent(vbox);

        this.setContent(scrollPane);
        this.setText("Debug");
    }

    private Node createDistributionTableView() {
        TableView<CacheData> tbv = new TableView<>();
        TableColumn cl1 = new TableColumn("Cache key");
        cl1.setCellValueFactory(new PropertyValueFactory<>("key"));
        cl1.setPrefWidth(630);

        TableColumn cl2 = new TableColumn("Size (MB)");
        cl2.setCellValueFactory(new PropertyValueFactory<>("dataSizeString"));
        cl2.setPrefWidth(100);

        tbv.getColumns().setAll(cl2, cl1);
        tbv.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        tbv.setItems(mediaCacheDistributionDataForTable);

        tbv.setPrefWidth(DEFAULT_WIDTH - 250);

        return tbv;
    }

    private void clearChart(Series<Number, Number> mediaCacheUsedSeries) {
        mediaCacheUsedSeries.getData().clear();
        for (int i = 0; i < 20; ++i) {
            mediaCacheUsedSeries.getData().add(new XYChart.Data<>(i, 0));
        }
    }

    private LineChart<Number, Number> createLineChart(Series<Number, Number> series, String yLabel) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<Number, Number> lineChart = new LineChart<Number, Number>(xAxis, yAxis);

        yAxis.setLabel(yLabel);

        lineChart.getData().add(series);

        lineChartToData.put(lineChart, series);

        lineChart.setMaxWidth(DEFAULT_WIDTH - 200);
        lineChart.setMaxHeight(300);
        lineChart.setAnimated(false);

        clearChart(series);

        series.setName("Used");

        return lineChart;
    }

    private PieChart createPieChart(ObservableList<PieChart.Data> data, String name) {
        PieChart memoryManagerChart = new PieChart(data);
        memoryManagerChart.setLabelsVisible(false);
        memoryManagerChart.setTitle(name);
        memoryManagerChart.setMaxWidth(150);
        memoryManagerChart.setMaxHeight(300);
        memoryManagerChart.setLegendVisible(true);
        return memoryManagerChart;
    }

    @Override
    public void tabOpened() {
        isTabOpen = true;
        if (job != null) {
            job.cancel(true);
        }

        Platform.runLater(() -> {
            for (var entry : lineChartToData.entrySet()) {
                clearChart(entry.getValue());
            }
        });
        job = scheduledExecutorService.scheduleAtFixedRate(() -> update(), 0, 1000, TimeUnit.MILLISECONDS);
        this.openTab();
    }

    @Override
    public void tabClosed() {
        isTabOpen = false;
        if (job != null) {
            job.cancel(true);
        }
        job = null;
    }

    public void update() {
        if (isTabOpen) {
            try {
                updateMediaCacheData();
                updateMemoryManagerData();
                updateJvmManagerData();
                updateMediaCacheDistribution();
            } catch (Exception e) {
                LOGGER.warn("Unable to refresh debug window", e);
            }
        }
    }

    private void updateMediaCacheDistribution() {
        Platform.runLater(() -> {
            Set<CacheData> cacheDistribution = mediaCache.getCacheDistribution();

            mediaCacheDistributionDataForTable.setAll(cacheDistribution);

            List<PieChart.Data> newList = new ArrayList<>();
            for (var element : cacheDistribution) {
                newList.add(new PieChart.Data(element.getKey(), element.rawDataSize()));
            }
            if (!isEquals(mediaCacheDistributionData, newList)) {
                if (listIdsEqual(mediaCacheDistributionData, newList)) {
                    for (var element : newList) {
                        int id = findNameInList(mediaCacheDistributionData, element.getName());
                        mediaCacheDistributionData.get(id).setPieValue(element.getPieValue());
                    }
                } else {
                    mediaCacheDistributionData.setAll(newList);
                }
            }
        });
    }

    private boolean listIdsEqual(ObservableList<javafx.scene.chart.PieChart.Data> a, List<javafx.scene.chart.PieChart.Data> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); ++i) {
            String name = a.get(i).getName();
            if (findNameInList(b, name) == -1) {
                return false;
            }
        }
        return true;
    }

    private int findNameInList(List<javafx.scene.chart.PieChart.Data> list, String name) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isEquals(ObservableList<javafx.scene.chart.PieChart.Data> a, List<javafx.scene.chart.PieChart.Data> b) {
        return convertList(a).equals(convertList(b));
    }

    private List<Entry<String, Double>> convertList(List<javafx.scene.chart.PieChart.Data> data) {
        return data.stream()
                .map(a -> Map.entry(a.getName(), a.getPieValue()))
                .collect(Collectors.toList());
    }

    private void updateMediaCacheData() {
        long bufferSize = mediaCache.recalculateBufferSize();
        long maximumSize = mediaCache.getMaximumSize();

        mediaCacheSizeData.get(0).setPieValue(minZero(bufferSize));
        mediaCacheSizeData.get(1).setPieValue(minZero(maximumSize - bufferSize));

        addAndShift(mediaCacheUsedSeries, bytesToMB(bufferSize));
    }

    private void addAndShift(Series<Number, Number> series, long newData) {
        Platform.runLater(() -> {
            ObservableList<Data<Number, Number>> list = series.getData();
            for (int i = 0; i < list.size() - 1; ++i) {
                list.get(i).setYValue(list.get(i + 1).getYValue().intValue());
            }
            list.get(list.size() - 1).setYValue(newData);
        });
    }

    private void updateMemoryManagerData() {
        long maximumSize = memoryManager.getMaximumSize();
        long currentSize = memoryManager.getCurrentSize();
        long readyToBeFreedSize = memoryManager.getReadyToBeFreedBuffers();
        long mediaCacheSize = mediaCache.recalculateBufferSize();

        memoryManagerSizeData.get(0).setPieValue(minZero(mediaCacheSize));
        memoryManagerSizeData.get(1).setPieValue(minZero(currentSize - mediaCacheSize - readyToBeFreedSize));
        memoryManagerSizeData.get(2).setPieValue(minZero(maximumSize - (currentSize - readyToBeFreedSize)));

        addAndShift(memoryManagerUsedSeries, bytesToMB(currentSize));
    }

    private void updateJvmManagerData() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long usedMemory = Runtime.getRuntime().totalMemory();

        jvmMemoryData.get(0).setPieValue(minZero(usedMemory));
        jvmMemoryData.get(1).setPieValue(minZero(maxMemory - usedMemory));

        addAndShift(jvmUsedSeries, bytesToMB(usedMemory));
    }

    private double minZero(long data) {
        return data < 0 ? 0 : data;
    }

    private long bytesToMB(long data) {
        return data / 1024 / 1024;
    }
}
