package com.shortestpath.ui;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import javafx.embed.swing.SwingNode;
import javafx.application.Platform;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;
import com.mxgraph.view.mxPerimeter;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.model.mxCell;

import com.shortestpath.model.WeightedGraph;
import com.shortestpath.model.Node;
import com.shortestpath.model.Edge;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.function.Consumer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.Cursor;

public class GraphRenderer {
    private final Map<String, Object> nodeIdToVertexMap;
    private final Map<String, Object> edgeIdToEdgeMap;
    private mxGraph mxGraph;
    private Consumer<String> nodeClickCallback;

    public GraphRenderer() {
        this.nodeIdToVertexMap = new HashMap<>();
        this.edgeIdToEdgeMap = new HashMap<>();
        this.nodeClickCallback = null;
    }

    public void renderGraph(WeightedGraph graph, SwingNode swingNode) {
        if (graph == null || swingNode == null) {
            throw new IllegalArgumentException("Graph and SwingNode must be non-null");
        }
        SwingUtilities.invokeLater(() -> {
            mxGraph = new mxGraph();
            mxGraph.setAllowDanglingEdges(false);
            mxGraph.setCellsEditable(false);
            mxGraph.setCellsResizable(false);
            mxGraph.setCellsMovable(true);

            applyDefaultStyles(mxGraph);

            Object parent = mxGraph.getDefaultParent();
            mxGraph.getModel().beginUpdate();
            try {
                createVertices(graph, parent);
                createEdges(graph, parent);
            } finally {
                mxGraph.getModel().endUpdate();
            }

            mxGraphComponent graphComponent = new mxGraphComponent(mxGraph);
            graphComponent.setConnectable(false);
            graphComponent.setZoomFactor(1.2);
            graphComponent.setPanning(true);

            // Mouse listeners for node selection and hover cursor
            graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                    if (cell != null && mxGraph.getModel().isVertex(cell)) {
                        String cellId = ((mxCell) cell).getId();
                        // Temporary highlight
                        String originalStyle = mxGraph.getModel().getStyle(cell);
                        String highlightStyle = "strokeColor=#FFC107;strokeWidth=4;" + (originalStyle == null ? "" : originalStyle);
                        mxGraph.getModel().beginUpdate();
                        try {
                            mxGraph.setCellStyle(highlightStyle, new Object[]{cell});
                        } finally {
                            mxGraph.getModel().endUpdate();
                            mxGraph.refresh();
                        }
                        Timer t = new Timer(500, evt -> {
                            mxGraph.getModel().beginUpdate();
                            try {
                                mxGraph.setCellStyle(originalStyle, new Object[]{cell});
                            } finally {
                                mxGraph.getModel().endUpdate();
                                mxGraph.refresh();
                            }
                        });
                        t.setRepeats(false);
                        t.start();

                        if (nodeClickCallback != null) {
                            String id = cellId;
                            try {
                                Platform.runLater(() -> {
                                    try { nodeClickCallback.accept(id); } catch (Exception ignored) {}
                                });
                            } catch (Exception ignored) { }
                        }
                    }
                }
            });

            graphComponent.getGraphControl().addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Object cell = graphComponent.getCellAt(e.getX(), e.getY());
                    if (cell != null && mxGraph.getModel().isVertex(cell)) {
                        graphComponent.getGraphControl().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        graphComponent.getGraphControl().setCursor(Cursor.getDefaultCursor());
                    }
                }
            });

            swingNode.setContent(graphComponent);
        });
    }

    private void applyDefaultStyles(mxGraph graph) {
        mxStylesheet stylesheet = graph.getStylesheet();

        Map<String, Object> vertexStyle = stylesheet.getDefaultVertexStyle();
        vertexStyle.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_ELLIPSE);
        vertexStyle.put(mxConstants.STYLE_PERIMETER, mxPerimeter.EllipsePerimeter);
        vertexStyle.put(mxConstants.STYLE_FILLCOLOR, "#E3F2FD");
        vertexStyle.put(mxConstants.STYLE_STROKECOLOR, "#1565C0");
        vertexStyle.put(mxConstants.STYLE_FONTCOLOR, "#000000");
        vertexStyle.put(mxConstants.STYLE_FONTSIZE, 14);
        vertexStyle.put(mxConstants.STYLE_FONTSTYLE, mxConstants.FONT_BOLD);

        Map<String, Object> edgeStyle = stylesheet.getDefaultEdgeStyle();
        edgeStyle.put(mxConstants.STYLE_STROKECOLOR, "#424242");
        edgeStyle.put(mxConstants.STYLE_STROKEWIDTH, 2);
        edgeStyle.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
        edgeStyle.put(mxConstants.STYLE_EDGE, mxEdgeStyle.EntityRelation);
        edgeStyle.put(mxConstants.STYLE_ROUNDED, true);
        edgeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#FFFFFF");
        edgeStyle.put(mxConstants.STYLE_FONTSIZE, 12);
    }

    private void createVertices(WeightedGraph graph, Object parent) {
        nodeIdToVertexMap.clear();
        for (Node node : graph.getAllNodes()) {
            double x = node.getX();
            double y = node.getY();
            String label = node.getLabel();
            Object vertex = mxGraph.insertVertex(parent, node.getId(), label, x, y, 60, 60);
            nodeIdToVertexMap.put(node.getId(), vertex);
        }
    }

    private void createEdges(WeightedGraph graph, Object parent) {
        edgeIdToEdgeMap.clear();
        for (Edge edge : graph.getAllEdges()) {
            Object sourceVertex = nodeIdToVertexMap.get(edge.getSource().getId());
            Object targetVertex = nodeIdToVertexMap.get(edge.getTarget().getId());
            if (sourceVertex == null || targetVertex == null) continue;
            String weightLabel = String.format("%.1f", edge.getWeight());
            Object mxE = mxGraph.insertEdge(parent, edge.getId(), weightLabel, sourceVertex, targetVertex);
            edgeIdToEdgeMap.put(edge.getId(), mxE);
        }
    }

    public Map<String, Object> getNodeIdToVertexMap() {
        return Collections.unmodifiableMap(nodeIdToVertexMap);
    }

    public Map<String, Object> getEdgeIdToEdgeMap() {
        return Collections.unmodifiableMap(edgeIdToEdgeMap);
    }

    public mxGraph getMxGraph() {
        return mxGraph;
    }

    public void setNodeClickCallback(Consumer<String> callback) {
        this.nodeClickCallback = callback;
    }
}
