package com.shortestpath.algorithm;

import com.shortestpath.model.Edge;
import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;

import java.util.*;

/**
 * A* shortest path algorithm using Euclidean distance heuristic based on node coordinates.
 * Requires non-negative edge weights.
 * Provides step-by-step snapshots for visualization.
 */
public final class AStarAlgorithm {

    private AStarAlgorithm() {}

    public static AlgorithmResult findShortestPath(WeightedGraph graph, Node source, Node target) {
        if (graph == null) throw new IllegalArgumentException("graph must not be null");
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (!graph.containsNode(source.getId())) throw new IllegalArgumentException("source not in graph");
        if (!graph.containsNode(target.getId())) throw new IllegalArgumentException("target not in graph");

        long start = System.currentTimeMillis();

        Map<Node, Double> gScore = new HashMap<>();
        Map<Node, Double> fScore = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        Set<Node> closedSet = new HashSet<>();

        for (Node n : graph.getAllNodes()) {
            gScore.put(n, Double.POSITIVE_INFINITY);
            fScore.put(n, Double.POSITIVE_INFINITY);
            predecessors.put(n, null);
        }
        double h0 = calculateHeuristic(source, target);
        gScore.put(source, 0.0);
        fScore.put(source, h0);

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));
        openSet.add(source);

        List<AlgorithmStep> steps = new ArrayList<>();
        steps.add(createStepSnapshot(0, null, closedSet, gScore, predecessors, openSet, fScore,
                "Initialized source node " + source + " with g=0, h=" + h0 + ", f=" + h0));

        int step = 1;
        int nodesVisited = 0;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (closedSet.contains(current)) {
                continue;
            }
            if (current.equals(target)) {
                double g = gScore.get(current);
                double h = calculateHeuristic(current, target);
                double f = fScore.get(current);
                steps.add(createStepSnapshot(step++, current, closedSet, gScore, predecessors, openSet, fScore,
                        formatDescription(current, g, h, f, List.of())));
                break; // path found
            }
            closedSet.add(current);
            nodesVisited++;

            List<Node> updated = new ArrayList<>();
            for (Edge e : graph.getOutgoingEdges(current)) {
                Node neighbor = e.getTarget();
                if (closedSet.contains(neighbor)) continue;
                double tentativeG = gScore.get(current) + e.getWeight();
                if (tentativeG < gScore.get(neighbor)) {
                    gScore.put(neighbor, tentativeG);
                    double h = calculateHeuristic(neighbor, target);
                    double f = tentativeG + h;
                    fScore.put(neighbor, f);
                    predecessors.put(neighbor, current);
                    openSet.add(neighbor);
                    updated.add(neighbor);
                }
            }
            double g = gScore.get(current);
            double h = calculateHeuristic(current, target);
            double f = fScore.get(current);
            steps.add(createStepSnapshot(step++, current, closedSet, gScore, predecessors, openSet, fScore,
                    formatDescription(current, g, h, f, updated)));
        }

        List<Node> path = reconstructPath(predecessors, source, target);
        double cost = gScore.getOrDefault(target, Double.POSITIVE_INFINITY);
        long timeMs = System.currentTimeMillis() - start;
        return new AlgorithmResult(steps, path,
                (path.isEmpty() && !source.equals(target)) ? Double.POSITIVE_INFINITY : cost,
                source, target, timeMs, nodesVisited);
    }

    private static double calculateHeuristic(Node from, Node to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static List<Node> reconstructPath(Map<Node, Node> predecessors, Node source, Node target) {
        List<Node> path = new ArrayList<>();
        if (source.equals(target)) {
            path.add(source);
            return path;
        }
        Node curr = target;
        if (predecessors.get(curr) == null) {
            return path; // empty
        }
        while (curr != null) {
            path.add(curr);
            if (curr.equals(source)) break;
            curr = predecessors.get(curr);
        }
        if (!path.isEmpty() && path.get(path.size() - 1).equals(source)) {
            Collections.reverse(path);
            return path;
        }
        return List.of();
    }

    private static AlgorithmStep createStepSnapshot(
            int stepNumber,
            Node current,
            Set<Node> closedSet,
            Map<Node, Double> gScore,
            Map<Node, Node> predecessors,
            PriorityQueue<Node> openSet,
            Map<Node, Double> fScore,
            String description
    ) {
        // Snapshot openSet ordered by current fScore
        List<Node> pqSnapshot = new ArrayList<>(openSet);
        pqSnapshot.sort(Comparator.comparingDouble(fScore::get));
        return new AlgorithmStep(
                stepNumber,
                current,
                new HashSet<>(closedSet),
                new HashMap<>(gScore),
                new HashMap<>(predecessors),
                pqSnapshot,
                description
        );
    }

    private static String formatDescription(Node current, double g, double h, double f, List<Node> updatedNeighbors) {
        StringBuilder sb = new StringBuilder();
        sb.append("Visiting node ").append(current)
                .append(" (g=").append(g)
                .append(", h=").append(h)
                .append(", f=").append(f)
                .append(")");
        if (!updatedNeighbors.isEmpty()) {
            sb.append(", updated neighbors: ");
            for (int i = 0; i < updatedNeighbors.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(updatedNeighbors.get(i));
            }
        }
        return sb.toString();
    }
}
