package com.shortestpath.algorithm;

import com.shortestpath.model.Node;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the result of running Dijkstra's algorithm including
 * the step-by-step history for visualization and the final shortest path.
 */
public final class AlgorithmResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<AlgorithmStep> steps;
    private final List<Node> shortestPath;
    private final double pathCost;
    private final Node sourceNode;
    private final Node targetNode;
    private final long executionTimeMs;
    private final int nodesVisited;

    public AlgorithmResult(
            List<AlgorithmStep> steps,
            List<Node> shortestPath,
            double pathCost,
            Node sourceNode,
            Node targetNode,
            long executionTimeMs,
            int nodesVisited
    ) {
        if (sourceNode == null) {
            throw new IllegalArgumentException("sourceNode must not be null");
        }
        if (targetNode == null) {
            throw new IllegalArgumentException("targetNode must not be null");
        }
        if (executionTimeMs < 0) {
            throw new IllegalArgumentException("executionTimeMs must be >= 0");
        }
        if (nodesVisited < 0) {
            throw new IllegalArgumentException("nodesVisited must be >= 0");
        }
        this.steps = Collections.unmodifiableList(steps == null ? List.of() : List.copyOf(steps));
        this.shortestPath = Collections.unmodifiableList(shortestPath == null ? List.of() : List.copyOf(shortestPath));
        this.pathCost = pathCost;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.executionTimeMs = executionTimeMs;
        this.nodesVisited = nodesVisited;
    }

    public List<AlgorithmStep> getSteps() { return steps; }

    public List<Node> getShortestPath() { return shortestPath; }

    public double getPathCost() { return pathCost; }

    public Node getSourceNode() { return sourceNode; }

    public Node getTargetNode() { return targetNode; }

    public long getExecutionTimeMs() { return executionTimeMs; }

    public int getNodesVisited() { return nodesVisited; }

    public boolean hasPath() { return !Double.isInfinite(pathCost); }

    public int getStepCount() { return steps.size(); }

    @Override
    public String toString() {
        String pathStr = shortestPath.stream().map(Objects::toString).reduce((a, b) -> a + " -> " + b).orElse("<no path>");
        return "Dijkstra Result: path=" + pathStr + ", cost=" + pathCost +
                ", steps=" + steps.size() + ", visited=" + nodesVisited +
                ", timeMs=" + executionTimeMs;
    }
}
