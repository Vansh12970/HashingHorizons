package com.hashinghorizons.controller;

import com.hashinghorizons.service.SimulationService;
import com.hashinghorizons.service.SimulationService.SimulationResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class MainController {

    @FXML private ComboBox<String> methodBox;
    @FXML private Spinner<Integer> tableSizeSpinner;
    @FXML private TextArea keysArea;
    @FXML private Button loadSampleBtn, generateBtn, runBtn, resetBtn, exportBtn, aboutBtn;
    @FXML private Pane vizPane;
    @FXML private BarChart<String, Number> resultChart;
    @FXML private TextArea logArea;
    @FXML private Label statusTableSize, statusMethod, statusLoad;
    @FXML private Label metricCollisions, metricElements, metricLoad, metricTime;
    @FXML private CheckBox showProbesCheck, measureTimeCheck;
    @FXML private Slider speedSlider;

    private final SimulationService simService = new SimulationService();

    @FXML
    public void initialize() {
        methodBox.getItems().addAll("Chaining", "Linear Probing", "Quadratic Probing", "Double Hashing");
        methodBox.setValue("Chaining");

        tableSizeSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 200, 10)
        );

        loadSampleBtn.setOnAction(e -> loadSample());
        generateBtn.setOnAction(e -> generateRandom());
        runBtn.setOnAction(e -> runSimulation());
        resetBtn.setOnAction(e -> resetUI());
        aboutBtn.setOnAction(e -> showAbout());
        exportBtn.setOnAction(e -> exportCSV());

        clearViz();
        updateStatus();
    }

    private void updateStatus() {
        statusTableSize.setText("Table: " + tableSizeSpinner.getValue());
        statusMethod.setText("Method: " + methodBox.getValue());
        statusLoad.setText("Load: -");
    }

    private void loadSample() {
        try {
            InputStream in = getClass().getResourceAsStream("/data/sample_keys.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) sb.append(line).append(",");
            }
            keysArea.setText(sb.toString());
            log("Loaded sample keys.");
        } catch (Exception ex) {
            log("Failed to load sample keys: " + ex.getMessage());
        }
    }

    private void generateRandom() {
        int count = Math.min((int) tableSizeSpinner.getValue(), 12);
        int[] keys = simService.generateRandomKeys(count, 500, System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        for (int k : keys) sb.append(k).append(" ");
        keysArea.setText(sb.toString());
        log("Generated " + count + " random keys.");
    }

    private void runSimulation() {
    String method = methodBox.getValue();
    int tableSize = tableSizeSpinner.getValue();
    String csv = keysArea.getText().trim();
    int[] keys = csv.isEmpty()
            ? new int[]{15, 23, 7, 89, 42, 56, 78, 90, 34, 67}
            : simService.parseKeys(csv);

    updateStatus();
    clearViz();
    drawEmptyGrid(tableSize);

    log("Running " + method + " with table size " + tableSize + " ...");

    boolean shouldMeasureTime = measureTimeCheck.isSelected();

    new Thread(() -> {
        SimulationService.SimulationResult result;
        if (shouldMeasureTime) {
            result = simService.runSimulation(method, tableSize, keys);
        } else {
            // Run simulation without time measurement
            com.hashinghorizons.model.HashTable ht;
            switch (method) {
                case "Linear Probing": ht = new com.hashinghorizons.model.LinearProbingHash(tableSize); break;
                case "Quadratic Probing": ht = new com.hashinghorizons.model.QuadraticProbingHash(tableSize); break;
                case "Double Hashing": ht = new com.hashinghorizons.model.DoubleHashing(tableSize); break;
                default: ht = new com.hashinghorizons.model.ChainingHash(tableSize);
            }
            for (int k : keys) ht.insert(k);
            result = new SimulationService.SimulationResult(method, tableSize,
                    ht.getElementCount(), ht.getCollisions(),
                    (double) ht.getElementCount() / tableSize, 0);
        }

        final boolean showTime = shouldMeasureTime;
        Platform.runLater(() -> {
            showResult(result, showTime);
            animateInsertion(result, keys, method);
        });
    }).start();
}


    private void showResult(SimulationResult r, boolean showTime) {
    metricCollisions.setText("Collisions: " + r.collisions);
    metricElements.setText("Elements: " + r.elements);
    metricLoad.setText(String.format("Load Factor: %.2f", r.loadFactor));

    if (showTime) {
        metricTime.setText(String.format("Insert Time (ms): %.3f", r.insertTimeNanos / 1_000_000.0));
    } else {
        metricTime.setText("Insert Time (ms): N/A");
    }

    statusLoad.setText(String.format("Load: %.2f", r.loadFactor));

    resultChart.getData().clear();
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.getData().add(new XYChart.Data<>("Collisions", r.collisions));
    series.getData().add(new XYChart.Data<>("Load Factor", r.loadFactor));
    series.getData().add(new XYChart.Data<>("Elements", r.elements));
    resultChart.getData().add(series);

    log("Simulation complete: " + r.technique + " | collisions=" + r.collisions);
}


    private void clearViz() {
        vizPane.getChildren().clear();
    }

    private void drawEmptyGrid(int tableSize) {
        vizPane.getChildren().clear();

        double w = vizPane.getWidth() <= 0 ? 860 : vizPane.getWidth();
        int cols = (int) Math.max(4, Math.floor(w / 70));
        double slotW = Math.min(80, (w - 20) / cols);
        double x = 10, y = 10;

        for (int i = 0; i < tableSize; i++) {
            Rectangle r = new Rectangle(slotW - 8, 48);
            r.setArcWidth(10);
            r.setArcHeight(10);
            r.setX(x);
            r.setY(y);
            r.setStroke(Color.web("#697565", 0.25));
            r.setFill(Color.web("#3C3D37"));
            r.setUserData(i);
            vizPane.getChildren().add(r);

            x += slotW;
            if ((i + 1) % cols == 0) { x = 10; y += 64; }
        }

        vizPane.widthProperty().addListener((obs, oldW, newW) -> drawEmptyGrid(tableSize));
    }

    private void animateInsertion(SimulationService.SimulationResult result, int[] keys, String method) {
        clearViz();
        drawEmptyGrid(result.tableSize);

        com.hashinghorizons.model.HashTable ht;
        switch (method) {
            case "Linear Probing": ht = new com.hashinghorizons.model.LinearProbingHash(result.tableSize); break;
            case "Quadratic Probing": ht = new com.hashinghorizons.model.QuadraticProbingHash(result.tableSize); break;
            case "Double Hashing": ht = new com.hashinghorizons.model.DoubleHashing(result.tableSize); break;
            default: ht = new com.hashinghorizons.model.ChainingHash(result.tableSize);
        }

        javafx.scene.Node[] nodes = vizPane.getChildren().toArray(new javafx.scene.Node[0]);
        double speedFactor = speedSlider.getValue();
        double baseDelay = 500 / speedFactor;

        new Thread(() -> {
            for (int k : keys) {
                int primaryIndex = k % result.tableSize;
                if (primaryIndex < 0) primaryIndex += result.tableSize;

                final int keyValue = k;
                final int baseIndex = primaryIndex;

                // ✅ Only log probes if checkbox is checked
                if (showProbesCheck.isSelected()) {
                    Platform.runLater(() -> log("Inserting key " + keyValue + " → base index " + baseIndex));
                }

                if (baseIndex < nodes.length && nodes[baseIndex] instanceof Rectangle) {
                    Rectangle active = (Rectangle) nodes[baseIndex];
                    Platform.runLater(() -> active.setFill(Color.web("#F5A623")));
                }

                ht.insert(keyValue);
                int finalIndexTemp = ht.getLastIndex();
                final int finalIndex = finalIndexTemp;

                // ✅ Only log collision if checkbox is checked
                if (finalIndex != baseIndex && !method.equals("Chaining") && showProbesCheck.isSelected()) {
                    Platform.runLater(() -> log("Collision detected → resolved at " + finalIndex));
                }

                if (finalIndex >= 0 && finalIndex < nodes.length && nodes[finalIndex] instanceof Rectangle) {
                    Rectangle r = (Rectangle) nodes[finalIndex];
                    Platform.runLater(() -> {
                        r.setFill(method.equals("Chaining") ? Color.web("#6CA0DC") : Color.web("#A8E6A1"));

                        // ✅ Remove old overlapping text
                        vizPane.getChildren().removeIf(n -> n instanceof Text &&
                                Math.abs(((Text) n).getX() - (r.getX() + r.getWidth() / 3.8)) < 5 &&
                                Math.abs(((Text) n).getY() - (r.getY() + 30)) < 5);

                        Text keyText = new Text(String.valueOf(keyValue));
                        keyText.setFill(Color.web("#1E201E"));
                        keyText.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                        double labelY = r.getY() + 30;

                        // ✅ For chaining: slightly offset keys in same slot
                        if (method.equals("Chaining")) {
                            long existingCount = vizPane.getChildren().stream()
                                    .filter(n -> n instanceof Text &&
                                            Math.abs(((Text) n).getX() - (r.getX() + r.getWidth() / 3.8)) < 5)
                                    .count();
                            labelY = r.getY() + 20 + (existingCount * 15);
                        }

                        keyText.setX(r.getX() + r.getWidth() / 3.8);
                        keyText.setY(labelY);

                        Tooltip t = new Tooltip("Index: " + finalIndex + "\nKey: " + keyValue);
                        Tooltip.install(r, t);

                        vizPane.getChildren().add(keyText);

                        FadeTransition ft = new FadeTransition(Duration.millis(300), r);
                        ft.setFromValue(0.5);
                        ft.setToValue(1.0);
                        ft.play();
                    });
                }

                try {
                    Thread.sleep((long) baseDelay);
                } catch (InterruptedException ignored) {}
            }

            Platform.runLater(() -> log("Insertion completed."));
        }).start();
    }

    private void log(String s) {
        logArea.appendText(s + "\n");
    }

    private void resetUI() {
        keysArea.clear();
        logArea.clear();
        resultChart.getData().clear();
        metricCollisions.setText("Collisions: -");
        metricElements.setText("Elements: -");
        metricLoad.setText("Load Factor: -");
        metricTime.setText("Insert Time (ms): -");
        clearViz();
        updateStatus();
    }

    private void showAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("About");
        a.setHeaderText("Hashing Horizons");
        a.setContentText(
                "Project: Hashing Horizons\n" +
                        "Author: Vansh Pratap Singh\n" +
                        "Course: Design and Analysis of Algorithms (DAA)\n" +
                        "Description:\nThis project visualizes and compares different hashing collision-handling techniques:\n" +
                        "• Chaining\n• Linear Probing\n• Quadratic Probing\n• Double Hashing\n\n" +
                        "It displays load factor, collisions, and performance metrics visually."
        );
        a.showAndWait();
    }

    private void exportCSV() {
        try {
            SimulationService.SimulationResult lastResult = simService.getLastResult();
            if (lastResult == null) {
                log("No data to export. Run a simulation first.");
                return;
            }

            String filename = "hashing_metrics.csv";
            java.io.File file = new java.io.File(filename);
            java.io.FileWriter fw = new java.io.FileWriter(file);

            fw.write("Technique,Table Size,Collisions,Elements,Load Factor,Insert Time (ms)\n");
            fw.write(String.format("%s,%d,%d,%d,%.3f,%.3f\n",
                    lastResult.technique,
                    lastResult.tableSize,
                    lastResult.collisions,
                    lastResult.elements,
                    lastResult.loadFactor,
                    lastResult.insertTimeNanos / 1_000_000.0));
            fw.close();

            log("Exported results to " + filename);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Export Complete");
            a.setHeaderText("Data Exported Successfully");
            a.setContentText("File saved as " + filename + " in project directory.");
            a.showAndWait();

        } catch (Exception e) {
            log("Export failed: " + e.getMessage());
        }
    }
}
