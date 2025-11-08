package com.shortestpath.util;

import com.shortestpath.model.Edge;
import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class RandomGraphGenerator {
    private RandomGraphGenerator() {}

    public static WeightedGraph generateRandomGraph(int nodeCount,
                                                    double edgeDensity,
                                                    double minWeight,
                                                    double maxWeight,
                                                    boolean directed) {
        validateParameters(nodeCount, edgeDensity, minWeight, maxWeight);

        Random random = new Random();
        WeightedGraph graph = new WeightedGraph(directed);

        // 1) Create nodes positioned on a circle
        List<Node> nodes = new ArrayList<>(nodeCount);
        double centerX = 400.0;
        double centerY = 400.0; // keep y >= 0
        double radius = 300.0;  // ensure non-negative coordinates
        for (int i = 0; i < nodeCount; i++) {
            double angle = 2.0 * Math.PI * i / Math.max(1, nodeCount);
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Node n = new Node("N" + i, x, y, "N" + i);
            graph.addNode(n);
            nodes.add(n);
        }

        if (nodeCount <= 1) {
            return graph; // single node, no edges
        }

        // Helper set to track existing conceptual edges (avoid duplicates)
        Set<String> edgeSet = new HashSet<>();
        
        // 2) Ensure connectivity via a simple linear chain to guarantee reachability from N0 in directed graphs
        for (int i = 1; i < nodeCount; i++) {
            int parentIdx = i - 1;
            int childIdx = i;
            Node parent = nodes.get(parentIdx);
            Node child = nodes.get(childIdx);
            double w = generateRandomWeight(random, minWeight, maxWeight);
            graph.addEdge(parent, child, w); // parent -> child
            markEdge(edgeSet, parentIdx, childIdx, directed);
        }

        // 3) Add additional random edges according to density
        long maxEdgesConceptual = directed
                ? (long) nodeCount * (nodeCount - 1)
                : (long) nodeCount * (nodeCount - 1) / 2;
        long targetEdgesConceptual = (long) Math.floor(maxEdgesConceptual * edgeDensity);
        long currentConceptual = nodeCount - 1; // spanning tree edges added conceptually
        long additionalNeeded = Math.max(0, targetEdgesConceptual - currentConceptual);

        // limit attempts to avoid infinite loops at high density
        long attempts = 0;
        long maxAttempts = Math.max(1000L, additionalNeeded * 1000L);
        while (additionalNeeded > 0 && attempts < maxAttempts) {
            attempts++;
            int a = random.nextInt(nodeCount);
            int b = random.nextInt(nodeCount);
            if (a == b) continue;
            if (edgeExists(edgeSet, a, b, directed)) continue;

            Node src = nodes.get(a);
            Node dst = nodes.get(b);
            if (graph.getEdge(src, dst) != null) { // double-check in graph
                markEdge(edgeSet, a, b, directed);
                continue;
            }
            double w = generateRandomWeight(random, minWeight, maxWeight);
            graph.addEdge(src, dst, w);
            markEdge(edgeSet, a, b, directed);
            additionalNeeded--;
        }

        return graph;
    }

    private static void validateParameters(int nodeCount,
                                           double edgeDensity,
                                           double minWeight,
                                           double maxWeight) {
        if (nodeCount < 1) {
            throw new IllegalArgumentException("nodeCount must be >= 1");
        }
        if (Double.isNaN(edgeDensity) || edgeDensity < 0.0 || edgeDensity > 1.0) {
            throw new IllegalArgumentException("edgeDensity must be in [0.0, 1.0]");
        }
        if (Double.isNaN(minWeight) || Double.isInfinite(minWeight)) {
            throw new IllegalArgumentException("minWeight must be finite");
        }
        if (Double.isNaN(maxWeight) || Double.isInfinite(maxWeight)) {
            throw new IllegalArgumentException("maxWeight must be finite");
        }
        if (maxWeight < minWeight) {
            throw new IllegalArgumentException("maxWeight must be >= minWeight");
        }
    }

    private static double generateRandomWeight(Random random, double minWeight, double maxWeight) {
        return minWeight + random.nextDouble() * (maxWeight - minWeight);
    }

    private static boolean edgeExists(Set<String> set, int a, int b, boolean directed) {
        if (directed) {
            return set.contains(a + ":" + b);
        } else {
            int u = Math.min(a, b);
            int v = Math.max(a, b);
            return set.contains(u + ":" + v);
        }
    }

    private static void markEdge(Set<String> set, int a, int b, boolean directed) {
        if (directed) {
            set.add(a + ":" + b);
        } else {
            int u = Math.min(a, b);
            int v = Math.max(a, b);
            set.add(u + ":" + v);
        }
    }
}
