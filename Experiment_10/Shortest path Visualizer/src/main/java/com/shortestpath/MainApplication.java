package com.shortestpath;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;
import com.shortestpath.ui.GraphRenderer;
import com.shortestpath.util.RandomGraphGenerator;
import com.shortestpath.ui.AnimationEngine;
import com.shortestpath.ui.AlgorithmState;
import com.shortestpath.algorithm.AlgorithmStep;
import com.shortestpath.algorithm.DijkstraAlgorithm;
import com.shortestpath.algorithm.BellmanFordAlgorithm;
import com.shortestpath.algorithm.AStarAlgorithm;
import com.shortestpath.algorithm.AlgorithmResult;
import com.shortestpath.ui.AlgorithmVisualizer;
import com.shortestpath.ui.MetricsPanel;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Separator;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.util.Map;
import java.util.Optional;

public class MainApplication extends Application {
    private BorderPane root;
    private SwingNode graphSwingNode;
    private WeightedGraph currentGraph;
    private GraphRenderer graphRenderer;
    private TextField nodeCountField;
    private TextField edgeDensityField;
    private TextField minWeightField;
    private TextField maxWeightField;
    private Button generateButton;
    private Label statusLabel;
    private AnimationEngine animationEngine;
    private Button playButton;
    private Button pauseButton;
    private Button stepForwardButton;
    private Button stepBackwardButton;
    private Button resetButton;
    private Slider speedSlider;
    private Label stepInfoLabel;
    private Label stepDescriptionLabel;
    private ComboBox<String> algorithmSelector;
    private TextField sourceNodeField;
    private TextField targetNodeField;
    private Button runAlgorithmButton;
    private Label algorithmStatusLabel;
    private AlgorithmVisualizer algorithmVisualizer;
    private Label distancesLabel;
    private MetricsPanel metricsPanel;
    private Button runAllAlgorithmsButton;
    private Button clearAllButton;
    private Button selectSourceButton;
    private Button selectTargetButton;

    private enum NodeSelectionMode { NONE, SELECTING_SOURCE, SELECTING_TARGET }
    private NodeSelectionMode nodeSelectionMode = NodeSelectionMode.NONE;

    private Label graphInfoLabel = new Label("");
    private Label appStateLabel = new Label("Ready");

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox controlPanel = createControlPanel();
        root.setTop(controlPanel);
        BorderPane.setMargin(controlPanel, new Insets(5));

        graphSwingNode = new SwingNode();
        root.setCenter(graphSwingNode);
        BorderPane.setMargin(graphSwingNode, new Insets(5));

        currentGraph = new WeightedGraph(true);
        Node a = new Node("A", 100, 100, "A");
        Node b = new Node("B", 300, 100, "B");
        Node c = new Node("C", 200, 250, "C");
        Node d = new Node("D", 400, 250, "D");
        Node nodeE = new Node("E", 550, 150, "E");

        currentGraph.addNode(a);
        currentGraph.addNode(b);
        currentGraph.addNode(c);
        currentGraph.addNode(d);
        currentGraph.addNode(nodeE);

        currentGraph.addEdge(a, b, 5.0);
        currentGraph.addEdge(a, c, 3.0);
        currentGraph.addEdge(b, d, 2.0);
        currentGraph.addEdge(c, d, 4.0);
        currentGraph.addEdge(d, nodeE, 6.0);

        graphRenderer = new GraphRenderer();
        graphRenderer.renderGraph(currentGraph, graphSwingNode);
        graphRenderer.setNodeClickCallback(this::handleNodeClick);

        // Initialize animation engine and bottom control panel
        animationEngine = new AnimationEngine();
        animationEngine.addStateListener(this::updateControlStates);
        animationEngine.addStepListener(this::updateStepInfo);
        // Initialize algorithm visualizer and register as a step listener
        algorithmVisualizer = new AlgorithmVisualizer(graphRenderer, currentGraph);
        animationEngine.addStepListener(algorithmVisualizer::visualizeStep);

