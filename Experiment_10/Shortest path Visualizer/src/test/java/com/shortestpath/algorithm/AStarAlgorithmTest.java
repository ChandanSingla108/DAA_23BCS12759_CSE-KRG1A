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

@DisplayName("AStarAlgorithm Tests")
class AStarAlgorithmTest {

    private WeightedGraph graph;

    @BeforeEach
    void setup() {
        graph = new WeightedGraph();
    }

    @Test
    @DisplayName("Simple Path Test with coordinates: A -> B -> C")
    void testSimplePathThreeNodes() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C);
        graph.addEdge(A, B, 5.0);
        graph.addEdge(B, C, 3.0);

        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, C);
        assertNotNull(result);
        assertTrue(result.hasPath());
        assertEquals(8.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, B, C), result.getShortestPath());
        assertFalse(result.getSteps().isEmpty());
        assertTrue(result.getExecutionTimeMs() >= 0);
    }

    @Test
    @DisplayName("Multiple paths with heuristic guidance")
    void testMultiplePathsChoosesShortest() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 0, 1);
        Node D = new Node("D", 1, 1);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(B, D, 5.0); // 6
        graph.addEdge(A, C, 2.0);
        graph.addEdge(C, D, 2.0); // 4
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, D);
        assertTrue(result.hasPath());
        assertEquals(4.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, C, D), result.getShortestPath());
    }

    @Test
    @DisplayName("Disconnected graph: no path exists")
    void testNoPathExists() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 5, 5);
        Node D = new Node("D", 6, 5);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(C, D, 1.0);
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, D);
        assertFalse(result.hasPath());
        assertEquals(Double.POSITIVE_INFINITY, result.getPathCost());
        assertTrue(result.getShortestPath().isEmpty());
    }

    @Test
    @DisplayName("Single node: source equals target")
    void testSourceEqualsTarget() {
        Node A = new Node("A", 0, 0);
        graph.addNode(A);
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, A);
        assertTrue(result.hasPath());
        assertEquals(0.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A), result.getShortestPath());
    }

    @Test
    @DisplayName("Complex weighted graph correctness")
    void testComplexWeightedGraph() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 2, 0);
        Node C = new Node("C", 1, 1);
        Node D = new Node("D", 3, 1);
        Node E = new Node("E", 4, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D); graph.addNode(E);
        graph.addEdge(A, C, 1.5);
        graph.addEdge(C, B, 1.5);
        graph.addEdge(B, D, 1.0);
        graph.addEdge(D, E, 1.0);
        graph.addEdge(A, B, 10.0); // decoy edge
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, E);
        assertTrue(result.hasPath());
        assertEquals(5.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, C, B, D, E), result.getShortestPath());
    }

    @Test
    @DisplayName("Step tracking includes g,h,f values and visited growth")
    void testStepByStepTracking() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 1, 0);
        Node C = new Node("C", 2, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(B, C, 1.0);
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, C);
        assertTrue(result.getStepCount() > 0);
        assertTrue(result.getSteps().get(0).getDescription().toLowerCase().contains("initialized"));
        // Each subsequent step should have a current node
        for (int i = 1; i < result.getSteps().size(); i++) {
            assertNotNull(result.getSteps().get(i).getCurrentNode());
        }
        // closed set grows
        int maxVisited = result.getSteps().stream().mapToInt(s -> s.getVisitedNodes().size()).max().orElse(0);
        assertTrue(maxVisited >= 2);
    }

    @Test
    @DisplayName("Zero heuristic behaves like Dijkstra when all nodes share coordinates")
    void testZeroHeuristicBehavesLikeDijkstra() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 0, 0);
        Node C = new Node("C", 0, 0);
        Node D = new Node("D", 0, 0);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 1.0);
        graph.addEdge(B, D, 5.0); // 6
        graph.addEdge(A, C, 2.0);
        graph.addEdge(C, D, 2.0); // 4
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, D);
        assertTrue(result.hasPath());
        assertEquals(4.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, C, D), result.getShortestPath());
    }

    @Test
    @DisplayName("Obstacle avoidance: avoids high-cost direct edge")
    void testObstacleAvoidance() {
        Node A = new Node("A", 0, 0);
        Node B = new Node("B", 5, 0);
        Node C = new Node("C", 2, 2);
        Node D = new Node("D", 3, 2);
        graph.addNode(A); graph.addNode(B); graph.addNode(C); graph.addNode(D);
        graph.addEdge(A, B, 100.0);
        graph.addEdge(A, C, 3.0);
        graph.addEdge(C, D, 3.0);
        graph.addEdge(D, B, 4.0); // total 10
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, A, B);
        assertTrue(result.hasPath());
        assertEquals(10.0, result.getPathCost(), 1e-9);
        assertPathEquals(List.of(A, C, D, B), result.getShortestPath());
    }

    @Test
    @DisplayName("Validation: nulls and missing nodes throw")
    void testValidation() {
        Node A = new Node("A", 0, 0);
        assertThrows(IllegalArgumentException.class, () -> AStarAlgorithm.findShortestPath(null, A, A));
        assertThrows(IllegalArgumentException.class, () -> AStarAlgorithm.findShortestPath(graph, null, A));
        assertThrows(IllegalArgumentException.class, () -> AStarAlgorithm.findShortestPath(graph, A, null));
        Node B = new Node("B", 1, 0);
        graph.addNode(B);
        assertThrows(IllegalArgumentException.class, () -> AStarAlgorithm.findShortestPath(graph, A, B));
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
        Random rnd = new Random(123);
        for (int i = 0; i < 300; i++) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v) continue;
            double w = 1 + rnd.nextInt(10);
            graph.addEdge(nodes.get(u), nodes.get(v), w);
        }
        long t0 = System.currentTimeMillis();
        AlgorithmResult result = AStarAlgorithm.findShortestPath(graph, nodes.get(0), nodes.get(n-1));
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
