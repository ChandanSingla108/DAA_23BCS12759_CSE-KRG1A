package com.shortestpath.algorithm;

import com.shortestpath.model.Edge;
import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;

import java.util.*;

/**
 * Bellman-Ford shortest path algorithm supporting negative edge weights.
 * Detects negative cycles and provides step-by-step snapshots for visualization.
 *
 * Time complexity: O(V * E)
 */
public final class BellmanFordAlgorithm {

    private BellmanFordAlgorithm() {}

    public static AlgorithmResult findShortestPath(WeightedGraph graph, Node source, Node target) {
        if (graph == null) throw new IllegalArgumentException("graph must not be null");
        if (source == null) throw new IllegalArgumentException("source must not be null");
        if (target == null) throw new IllegalArgumentException("target must not be null");
        if (!graph.containsNode(source.getId())) throw new IllegalArgumentException("source not in graph");
        if (!graph.containsNode(target.getId())) throw new IllegalArgumentException("target not in graph");

        long start = System.currentTimeMillis();

        Map<Node, Double> dist = new HashMap<>();
        Map<Node, Node> pred = new HashMap<>();
        List<AlgorithmStep> steps = new ArrayList<>();

        Collection<Node> nodes = graph.getAllNodes();
        List<Edge> edges = graph.getAllEdges();

        for (Node n : nodes) {
            dist.put(n, Double.POSITIVE_INFINITY);
            pred.put(n, null);
        }
        dist.put(source, 0.0);

        steps.add(createStepSnapshot(0, dist, pred,
                "Initialized source node " + source + " with distance 0"));

        int step = 1;
        int nodesVisited = 0; // approximation: count nodes that ever become finite

        // V - 1 iterations
        int V = nodes.size();
        for (int i = 1; i <= V - 1; i++) {
            boolean updated = false;
            int updatesThisIter = 0;
            for (Edge e : edges) {
                Node u = e.getSource();
                Node v = e.getTarget();
                double w = e.getWeight();
                double du = dist.get(u);
                if (!Double.isInfinite(du)) {
                    double alt = du + w;
                    if (alt < dist.get(v)) {
                        dist.put(v, alt);
                        pred.put(v, u);
                        updated = true;
                        updatesThisIter++;
                    }
                }
            }
            // Track nodes visited (finite distance) after this iteration
            nodesVisited = countNodesWithFiniteDistance(dist);
            steps.add(createStepSnapshot(step++, dist, pred,
                    "Iteration " + i + ": Relaxed edges, updated " + updatesThisIter + " distances"));
        }

        // Negative cycle detection
        boolean negativeCycle = false;
        for (Edge e : edges) {
            Node u = e.getSource();
            Node v = e.getTarget();
            double w = e.getWeight();
            double du = dist.get(u);
            if (!Double.isInfinite(du) && du + w < dist.get(v)) {
                negativeCycle = true;
                break;
            }
        }
        if (negativeCycle) {
            steps.add(createStepSnapshot(step, dist, pred, "Negative cycle detected"));
            long timeMs = System.currentTimeMillis() - start;
            return new AlgorithmResult(steps, List.of(), Double.POSITIVE_INFINITY,
                    source, target, timeMs, nodesVisited);
        }

        List<Node> path = reconstructPath(pred, source, target);
        double cost = dist.getOrDefault(target, Double.POSITIVE_INFINITY);
        long timeMs = System.currentTimeMillis() - start;
        return new AlgorithmResult(steps, path,
                (path.isEmpty() && !source.equals(target)) ? Double.POSITIVE_INFINITY : cost,
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

    private static AlgorithmStep createStepSnapshot(int stepNumber,
                                                    Map<Node, Double> distances,
                                                    Map<Node, Node> predecessors,
                                                    String description) {
        // Bellman-Ford does not have a current node or PQ; visited can be nodes with finite distance
        Set<Node> visited = new HashSet<>();
        for (Map.Entry<Node, Double> e : distances.entrySet()) {
            if (!Double.isInfinite(e.getValue())) visited.add(e.getKey());
        }
        return new AlgorithmStep(
                stepNumber,
                null,
                visited,
                new HashMap<>(distances),
                new HashMap<>(predecessors),
                List.of(),
                description
        );
    }

    private static int countNodesWithFiniteDistance(Map<Node, Double> distances) {
        int cnt = 0;
        for (double d : distances.values()) {
            if (!Double.isInfinite(d)) cnt++;
        }
        return cnt;
    }
}
