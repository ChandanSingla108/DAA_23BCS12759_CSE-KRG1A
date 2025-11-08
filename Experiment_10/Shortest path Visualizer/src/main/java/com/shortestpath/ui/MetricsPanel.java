package com.shortestpath.ui;

import com.shortestpath.algorithm.AlgorithmResult;
import com.shortestpath.algorithm.AlgorithmStep;
import com.shortestpath.model.Node;
import com.shortestpath.ui.AlgorithmState;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MetricsPanel extends VBox {
    private final Label titleLabel;
    private final Label currentAlgorithmLabel;
    private final Label executionTimeLabel;
    private final Label nodesVisitedLabel;
    private final Label pathCostLabel;
    private final TextArea pathSequenceArea;
    private final Label currentStepLabel;

    private final TableView<ComparisonRow> comparisonTable;
    private final Map<String, AlgorithmResult> comparisonResults;

    private AlgorithmResult currentResult;

    public MetricsPanel() {
        setSpacing(12);
        setPadding(new Insets(10));
        setPrefWidth(330);
        setStyle("-fx-background-color: rgba(0,0,0,0.03);");

        titleLabel = new Label("Metrics Panel");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Current algorithm section
        Label currentSectionTitle = new Label("Current Algorithm");
        currentSectionTitle.setStyle("-fx-font-weight: bold;");
        currentAlgorithmLabel = new Label("Algorithm: —");
        executionTimeLabel = new Label("Execution Time: —");
        nodesVisitedLabel = new Label("Nodes Visited: —");
        pathCostLabel = new Label("Path Cost: —");
        currentStepLabel = new Label("Current Step: —");
        currentStepLabel.setStyle("-fx-font-weight: bold;");

        // Path sequence section
        Label pathSectionTitle = new Label("Path Sequence");
        pathSectionTitle.setStyle("-fx-font-weight: bold;");
        pathSequenceArea = new TextArea();
        pathSequenceArea.setEditable(false);
        pathSequenceArea.setWrapText(true);
        pathSequenceArea.setPrefRowCount(4);
        pathSequenceArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");

        // Comparison section
        Label comparisonTitle = new Label("Comparison");
        comparisonTitle.setStyle("-fx-font-weight: bold;");
        comparisonTable = new TableView<>();
        setupComparisonTable();

        comparisonResults = new HashMap<>();

        getChildren().addAll(
                titleLabel,
                new Separator(Orientation.HORIZONTAL),
                currentSectionTitle,
                currentAlgorithmLabel,
                executionTimeLabel,
                nodesVisitedLabel,
                pathCostLabel,
                currentStepLabel,
                new Separator(Orientation.HORIZONTAL),
                pathSectionTitle,
                pathSequenceArea,
                new Separator(Orientation.HORIZONTAL),
                comparisonTitle,
                comparisonTable
        );

        clearMetrics();
    }

    public void loadAlgorithmResult(AlgorithmResult result, String algorithmName) {
        if (result == null) return;
        this.currentResult = result;

        currentAlgorithmLabel.setText("Algorithm: " + algorithmName);
        executionTimeLabel.setText("Execution Time: " + result.getExecutionTimeMs() + " ms");
        nodesVisitedLabel.setText("Nodes Visited: " + result.getNodesVisited());
        if (result.hasPath()) {
            pathCostLabel.setText("Path Cost: " + String.format("%.2f", result.getPathCost()));
            List<Node> path = result.getShortestPath();
            String pathStr = (path == null || path.isEmpty())
                    ? "No path exists"
                    : path.stream().map(Node::getId).collect(Collectors.joining(" → "));
            pathSequenceArea.setText(pathStr);
        } else {
            pathCostLabel.setText("Path Cost: ∞ (No path found)");
            pathSequenceArea.setText("No path exists");
        }

        comparisonResults.put(algorithmName, result);
        updateComparisonTable();
    }

    public void updateCurrentStep(AlgorithmState state) {
        if (state == null || !state.hasAlgorithm()) {
            currentStepLabel.setText("Current Step: —");
            return;
        }
        currentStepLabel.setText("Current Step: " + (state.getCurrentStepIndex() + 1) + " / " + state.getTotalSteps());
    }

    public void updateFromStep(AlgorithmStep step) {
        if (step == null) return;
        if (currentResult == null) return;
        int visitedCount = step.getVisitedNodes() != null ? step.getVisitedNodes().size() : currentResult.getNodesVisited();
        nodesVisitedLabel.setText("Nodes Visited: " + visitedCount);
    }

    public void clearMetrics() {
        currentAlgorithmLabel.setText("Algorithm: —");
        executionTimeLabel.setText("Execution Time: —");
        nodesVisitedLabel.setText("Nodes Visited: —");
        pathCostLabel.setText("Path Cost: —");
        pathSequenceArea.clear();
        currentStepLabel.setText("Current Step: —");
        clearComparison();
        currentResult = null;
    }

    public void addComparisonResult(String algorithmName, AlgorithmResult result) {
        if (algorithmName == null || result == null) return;
        comparisonResults.put(algorithmName, result);
        updateComparisonTable();
    }

    public void clearComparison() {
        comparisonResults.clear();
        comparisonTable.getItems().clear();
    }

    private void setupComparisonTable() {
        TableColumn<ComparisonRow, String> algoCol = new TableColumn<>("Algorithm");
        algoCol.setCellValueFactory(new PropertyValueFactory<>("algorithmName"));
        algoCol.setPrefWidth(120);

        TableColumn<ComparisonRow, Long> timeCol = new TableColumn<>("Time (ms)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("executionTime"));
        timeCol.setPrefWidth(90);

        TableColumn<ComparisonRow, Integer> nodesCol = new TableColumn<>("Nodes Visited");
        nodesCol.setCellValueFactory(new PropertyValueFactory<>("nodesVisited"));
        nodesCol.setPrefWidth(110);

        TableColumn<ComparisonRow, Double> costCol = new TableColumn<>("Path Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("pathCost"));
        costCol.setPrefWidth(90);

        TableColumn<ComparisonRow, Integer> stepsCol = new TableColumn<>("Steps");
        stepsCol.setCellValueFactory(new PropertyValueFactory<>("stepCount"));
        stepsCol.setPrefWidth(70);

        comparisonTable.getColumns().addAll(algoCol, timeCol, nodesCol, costCol, stepsCol);
        comparisonTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        comparisonTable.setPrefHeight(170);
    }

    private void updateComparisonTable() {
        comparisonTable.getItems().clear();
        if (comparisonResults.size() < 2) {
            return;
        }
        List<String> order = new ArrayList<>();
        order.add("Dijkstra");
        order.add("Bellman-Ford");
        order.add("A*");

        for (String key : order) {
            AlgorithmResult r = comparisonResults.get(key);
            if (r != null) {
                comparisonTable.getItems().add(new ComparisonRow(key, r));
            }
        }
        // Add any other algorithms if present
        for (Map.Entry<String, AlgorithmResult> e : comparisonResults.entrySet()) {
            if (!order.contains(e.getKey())) {
                comparisonTable.getItems().add(new ComparisonRow(e.getKey(), e.getValue()));
            }
        }
    }

    public static class ComparisonRow {
        private final SimpleStringProperty algorithmName;
        private final SimpleLongProperty executionTime;
        private final SimpleIntegerProperty nodesVisited;
        private final SimpleDoubleProperty pathCost;
        private final SimpleIntegerProperty stepCount;

        public ComparisonRow(String algorithmName, AlgorithmResult result) {
            this.algorithmName = new SimpleStringProperty(algorithmName);
            this.executionTime = new SimpleLongProperty(result.getExecutionTimeMs());
            this.nodesVisited = new SimpleIntegerProperty(result.getNodesVisited());
            this.pathCost = new SimpleDoubleProperty(result.hasPath() ? result.getPathCost() : Double.POSITIVE_INFINITY);
            this.stepCount = new SimpleIntegerProperty(result.getStepCount());
        }

        public String getAlgorithmName() { return algorithmName.get(); }
        public long getExecutionTime() { return executionTime.get(); }
        public int getNodesVisited() { return nodesVisited.get(); }
        public double getPathCost() { return pathCost.get(); }
        public int getStepCount() { return stepCount.get(); }
    }
}
