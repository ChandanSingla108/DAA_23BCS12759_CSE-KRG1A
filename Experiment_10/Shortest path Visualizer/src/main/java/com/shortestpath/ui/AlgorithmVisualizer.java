package com.shortestpath.ui;

import javax.swing.SwingUtilities;

import com.mxgraph.view.mxGraph;
import com.shortestpath.algorithm.AlgorithmResult;
import com.shortestpath.algorithm.AlgorithmStep;
import com.shortestpath.model.Edge;
import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;

import java.util.List;
import java.util.Map;

public final class AlgorithmVisualizer {
    private final GraphRenderer graphRenderer;
    private WeightedGraph currentGraph;
    private AlgorithmResult currentResult;

    // Color/style constants
    private static final String COLOR_UNVISITED_NODE = "#E3F2FD";
    private static final String COLOR_VISITED_NODE = "#C8E6C9";
    private static final String COLOR_CURRENT_NODE = "#FFF59D";
    private static final String COLOR_SOURCE_NODE = "#81C784";
    private static final String COLOR_TARGET_NODE = "#EF5350";
    private static final String COLOR_PATH_EDGE = "#4CAF50";
    private static final String COLOR_DEFAULT_EDGE = "#424242";
    private static final int STROKE_WIDTH_PATH = 4;
    private static final int STROKE_WIDTH_DEFAULT = 2;

    public AlgorithmVisualizer(GraphRenderer renderer, WeightedGraph graph) {
        if (renderer == null) throw new IllegalArgumentException("renderer must not be null");
        if (graph == null) throw new IllegalArgumentException("graph must not be null");
        this.graphRenderer = renderer;
        this.currentGraph = graph;
        this.currentResult = null;
    }

    public void visualizeStep(AlgorithmStep step) {
        SwingUtilities.invokeLater(() -> {
            mxGraph mx = graphRenderer.getMxGraph();
            if (mx == null) return;
            mx.getModel().beginUpdate();
            try {
                updateNodeStyles(mx, step);
                updateEdgeStyles(mx);
            } finally {
                mx.getModel().endUpdate();
                mx.refresh();
            }
        });
    }

    public void loadAlgorithmResult(AlgorithmResult result) {
        if (result == null) throw new IllegalArgumentException("result must not be null");
        this.currentResult = result;
    }

    public void reset() {
        this.currentResult = null;
        SwingUtilities.invokeLater(() -> {
            mxGraph mx = graphRenderer.getMxGraph();
            if (mx == null) return;
            mx.getModel().beginUpdate();
            try {
                // Reset vertices
                Map<String, Object> vMap = graphRenderer.getNodeIdToVertexMap();
                for (Object v : vMap.values()) {
                    String style = "shape=ellipse;fillColor=" + COLOR_UNVISITED_NODE + 
                            ";strokeColor=#1565C0;fontColor=#000000;fontSize=14;fontStyle=1";
                    mx.setCellStyle(style, new Object[]{v});
                }
                // Reset edges
                Map<String, Object> eMap = graphRenderer.getEdgeIdToEdgeMap();
                String edgeStyle = "strokeColor=" + COLOR_DEFAULT_EDGE + 
                        ";strokeWidth=" + STROKE_WIDTH_DEFAULT + 
                        ";endArrow=classic;rounded=1;labelBackgroundColor=#FFFFFF;fontSize=12";
                for (Object e : eMap.values()) {
                    mx.setCellStyle(edgeStyle, new Object[]{e});
                }
            } finally {
                mx.getModel().endUpdate();
                mx.refresh();
            }
        });
    }

    public void updateGraph(WeightedGraph newGraph) {
        if (newGraph == null) throw new IllegalArgumentException("newGraph must not be null");
        this.currentGraph = newGraph;
        this.currentResult = null;
        reset();
    }

    private void updateNodeStyles(mxGraph graph, AlgorithmStep step) {
        Map<String, Object> vertexMap = graphRenderer.getNodeIdToVertexMap();
        Node source = currentResult != null ? currentResult.getSourceNode() : null;
        Node target = currentResult != null ? currentResult.getTargetNode() : null;
        Node current = (step == null) ? null : step.getCurrentNode();

        for (Node n : currentGraph.getAllNodes()) {
            Object v = vertexMap.get(n.getId());
            if (v == null) continue;

            String fill;
            if (source != null && n.equals(source)) {
                fill = COLOR_SOURCE_NODE;
            } else if (target != null && n.equals(target)) {
                fill = COLOR_TARGET_NODE;
            } else if (current != null && n.equals(current)) {
                fill = COLOR_CURRENT_NODE;
            } else if (step != null && step.isVisited(n)) {
                fill = COLOR_VISITED_NODE;
            } else {
                fill = COLOR_UNVISITED_NODE;
            }

            String style = "shape=ellipse;fillColor=" + fill +
                    ";strokeColor=#1565C0;fontColor=#000000;fontSize=14;fontStyle=1";
            graph.setCellStyle(style, new Object[]{v});
        }
    }

    private void updateEdgeStyles(mxGraph graph) {
        Map<String, Object> edgeMap = graphRenderer.getEdgeIdToEdgeMap();
        // Reset all edges to default
        String defaultEdgeStyle = "strokeColor=" + COLOR_DEFAULT_EDGE + 
                ";strokeWidth=" + STROKE_WIDTH_DEFAULT + 
                ";endArrow=classic;rounded=1;labelBackgroundColor=#FFFFFF;fontSize=12";
        for (Object e : edgeMap.values()) {
            graph.setCellStyle(defaultEdgeStyle, new Object[]{e});
        }

        if (currentResult == null || !currentResult.hasPath()) return;
        List<Node> path = currentResult.getShortestPath();
        if (path == null || path.size() < 2) return;

        String pathStyle = "strokeColor=" + COLOR_PATH_EDGE + 
                ";strokeWidth=" + STROKE_WIDTH_PATH + 
                ";endArrow=classic;rounded=1;labelBackgroundColor=#FFFFFF;fontSize=12";

        for (int i = 0; i < path.size() - 1; i++) {
            Node a = path.get(i);
            Node b = path.get(i + 1);
            Edge edge = currentGraph.getEdge(a, b);
            if (edge == null) continue;
            Object mxE = edgeMap.get(edge.getId());
            if (mxE != null) {
                graph.setCellStyle(pathStyle, new Object[]{mxE});
            }
        }
    }
}
