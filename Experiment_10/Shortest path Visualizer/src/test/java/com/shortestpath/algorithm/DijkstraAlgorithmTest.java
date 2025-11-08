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

@DisplayName("DijkstraAlgorithm Tests")
class DijkstraAlgorithmTest {

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
        graph.addNode(A);
        graph.addNode(B);
        graph.addNode(C);
        graph.addEdge(A, B, 5.0);
        graph.addEdge(B, C, 3.0);

        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, A, C);
        assertNotNull(result);
        assertTrue(result.hasPath());
        assertEquals(8.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, B, C), result.getShortestPath());
        assertFalse(result.getSteps().isEmpty());
        assertTrue(result.getExecutionTimeMs() >= 0);
        assertTrue(result.getNodesVisited() <= 3 && result.getNodesVisited() >= 1);
    }

    @Test
    @DisplayName("Multiple paths: algorithm chooses the shortest one")
    void testMultiplePathsChoosesShortest() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        Node D = new Node("D", 3, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(B, D, 5.0); // path cost 6
        graph.addEdge(A, C, 2.0);
        graph.addEdge(C, D, 2.0); // path cost 4

        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, A, D);
        assertTrue(result.hasPath());
        assertEquals(4.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, C, D), result.getShortestPath());
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

        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, A, D);
        assertFalse(result.hasPath());
        assertEquals(Double.POSITIVE_INFINITY, result.getPathCost());
        assertTrue(result.getShortestPath().isEmpty());
    }

    @Test
    @DisplayName("Single node: source equals target")
    void testSourceEqualsTarget() {
        Node A = new Node("A", 0, 0);
        graph.addNode(A);

        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, A, A);
        assertTrue(result.hasPath());
        assertEquals(0.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A), result.getShortestPath());
    }

    @Test
    @DisplayName("Complex weighted graph shortest path correctness")
    void testComplexWeightedGraph() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        Node D = new Node("D", 3, 0);
        Node E = new Node("E", 4, 0);
        Node F = new Node("F", 5, 0);
        for (Node n : List.of(A,B,C,D,E,F)) graph.addNode(n);
        graph.addEdge(A,B,2.0);
        graph.addEdge(A,C,4.0);
        graph.addEdge(B,C,1.0);
        graph.addEdge(B,D,7.0);
        graph.addEdge(C,E,3.0);
        graph.addEdge(E,D,2.0);
        graph.addEdge(D,F,1.0);
        graph.addEdge(E,F,5.0);

        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, A, F);
        // Expected path: A -> B -> C -> E -> D -> F with cost 2+1+3+2+1 = 9
        assertTrue(result.hasPath());
        assertEquals(9.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A,B,C,E,D,F), result.getShortestPath());
        assertTrue(result.getStepCount() > 0);
    }

    @Test
    @DisplayName("Step-by-step tracking validates snapshots")
    void testStepByStepTracking() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C);
        graph.addEdge(A,B,1.0);
        graph.addEdge(B,C,1.0);

        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, A, C);
        assertNotNull(result.getSteps());
        assertTrue(result.getStepCount() > 0);
        AlgorithmStep first = result.getSteps().get(0);
        assertEquals(0, first.getStepNumber());
        assertNull(first.getCurrentNode());
        assertEquals(0.0, first.getDistance(A), 1e-9);
        for (int i = 1; i < result.getSteps().size(); i++) {
            AlgorithmStep s = result.getSteps().get(i);
            assertNotNull(s.getCurrentNode());
            assertTrue(s.getVisitedNodes().size() >= 1);
        }
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getVisitedNodes().contains(C)));
    }

    @Test
    @DisplayName("Negative weight behavior is not supported (documentation test)")
    void testNegativeWeightsNotSupported() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        graph.addNode(A); graph.addNode(B);
        graph.addEdge(A, B, -1.0);

        // We don't enforce validation here; result may be incorrect but algorithm should still run.
        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, A, B);
        assertNotNull(result);
        // Document limitation: do not assert correctness, only that it returns a result object
        assertNotNull(result.getSteps());
    }

    @Test
    @DisplayName("Validation: null graph throws")
    void testNullGraphThrowsException() {
        Node A = new Node("A", 0, 0);
        assertThrows(IllegalArgumentException.class, () -> DijkstraAlgorithm.findShortestPath(null, A, A));
    }

    @Test
    @DisplayName("Validation: null source throws")
    void testNullSourceThrowsException() {
        Node A = new Node("A", 0, 0);
        assertThrows(IllegalArgumentException.class, () -> DijkstraAlgorithm.findShortestPath(graph, null, A));
    }

    @Test
    @DisplayName("Validation: null target throws")
    void testNullTargetThrowsException() {
        Node A = new Node("A", 0, 0);
        assertThrows(IllegalArgumentException.class, () -> DijkstraAlgorithm.findShortestPath(graph, A, null));
    }

    @Test
    @DisplayName("Validation: source not in graph throws")
    void testSourceNotInGraph() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        graph.addNode(B);
        assertThrows(IllegalArgumentException.class, () -> DijkstraAlgorithm.findShortestPath(graph, A, B));
    }

    @Test
    @DisplayName("Large graph performance under time limit")
    void testLargeGraphPerformance() {
        int n = 100;
        List<Node> nodes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Node node = new Node("N" + i, i % 10, i / 10);
            nodes.add(node);
            graph.addNode(node);
        }
        Random rnd = new Random(42);
        // create ~300 edges
        for (int i = 0; i < 300; i++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v) continue;
            double w = 1 + rnd.nextInt(10);
            graph.addEdge(nodes.get(u), nodes.get(v), w);
        }
        long t0 = System.currentTimeMillis();
        AlgorithmResult result = DijkstraAlgorithm.findShortestPath(graph, nodes.get(0), nodes.get(n-1));
        long elapsed = System.currentTimeMillis() - t0;
        assertNotNull(result);
        assertTrue(elapsed < 1000, "Algorithm should finish under 1000ms, took " + elapsed + "ms");
    }

    // Helpers
    private static void assertPathEquals(List<Node> expected, List<Node> actual) {
        assertEquals(expected.size(), actual.size(), "Path length differs");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i), "Mismatch at index " + i);
        }
    }
}