        metricsPanel = new MetricsPanel();
        TitledPane metricsPane = new TitledPane("Metrics", metricsPanel);
        metricsPane.setCollapsible(true);
        metricsPane.setExpanded(true);
        root.setRight(metricsPane);
        BorderPane.setMargin(metricsPane, new Insets(5));
        animationEngine.addStateListener(metricsPanel::updateCurrentStep);

        HBox animationControlPanel = createAnimationControlPanel();
        HBox statusBar = createStatusBar();
        VBox bottom = new VBox(6, animationControlPanel, statusBar);
        root.setBottom(bottom);
        BorderPane.setMargin(bottom, new Insets(5));

        Scene scene = new Scene(root, 1200, 800);
        try {
            String css = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignore) { /* continue without CSS */ }
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        scene.setOnKeyPressed(this::handleKeyPress);
        scene.widthProperty().addListener((obs, ov, nv) -> {
            double w = nv.doubleValue();
            controlPanel.setSpacing(w < 1200 ? 6 : 10);
            if (w < 1000) {
                appStateLabel.setText("Compact layout: limited space");
            } else {
                appStateLabel.setText("Ready");
            }
        });
        primaryStage.setTitle("Shortest Path Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> { if (animationEngine != null) animationEngine.dispose(); });
        primaryStage.show();

        // Algorithm execution controls are available in the top control panel
    }

