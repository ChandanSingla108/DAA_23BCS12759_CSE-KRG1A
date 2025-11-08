package com.shortestpath.algorithm;

import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BellmanFordAlgorithm Tests")
class BellmanFordAlgorithmTest {

    private WeightedGraph graph;

    @BeforeEach
    void setup() {
        graph = new WeightedGraph();
    }

    @Test
    @DisplayName("Simple Path Test: A -> B -> C with costs 5 and 3")
    void testSimplePathThreeNodes() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C);
        graph.addEdge(A, B, 5.0);
        graph.addEdge(B, C, 3.0);

        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, C);
        assertNotNull(result);
        assertTrue(result.hasPath());
        assertEquals(8.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, B, C), result.getShortestPath());
        assertFalse(result.getSteps().isEmpty());
        assertTrue(result.getExecutionTimeMs() >= 0);
        assertTrue(result.getNodesVisited() > 0);
    }

    @Test
    @DisplayName("Multiple paths: chooses the shortest one")
    void testMultiplePathsChoosesShortest() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        Node D = new Node("D", 3, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(B, D, 5.0); // 6
        graph.addEdge(A, C, 2.0);
        graph.addEdge(C, D, 2.0); // 4

        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, D);
        assertTrue(result.hasPath());
        assertEquals(4.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, C, D), result.getShortestPath());
    }

    @Test
    @DisplayName("Handles negative weight edges correctly")
    void testNegativeWeightPath() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        Node D = new Node("D", 3, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 5.0);
        graph.addEdge(B, C, -2.0);
        graph.addEdge(C, D, 3.0); // path cost 6
        graph.addEdge(A, D, 10.0);

        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, D);
        assertTrue(result.hasPath());
        assertEquals(6.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, B, C, D), result.getShortestPath());
    }

    @Test
    @DisplayName("Detects negative cycle and reports no path")
    void testNegativeCycleDetection() {
        WeightedGraph g = createGraphWithNegativeCycle();
        Node A = g.getNode("A");
        Node D = new Node("D", 3, 0);
        g.addNode(D);
        g.addEdge(g.getNode("C"), D, 1.0);

        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(g, A, D);
        assertFalse(result.hasPath());
        assertEquals(Double.POSITIVE_INFINITY, result.getPathCost());
        assertTrue(result.getShortestPath().isEmpty());
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getDescription().toLowerCase().contains("negative cycle")));
    }

    @Test
    @DisplayName("Disconnected graph: no path exists")
    void testNoPathExists() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        Node D = new Node("D", 3, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(C, D, 1.0);
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, D);
        assertFalse(result.hasPath());
        assertEquals(Double.POSITIVE_INFINITY, result.getPathCost());
        assertTrue(result.getShortestPath().isEmpty());
    }

    @Test
    @DisplayName("Single node: source equals target")
    void testSourceEqualsTarget() {
        Node A = new Node("A", 0, 0);
        graph.addNode(A);
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, A);
        assertTrue(result.hasPath());
        assertEquals(0.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A), result.getShortestPath());
    }

    @Test
    @DisplayName("Complex graph with negative edges (no negative cycles)")
    void testComplexGraphWithNegativeEdges() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        Node D = new Node("D", 3, 0);
        Node E = new Node("E", 4, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D); graph.addNode(E);
        graph.addEdge(A, B, 4.0);
        graph.addEdge(A, C, 2.0);
        graph.addEdge(C, B, -1.0);
        graph.addEdge(B, D, 2.0);
        graph.addEdge(C, D, 5.0);
        graph.addEdge(D, E, 1.0);
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, E);
        // A->C (2), C->B (-1), B->D (2), D->E (1) => 4
        assertTrue(result.hasPath());
        assertEquals(4.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, C, B, D, E), result.getShortestPath());
    }

    @Test
    @DisplayName("Iteration steps and descriptions are recorded")
    void testStepByStepTracking() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(B, C, 1.0);
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, C);
        assertTrue(result.getStepCount() > 0);
        assertTrue(result.getSteps().get(0).getDescription().toLowerCase().contains("initialized"));
        assertTrue(result.getSteps().stream().skip(1)
                .allMatch(s -> s.getDescription().toLowerCase().contains("iteration")));
        // distances should improve over steps
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getDistance(C) < Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("Zero weight edges are handled")
    void testZeroWeightEdges() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C);
        graph.addEdge(A, B, 0.0);
        graph.addEdge(B, C, 5.0);
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, C);
        assertTrue(result.hasPath());
        assertEquals(5.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, B, C), result.getShortestPath());
    }

    @Test
    @DisplayName("All negative weights without negative cycle")
    void testAllNegativeWeights() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C);
        graph.addEdge(A, B, -1.0);
        graph.addEdge(B, C, -2.0);
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, A, C);
        assertTrue(result.hasPath());
        assertEquals(-3.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, B, C), result.getShortestPath());
    }

    @Test
    @DisplayName("Validation: nulls and missing nodes throw")
    void testValidation() {
        Node A = new Node("A", 0, 0);
        assertThrows(IllegalArgumentException.class, () -> BellmanFordAlgorithm.findShortestPath(null, A, A));
        assertThrows(IllegalArgumentException.class, () -> BellmanFordAlgorithm.findShortestPath(graph, null, A));
        assertThrows(IllegalArgumentException.class, () -> BellmanFordAlgorithm.findShortestPath(graph, A, null));
        Node B = new Node("B", 1, 0);
        graph.addNode(B);
        assertThrows(IllegalArgumentException.class, () -> BellmanFordAlgorithm.findShortestPath(graph, A, B));
    }

    @Test
    @DisplayName("Large graph performance under time limit")
    void testLargeGraphPerformance() {
        int n = 50;
        List<Node> nodes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Node node = new Node("N" + i, i % 10, i / 10);
            nodes.add(node);
            graph.addNode(node);
        }
        Random rnd = new Random(7);
        for (int i = 0; i < 150; i++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v) continue;
            double w = rnd.nextBoolean() ? rnd.nextInt(10) : -rnd.nextInt(3);
            graph.addEdge(nodes.get(u), nodes.get(v), w);
        }
        long t0 = System.currentTimeMillis();
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(graph, nodes.get(0), nodes.get(n-1));
        long elapsed = System.currentTimeMillis() - t0;
        assertNotNull(result);
        assertTrue(elapsed < 2000, "Algorithm should finish under 2000ms, took " + elapsed + "ms");
    }

    @Test
    @DisplayName("Iteration count approximately V-1 plus initialization")
    void testIterationCount() {
        int N = 6;
        WeightedGraph g = createLinearGraph(N);
        Node s = g.getNode("N0");
        Node t = g.getNode("N" + (N-1));
        AlgorithmResult result = BellmanFordAlgorithm.findShortestPath(g, s, t);
        assertTrue(result.getStepCount() >= N - 1); // init + iterations (some may early break)
    }

    // Helpers
    private static void assertPathEquals(List<Node> expected, List<Node> actual) {
        assertEquals(expected.size(), actual.size(), "Path length differs");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), "Mismatch at index " + i);
        }
    }

    private static WeightedGraph createLinearGraph(int nodeCount) {
        WeightedGraph g = new WeightedGraph();
        List<Node> nodes = new ArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            Node n = new Node("N" + i, i, 0);
            nodes.add(n);
            g.addNode(n);
            if (i > 0) g.addEdge(nodes.get(i-1), n, 1.0);
        }
        return g;
    }

    private static WeightedGraph createGraphWithNegativeCycle() {
        WeightedGraph g = new WeightedGraph();
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        g.addNode(A); g.addNode(B); g.addNode(C);
        g.addEdge(A, B, 1.0);
        g.addEdge(B, C, 2.0);
        g.addEdge(C, A, -5.0); // negative cycle total -2
        return g;
    }
}
