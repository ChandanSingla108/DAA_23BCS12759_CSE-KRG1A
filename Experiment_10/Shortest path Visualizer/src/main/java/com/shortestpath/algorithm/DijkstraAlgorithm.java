package com.shortestpath.algorithm;

import com.shortestpath.model.Edge;
import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;

import java.util.*;

/**
 * Dijkstra's algorithm for shortest paths on graphs with non-negative edge weights.
 * Provides detailed step-by-step snapshots for visualization.
 */
public final class DijkstraAlgorithm {

    private DijkstraAlgorithm() {}

    public static AlgorithmResult findShortestPath(WeightedGraph graph, Node source, Node target) {
        if (graph == null) throw new IllegalArgumentException("graph must not be null");
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (!graph.containsNode(source.getId())) throw new IllegalArgumentException("source not in graph");
        if (!graph.containsNode(target.getId())) throw new IllegalArgumentException("target not in graph");

        long start = System.currentTimeMillis();

        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> predecessors = new HashMap<>();
        Set<Node> visited = new HashSet<>();

        for (Node n : graph.getAllNodes()) {
            distances.put(n, Double.POSITIVE_INFINITY);
            predecessors.put(n, null);
        }
        distances.put(source, 0.0);

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
        pq.add(source);

        List<AlgorithmStep> steps = new ArrayList<>();
        steps.add(createStepSnapshot(
                0,
                null,
                visited,
                distances,
                predecessors,
                pq,
                "Initialized source node " + source + " with distance 0"
        ));

        int step = 1;
        int nodesVisited = 0;

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            nodesVisited++;

            if (current.equals(target)) {
                steps.add(createStepSnapshot(step++, current, visited, distances, predecessors, pq,
                        "Reached target " + target + ". Early exit."));
                break;
            }

            List<Node> updated = new ArrayList<>();
            for (Edge e : graph.getOutgoingEdges(current)) {
                Node neighbor = e.getTarget();
                double weight = e.getWeight();
                double alt = distances.get(current) + weight;
                if (alt < distances.get(neighbor)) {
                    distances.put(neighbor, alt);
                    predecessors.put(neighbor, current);
                    updated.add(neighbor);
                    pq.add(neighbor);
                }
            }

            String desc = formatDescription(current, updated);
            steps.add(createStepSnapshot(step++, current, visited, distances, predecessors, pq, desc));
        }

        List<Node> path = reconstructPath(predecessors, source, target);
        double cost = distances.getOrDefault(target, Double.POSITIVE_INFINITY);
        long timeMs = System.currentTimeMillis() - start;

        return new AlgorithmResult(steps, path, path.isEmpty() && !source.equals(target) ? Double.POSITIVE_INFINITY : cost,
                source, target, timeMs, nodesVisited);
    }

    private static List<Node> reconstructPath(Map<Node, Node> predecessors, Node source, Node target) {
        List<Node> path = new ArrayList<>();
        if (source.equals(target)) {
            path.add(source);
            return path;
        }
        Node curr = target;
        if (predecessors.get(curr) == null) {
            return path; // empty, no path
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
            Set<Node> visited,
            Map<Node, Double> distances,
            Map<Node, Node> predecessors,
            PriorityQueue<Node> pq,
            String description
    ) {
        // Snapshot PQ ordered by current distances
        List<Node> pqSnapshot = new ArrayList<>(pq);
        pqSnapshot.sort(Comparator.comparingDouble(distances::get));
        return new AlgorithmStep(
                stepNumber,
                current,
                new HashSet<>(visited),
                new HashMap<>(distances),
                new HashMap<>(predecessors),
                pqSnapshot,
                description
        );
    }

    private static String formatDescription(Node current, List<Node> updatedNeighbors) {
        if (updatedNeighbors.isEmpty()) {
            return "Visiting node " + current + ", no updates";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Visiting node ").append(current).append(", updated neighbors: ");
        for (int i = 0; i < updatedNeighbors.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(updatedNeighbors.get(i));
        }
        return sb.toString();
    }
}