    private HBox createControlPanel() {
        nodeCountField = new TextField("10");
        nodeCountField.setPrefWidth(80);
        nodeCountField.setPromptText("10");
        nodeCountField.getStyleClass().addAll("input-field", "small-input");
        nodeCountField.setTooltip(new Tooltip("Number of nodes to generate (1-100)"));

        edgeDensityField = new TextField("0.3");
        edgeDensityField.setPrefWidth(80);
        edgeDensityField.setPromptText("0.3");
        edgeDensityField.getStyleClass().addAll("input-field", "small-input");
        edgeDensityField.setTooltip(new Tooltip("Edge density: 0.0 (sparse) to 1.0 (dense)"));

        minWeightField = new TextField("1.0");
        minWeightField.setPrefWidth(80);
        minWeightField.setPromptText("1.0");
        minWeightField.getStyleClass().addAll("input-field", "small-input");
        minWeightField.setTooltip(new Tooltip("Minimum edge weight (must be positive)"));

        maxWeightField = new TextField("10.0");
        maxWeightField.setPrefWidth(80);
        maxWeightField.setPromptText("10.0");
        maxWeightField.getStyleClass().addAll("input-field", "small-input");
        maxWeightField.setTooltip(new Tooltip("Maximum edge weight (must be >= min weight)"));

        generateButton = new Button("Generate Random Graph");
        generateButton.setOnAction(e -> handleGenerateGraph());
        generateButton.getStyleClass().add("primary-button");
        generateButton.setTooltip(new Tooltip("Generate a new random connected weighted graph"));

        Label nodesLbl = new Label("Nodes:");
        Label densityLbl = new Label("Density:");
        Label minWLbl = new Label("Min Weight:");
        Label maxWLbl = new Label("Max Weight:");

        statusLabel = new Label("");
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.getStyleClass().add("status-label");

        // Algorithm controls
        Separator sep = new Separator(Orientation.VERTICAL);
        algorithmSelector = new ComboBox<>();
        algorithmSelector.getItems().addAll("Dijkstra", "Bellman-Ford", "A*");
        algorithmSelector.setValue("Dijkstra");
        algorithmSelector.setPrefWidth(130);
        algorithmSelector.setTooltip(new Tooltip("Choose shortest path algorithm to execute"));

        selectSourceButton = new Button("📍");
        selectSourceButton.setOnAction(e -> { nodeSelectionMode = NodeSelectionMode.SELECTING_SOURCE; updateSelectionModeUI(); });
        selectSourceButton.getStyleClass().add("icon-button");
        selectSourceButton.setPrefWidth(40);
        selectSourceButton.setTooltip(new Tooltip("Click to select source node from graph"));

        sourceNodeField = new TextField("A");
        sourceNodeField.setPrefWidth(60);
        sourceNodeField.setPromptText("Source");
        sourceNodeField.getStyleClass().addAll("input-field", "small-input");
        sourceNodeField.setTooltip(new Tooltip("Source node ID (click on graph to select)"));

        selectTargetButton = new Button("📍");
        selectTargetButton.setOnAction(e -> { nodeSelectionMode = NodeSelectionMode.SELECTING_TARGET; updateSelectionModeUI(); });
        selectTargetButton.getStyleClass().add("icon-button");
        selectTargetButton.setPrefWidth(40);
        selectTargetButton.setTooltip(new Tooltip("Click to select target node from graph"));

        targetNodeField = new TextField("E");
        targetNodeField.setPrefWidth(60);
        targetNodeField.setPromptText("Target");
        targetNodeField.getStyleClass().addAll("input-field", "small-input");
        targetNodeField.setTooltip(new Tooltip("Target node ID (click on graph to select)"));

        runAlgorithmButton = new Button("Run Algorithm");
        runAlgorithmButton.setOnAction(e -> handleRunAlgorithm());
        runAlgorithmButton.setPrefWidth(130);
        runAlgorithmButton.getStyleClass().add("primary-button");
        runAlgorithmButton.setTooltip(new Tooltip("Execute selected algorithm and load results for animation"));

        runAllAlgorithmsButton = new Button("Run All Algorithms");
        runAllAlgorithmsButton.setOnAction(e -> handleRunAllAlgorithms());
        runAllAlgorithmsButton.setPrefWidth(150);
        runAllAlgorithmsButton.setTooltip(new Tooltip("Execute Dijkstra, Bellman-Ford, and A* sequentially for comparison"));
        runAllAlgorithmsButton.getStyleClass().add("secondary-button");

        clearAllButton = new Button("🗑 Clear All");
        clearAllButton.getStyleClass().add("danger-button");
        clearAllButton.setOnAction(e -> handleClearAll());
        clearAllButton.setTooltip(new Tooltip("Clear graph, algorithm results, and reset all controls to initial state"));

        algorithmStatusLabel = new Label("");
        algorithmStatusLabel.setTextFill(Color.GREEN);

        HBox h = new HBox(10,
                nodesLbl, nodeCountField,
                densityLbl, edgeDensityField,
                minWLbl, minWeightField,
                maxWLbl, maxWeightField,
                generateButton,
                sep,
                new Label("Algorithm:"), algorithmSelector,
                new Label("From:"), selectSourceButton, sourceNodeField,
                new Label("To:"), selectTargetButton, targetNodeField,
                runAlgorithmButton,
                runAllAlgorithmsButton,
                clearAllButton,
                algorithmStatusLabel,
                statusLabel);
        h.setPadding(new Insets(10));
        h.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(algorithmStatusLabel, Priority.ALWAYS);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        return h;
    }

    private void handleGenerateGraph() {
        int nodeCount;
        double density;
        double minW;
        double maxW;
        try {
            nodeCount = Integer.parseInt(nodeCountField.getText().trim());
            density = Double.parseDouble(edgeDensityField.getText().trim());
            minW = Double.parseDouble(minWeightField.getText().trim());
            maxW = Double.parseDouble(maxWeightField.getText().trim());
        } catch (NumberFormatException ex) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Invalid input: please enter valid numbers");
            showErrorAlert("Invalid Input", "Please enter valid numeric values.", "Nodes must be integer. Density and weights must be numbers.");
            return;
        }

