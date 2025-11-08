package com.shortestpath.util;

import com.shortestpath.model.Node;
import com.shortestpath.model.WeightedGraph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RandomGraphGenerator Tests")
public class RandomGraphGeneratorTest {

    @Test
    void testGenerateBasicGraph() {
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(10, 0.3, 1.0, 10.0, true);
        assertNotNull(g);
        assertEquals(10, g.getNodeCount());
        assertTrue(g.getEdgeCount() > 0);
        for (Node n : g.getAllNodes()) {
            assertTrue(Double.isFinite(n.getX()));
            assertTrue(Double.isFinite(n.getY()));
        }
        g.getAllEdges().forEach(e -> {
            assertTrue(e.getWeight() >= 1.0 && e.getWeight() <= 10.0);
        });
    }

    @Test
    void testGeneratedGraphIsConnected() {
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(20, 0.2, 1.0, 5.0, true);
        assertTrue(isGraphConnected(g));
    }

    @Test
    void testMinimumDensityCreatesSpanningTree() {
        int n = 15;
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(n, 0.0, 1.0, 10.0, true);
        assertEquals(n, g.getNodeCount());
        assertEquals(n - 1, g.getEdgeCount());
        assertTrue(isGraphConnected(g));
    }

    @Test
    void testHighDensityCreatesMoreEdges() {
        int n = 10;
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(n, 0.8, 1.0, 10.0, true);
        assertTrue(g.getEdgeCount() > (n - 1));
        int max = n * (n - 1);
        assertTrue(g.getEdgeCount() <= max);
    }

    @Test
    void testWeightsAreWithinSpecifiedRange() {
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(20, 0.3, 5.0, 15.0, true);
        g.getAllEdges().forEach(e -> {
            assertTrue(e.getWeight() >= 5.0 && e.getWeight() <= 15.0);
        });
    }

    @Test
    void testNodesHaveValidCoordinates() {
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(15, 0.3, 1.0, 10.0, true);
        Set<String> positions = new HashSet<>();
        for (Node n : g.getAllNodes()) {
            assertTrue(Double.isFinite(n.getX()));
            assertTrue(Double.isFinite(n.getY()));
            assertTrue(n.getX() >= 0 && n.getY() >= 0);
            positions.add(n.getX() + "," + n.getY());
        }
        assertTrue(positions.size() > 1);
    }

    @Test
    void testSingleNodeGraph() {
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(1, 0.5, 1.0, 10.0, true);
        assertEquals(1, g.getNodeCount());
        assertEquals(0, g.getEdgeCount());
    }

    @Test
    void testUndirectedGraphGeneration() {
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(10, 0.3, 1.0, 10.0, false);
        assertFalse(g.isDirected());
        assertTrue(isGraphConnected(g));
        // Ensure count does not exceed theoretical maximum of actual stored edges (includes reverse edges)
        int n = g.getNodeCount();
        int maxActual = n * (n - 1); // because undirected adds reverse automatically
        assertTrue(g.getEdgeCount() <= maxActual);
    }

    @Test
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> RandomGraphGenerator.generateRandomGraph(-5, 0.3, 1.0, 10.0, true));
        assertThrows(IllegalArgumentException.class, () -> RandomGraphGenerator.generateRandomGraph(0, 0.3, 1.0, 10.0, true));
        assertThrows(IllegalArgumentException.class, () -> RandomGraphGenerator.generateRandomGraph(5, -0.1, 1.0, 10.0, true));
        assertThrows(IllegalArgumentException.class, () -> RandomGraphGenerator.generateRandomGraph(5, 1.5, 1.0, 10.0, true));
        assertThrows(IllegalArgumentException.class, () -> RandomGraphGenerator.generateRandomGraph(5, 0.3, 10.0, 5.0, true));
        assertThrows(IllegalArgumentException.class, () -> RandomGraphGenerator.generateRandomGraph(5, 0.3, Double.POSITIVE_INFINITY, 5.0, true));
        assertThrows(IllegalArgumentException.class, () -> RandomGraphGenerator.generateRandomGraph(5, 0.3, Double.NaN, 5.0, true));
    }

    @Test
    void testSameParametersProduceDifferentGraphs() {
        WeightedGraph g1 = RandomGraphGenerator.generateRandomGraph(12, 0.4, 1.0, 10.0, true);
        WeightedGraph g2 = RandomGraphGenerator.generateRandomGraph(12, 0.4, 1.0, 10.0, true);
        assertEquals(g1.getNodeCount(), g2.getNodeCount());
        assertTrue(Math.abs(g1.getEdgeCount() - g2.getEdgeCount()) < 20); // rough similarity
        // Compare edge id sets for non-identity
        Set<String> s1 = new HashSet<>();
        g1.getAllEdges().forEach(e -> s1.add(e.getId()));
        Set<String> s2 = new HashSet<>();
        g2.getAllEdges().forEach(e -> s2.add(e.getId()));
        assertNotEquals(s1, s2);
    }

    @Test
    void testLargeGraphGeneration() {
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(100, 0.1, 1.0, 20.0, true);
        assertEquals(100, g.getNodeCount());
        assertTrue(g.getEdgeCount() >= 99);
        assertTrue(isGraphConnected(g));
    }

    @Test
    void testEdgeDensityApproximation() {
        int n = 20;
        double density = 0.5;
        WeightedGraph g = RandomGraphGenerator.generateRandomGraph(n, density, 1.0, 10.0, true);
        int expected = (int) Math.floor(density * (n * (n - 1)));
        int actual = g.getEdgeCount();
        int tolerance = (int) (expected * 0.1);
        assertTrue(actual >= expected - tolerance && actual <= expected + tolerance);
    }

    // Helpers
    private boolean isGraphConnected(WeightedGraph g) {
        if (g.getNodeCount() == 0) return true;
        Map<String, Node> idToNode = new HashMap<>();
        for (Node n : g.getAllNodes()) idToNode.put(n.getId(), n);
        Node start = g.getAllNodes().iterator().next();
        Set<Node> visited = new HashSet<>();
        Deque<Node> dq = new ArrayDeque<>();
        dq.add(start);
        visited.add(start);
        while (!dq.isEmpty()) {
            Node u = dq.removeFirst();
            for (Node v : g.getNeighbors(u)) {
                if (visited.add(v)) dq.addLast(v);
            }
        }
        return visited.size() == g.getNodeCount();
    }
}
