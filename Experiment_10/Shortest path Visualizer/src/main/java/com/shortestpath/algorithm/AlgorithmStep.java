package com.shortestpath.algorithm;

import com.shortestpath.model.Node;

import java.io.Serializable;
import java.util.*;

/**
 * Immutable snapshot of Dijkstra's algorithm state at a given step.
 */
public final class AlgorithmStep implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int stepNumber;
    private final Node currentNode;
    private final Set<Node> visitedNodes;
    private final Map<Node, Double> distances;
    private final Map<Node, Node> predecessors;
    private final List<Node> priorityQueue;
    private final String description;

    public AlgorithmStep(
            int stepNumber,
            Node currentNode,
            Set<Node> visitedNodes,
            Map<Node, Double> distances,
            Map<Node, Node> predecessors,
            List<Node> priorityQueue,
            String description
    ) {
        if (stepNumber < 0) {
            throw new IllegalArgumentException("stepNumber must be >= 0");
        }
        this.stepNumber = stepNumber;
        this.currentNode = currentNode;

        // Defensive copies and unmodifiable wrappers
        Set<Node> visitedCopy = visitedNodes == null ? new HashSet<>() : new HashSet<>(visitedNodes);
        Map<Node, Double> distancesCopy = distances == null ? new HashMap<>() : new HashMap<>(distances);
        Map<Node, Node> predecessorsCopy = predecessors == null ? new HashMap<>() : new HashMap<>(predecessors);
        List<Node> pqCopy = priorityQueue == null ? new ArrayList<>() : new ArrayList<>(priorityQueue);

        this.visitedNodes = Collections.unmodifiableSet(visitedCopy);
        this.distances = Collections.unmodifiableMap(distancesCopy);
        this.predecessors = Collections.unmodifiableMap(predecessorsCopy);
        this.priorityQueue = Collections.unmodifiableList(pqCopy);
        this.description = description == null ? "" : description;
    }

    public int getStepNumber() { return stepNumber; }

    public Node getCurrentNode() { return currentNode; }

    public Set<Node> getVisitedNodes() { return visitedNodes; }

    public Map<Node, Double> getDistances() { return distances; }

    public Map<Node, Node> getPredecessors() { return predecessors; }

    public List<Node> getPriorityQueue() { return priorityQueue; }

    public String getDescription() { return description; }

    public double getDistance(Node node) {
        Double d = distances.get(node);
        return d == null ? Double.POSITIVE_INFINITY : d;
    }

    public Node getPredecessor(Node node) {
        return predecessors.get(node);
    }

    public boolean isVisited(Node node) {
        return visitedNodes.contains(node);
    }

    @Override
    public String toString() {
        return "Step " + stepNumber + ": " + description;
    }
}