        if (nodeCount < 1 || nodeCount > 100 || density < 0.0 || density > 1.0 || minW <= 0.0 || maxW < minW) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Invalid parameter ranges");
            showErrorAlert("Invalid Parameters", "One or more parameters are out of range.",
                    "Nodes: 1-100\nDensity: 0.0-1.0\nWeights: min>0 and max>=min");
            return;
        }

        try {
            currentGraph = RandomGraphGenerator.generateRandomGraph(nodeCount, density, minW, maxW, true);
        } catch (IllegalArgumentException ex) {
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText(ex.getMessage());
            showErrorAlert("Graph Generation Failed", "Could not generate graph.", ex.getMessage());
            return;
        }

        graphRenderer.renderGraph(currentGraph, graphSwingNode);
        graphRenderer.setNodeClickCallback(this::handleNodeClick);
        if (algorithmVisualizer != null) {
            algorithmVisualizer.updateGraph(currentGraph);
        }
        if (animationEngine != null) {
            animationEngine.stop();
        }
        if (metricsPanel != null) {
            metricsPanel.clearMetrics();
        }
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText("Generated graph with " + currentGraph.getNodeCount() + " nodes and " + currentGraph.getEdgeCount() + " edges");
        nodeSelectionMode = NodeSelectionMode.NONE;
        updateSelectionModeUI();
        updateGraphInfoLabel();
    }

    private HBox createAnimationControlPanel() {
        playButton = new Button("▶ Play");
        playButton.setOnAction(e -> animationEngine.play());
        playButton.setPrefWidth(100);
        playButton.getStyleClass().add("icon-button");
        playButton.setTooltip(new Tooltip("Play animation automatically (Space)"));

        pauseButton = new Button("⏸ Pause");
        pauseButton.setOnAction(e -> animationEngine.pause());
        pauseButton.setPrefWidth(100);
        pauseButton.getStyleClass().add("icon-button");
        pauseButton.setTooltip(new Tooltip("Pause animation (Space)"));

        stepBackwardButton = new Button("⏮ Step Back");
        stepBackwardButton.setOnAction(e -> animationEngine.stepBackward());
        stepBackwardButton.setPrefWidth(120);
        stepBackwardButton.getStyleClass().add("icon-button");
        stepBackwardButton.setTooltip(new Tooltip("Go to previous step (Left Arrow)"));

        stepForwardButton = new Button("Step Forward ⏭");
        stepForwardButton.setOnAction(e -> animationEngine.stepForward());
        stepForwardButton.setPrefWidth(130);
        stepForwardButton.getStyleClass().add("icon-button");
        stepForwardButton.setTooltip(new Tooltip("Go to next step (Right Arrow)"));

        resetButton = new Button("⏹ Reset");
        resetButton.setOnAction(e -> animationEngine.reset());
        resetButton.setPrefWidth(100);
        resetButton.getStyleClass().add("icon-button");
        resetButton.setTooltip(new Tooltip("Reset animation to first step (Home)"));

        Label speedLbl = new Label("Speed:");
        speedSlider = new Slider(0.5, 3.0, 1.0);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setPrefWidth(200);
        speedSlider.valueProperty().addListener((obs, ov, nv) -> animationEngine.setSpeed(nv.doubleValue()));
        speedSlider.getStyleClass().add("speed-slider");
        speedSlider.setTooltip(new Tooltip("Adjust animation playback speed (0.5x to 3.0x)"));

        stepInfoLabel = new Label("No algorithm loaded");
        stepInfoLabel.setMinWidth(150);
        stepInfoLabel.getStyleClass().add("title-label");

        stepDescriptionLabel = new Label("");
        stepDescriptionLabel.setMaxWidth(400);
        stepDescriptionLabel.setWrapText(true);
        stepDescriptionLabel.getStyleClass().add("info-label");

        distancesLabel = new Label("");
        distancesLabel.setMaxWidth(400);
        distancesLabel.setWrapText(true);

        HBox controlPanel = new HBox(10,
                playButton,
                pauseButton,
                stepBackwardButton,
                stepForwardButton,
                resetButton,
                new Separator(Orientation.VERTICAL),
                speedLbl,
                speedSlider,
                new Separator(Orientation.VERTICAL),
                stepInfoLabel,
                stepDescriptionLabel,
                distancesLabel
        );
        controlPanel.setPadding(new Insets(10));
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(stepDescriptionLabel, Priority.ALWAYS);
        HBox.setHgrow(distancesLabel, Priority.ALWAYS);
        controlPanel.getStyleClass().add("animation-panel");

        updateControlStates(animationEngine.getCurrentState());
        return controlPanel;
    }

    private void updateControlStates(AlgorithmState state) {
        playButton.setDisable(!state.hasAlgorithm() || state.isPlaying() || state.isAtEnd());
        pauseButton.setDisable(!state.isPlaying());
        stepForwardButton.setDisable(!state.canStepForward() || state.isPlaying());
        stepBackwardButton.setDisable(!state.canStepBackward() || state.isPlaying());
        resetButton.setDisable(!state.hasAlgorithm());
        speedSlider.setDisable(!state.hasAlgorithm());

        if (!state.hasAlgorithm()) {
            stepInfoLabel.setText("No algorithm loaded");
            if (appStateLabel != null) appStateLabel.setText("Ready");
        } else {
            stepInfoLabel.setText(String.format("Step %d / %d", state.getCurrentStepIndex() + 1, state.getTotalSteps()));
            if (appStateLabel != null) appStateLabel.setText(state.isPlaying() ? "Animating" : "Algorithm loaded");
        }
    }

    private void updateStepInfo(AlgorithmStep step) {
        if (step == null) {
            stepDescriptionLabel.setText("");
            if (distancesLabel != null) distancesLabel.setText("");
        } else {
            stepDescriptionLabel.setText(step.getDescription());
            if (distancesLabel != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Distances: ");
                int count = 0;
                for (Map.Entry<Node, Double> e : step.getDistances().entrySet()) {
                    if (count > 0) sb.append(", ");
                    sb.append(e.getKey().getId()).append("=");
                    double d = e.getValue();
                    sb.append(Double.isInfinite(d) ? "∞" : String.format("%.2f", d));
                    count++;
                    if (count >= 5) break;
                }
                distancesLabel.setText(sb.toString());
            }
        }
    }

    private void handleRunAlgorithm() {
        String algorithm = algorithmSelector.getValue();
        String sourceId = sourceNodeField.getText() == null ? "" : sourceNodeField.getText().trim();
        String targetId = targetNodeField.getText() == null ? "" : targetNodeField.getText().trim();

        if (algorithm == null || algorithm.isEmpty()) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Please select an algorithm");
            return;
        }
        if (sourceId.isEmpty() || targetId.isEmpty()) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Please enter source and target nodes");
            return;
        }

        Node source = currentGraph.getNode(sourceId);
        Node target = currentGraph.getNode(targetId);
        if (source == null) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Source node '" + sourceId + "' not found in graph");
            return;
        }
        if (target == null) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Target node '" + targetId + "' not found in graph");
            return;
        }

        try {
            AlgorithmResult result;
            switch (algorithm) {
                case "Dijkstra":
                    result = DijkstraAlgorithm.findShortestPath(currentGraph, source, target);
                    break;
                case "Bellman-Ford":
                    result = BellmanFordAlgorithm.findShortestPath(currentGraph, source, target);
                    break;
                case "A*":
                    result = AStarAlgorithm.findShortestPath(currentGraph, source, target);
                    break;
                default:
                    algorithmStatusLabel.setTextFill(Color.RED);
                    algorithmStatusLabel.setText("Unknown algorithm: " + algorithm);
                    return;
            }

            algorithmVisualizer.loadAlgorithmResult(result);
            animationEngine.loadAlgorithm(result);
            if (metricsPanel != null) {
                metricsPanel.loadAlgorithmResult(result, algorithm);
            }

            if (!result.hasPath()) {
                algorithmStatusLabel.setTextFill(Color.RED);
                algorithmStatusLabel.setText("No path found from " + sourceId + " to " + targetId);
                showWarningAlert("No Path Found", "The selected nodes are disconnected.",
                        "Try a denser graph or different nodes.");
            } else {
                algorithmStatusLabel.setTextFill(Color.GREEN);
                algorithmStatusLabel.setText("Executed " + algorithm + ": " + result.getStepCount() +
                        " steps, path cost: " + String.format("%.2f", result.getPathCost()));
            }
        } catch (Exception ex) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Error: " + ex.getMessage());
            showErrorAlert("Algorithm Error", "An error occurred while running the algorithm.", ex.toString());
        }
    }

    private void handleRunAllAlgorithms() {
        String sourceId = sourceNodeField.getText() == null ? "" : sourceNodeField.getText().trim();
        String targetId = targetNodeField.getText() == null ? "" : targetNodeField.getText().trim();

        if (sourceId.isEmpty() || targetId.isEmpty()) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Please enter source and target nodes");
            return;
        }

        Node source = currentGraph.getNode(sourceId);
        Node target = currentGraph.getNode(targetId);
        if (source == null) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Source node '" + sourceId + "' not found in graph");
            return;
        }
        if (target == null) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Target node '" + targetId + "' not found in graph");
            return;
        }

        if (metricsPanel != null) {
            metricsPanel.clearComparison();
        }

        AlgorithmResult dijkstraResult = null;
        AlgorithmResult bellmanFordResult = null;
        AlgorithmResult aStarResult = null;

        StringBuilder summary = new StringBuilder();
        boolean anyError = false;

        try {
            dijkstraResult = DijkstraAlgorithm.findShortestPath(currentGraph, source, target);
            if (metricsPanel != null) metricsPanel.addComparisonResult("Dijkstra", dijkstraResult);
            summary.append("Dijkstra: ").append(dijkstraResult.getStepCount()).append(" steps");
        } catch (Exception ex) {
            anyError = true;
        }

        try {
            bellmanFordResult = BellmanFordAlgorithm.findShortestPath(currentGraph, source, target);
            if (metricsPanel != null) metricsPanel.addComparisonResult("Bellman-Ford", bellmanFordResult);
            if (summary.length() > 0) summary.append(", ");
            summary.append("Bellman-Ford: ").append(bellmanFordResult.getStepCount()).append(" steps");
        } catch (Exception ex) {
            anyError = true;
        }

        try {
            aStarResult = AStarAlgorithm.findShortestPath(currentGraph, source, target);
            if (metricsPanel != null) metricsPanel.addComparisonResult("A*", aStarResult);
            if (summary.length() > 0) summary.append(", ");
            summary.append("A*: ").append(aStarResult.getStepCount()).append(" steps");
        } catch (Exception ex) {
            anyError = true;
        }

        AlgorithmResult chosen = aStarResult != null ? aStarResult : (dijkstraResult != null ? dijkstraResult : bellmanFordResult);
        String chosenName = aStarResult != null ? "A*" : (dijkstraResult != null ? "Dijkstra" : "Bellman-Ford");

        if (chosen != null) {
            algorithmVisualizer.loadAlgorithmResult(chosen);
            animationEngine.loadAlgorithm(chosen);
            if (metricsPanel != null) metricsPanel.loadAlgorithmResult(chosen, chosenName);
        }

        if (anyError) {
            algorithmStatusLabel.setTextFill(Color.RED);
            algorithmStatusLabel.setText("Executed with errors. " + summary);
            showWarningAlert("Partial Failure", "Some algorithms failed.", summary.toString());
        } else {
            algorithmStatusLabel.setTextFill(Color.GREEN);
            algorithmStatusLabel.setText("Executed all algorithms. Results in comparison table. " + summary);
        }
    }

    private void handleClearAll() {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Clear All");
        confirm.setHeaderText("Are you sure you want to clear everything?");
        confirm.setContentText("This will clear the graph, algorithm results, and reset all controls.");
        Optional<ButtonType> res = confirm.showAndWait();
        if (!res.isPresent() || res.get() != ButtonType.OK) return;

        try { animationEngine.stop(); } catch (Exception ignored) {}
        try { animationEngine.dispose(); } catch (Exception ignored) {}
        animationEngine = new AnimationEngine();
        animationEngine.addStateListener(this::updateControlStates);
        animationEngine.addStepListener(this::updateStepInfo);
        if (algorithmVisualizer != null) algorithmVisualizer.reset();
        if (metricsPanel != null) metricsPanel.clearMetrics();

        currentGraph = new WeightedGraph(true);
        graphRenderer.renderGraph(currentGraph, graphSwingNode);
        graphRenderer.setNodeClickCallback(this::handleNodeClick);
        if (algorithmVisualizer != null) algorithmVisualizer.updateGraph(currentGraph);

        nodeCountField.setText("10");
        edgeDensityField.setText("0.3");
        minWeightField.setText("1.0");
        maxWeightField.setText("10.0");
        sourceNodeField.setText("A");
        targetNodeField.setText("E");
        algorithmSelector.setValue("Dijkstra");

        statusLabel.setText("");
        algorithmStatusLabel.setText("");

        nodeSelectionMode = NodeSelectionMode.NONE;
        updateSelectionModeUI();
        updateControlStates(animationEngine.getCurrentState());
        updateGraphInfoLabel();
        showInfoAlert("Cleared", null, "Application reset to initial state.");
    }

    private void handleNodeClick(String nodeId) {
        if (nodeId == null || nodeId.isEmpty()) return;
        switch (nodeSelectionMode) {
            case SELECTING_SOURCE:
                sourceNodeField.setText(nodeId);
                nodeSelectionMode = NodeSelectionMode.NONE;
                updateSelectionModeUI();
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Source node set to: " + nodeId);
                break;
            case SELECTING_TARGET:
                targetNodeField.setText(nodeId);
                nodeSelectionMode = NodeSelectionMode.NONE;
                updateSelectionModeUI();
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Target node set to: " + nodeId);
                break;
            case NONE:
            default:
                // optional info
                statusLabel.setTextFill(Color.BLUE);
                statusLabel.setText("Clicked node: " + nodeId);
        }
    }

    private void updateSelectionModeUI() {
        switch (nodeSelectionMode) {
            case SELECTING_SOURCE:
                selectSourceButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
                selectTargetButton.setStyle("");
                statusLabel.setTextFill(Color.BLUE);
                statusLabel.setText("Click a node on the graph to set as source");
                break;
            case SELECTING_TARGET:
                selectTargetButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
                selectSourceButton.setStyle("");
                statusLabel.setTextFill(Color.BLUE);
                statusLabel.setText("Click a node on the graph to set as target");
                break;
            case NONE:
            default:
                selectSourceButton.setStyle("");
                selectTargetButton.setStyle("");
                // leave status as is
        }
    }

    private void handleKeyPress(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.SPACE) {
            if (animationEngine.getCurrentState().isPlaying()) animationEngine.pause(); else animationEngine.play();
            e.consume();
        } else if (code == KeyCode.LEFT) {
            animationEngine.stepBackward();
            e.consume();
        } else if (code == KeyCode.RIGHT) {
            animationEngine.stepForward();
            e.consume();
        } else if (code == KeyCode.HOME) {
            animationEngine.reset();
            e.consume();
        } else if (code == KeyCode.ESCAPE) {
            if (nodeSelectionMode != NodeSelectionMode.NONE) {
                nodeSelectionMode = NodeSelectionMode.NONE;
                updateSelectionModeUI();
                statusLabel.setTextFill(Color.BLUE);
                statusLabel.setText("Node selection cancelled");
                e.consume();
            }
        } else if (e.isControlDown() && code == KeyCode.G) {
            generateButton.requestFocus();
            e.consume();
        } else if (e.isControlDown() && code == KeyCode.R) {
            runAlgorithmButton.requestFocus();
            e.consume();
        }
    }

    private HBox createStatusBar() {
        graphInfoLabel = new Label("");
        appStateLabel = new Label("Ready");
        Label help = new Label("Press F1 for help");
        HBox bar = new HBox(12, graphInfoLabel, new Separator(Orientation.VERTICAL), appStateLabel, new Separator(Orientation.VERTICAL), help);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 10, 6, 10));
        return bar;
    }

    private void updateGraphInfoLabel() {
        if (graphInfoLabel != null && currentGraph != null) {
            graphInfoLabel.setText("Graph: " + currentGraph.getNodeCount() + " nodes, " + currentGraph.getEdgeCount() + " edges");
        }
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert a = new Alert(AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showWarningAlert(String title, String header, String content) {
        Alert a = new Alert(AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert a = new Alert(AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


