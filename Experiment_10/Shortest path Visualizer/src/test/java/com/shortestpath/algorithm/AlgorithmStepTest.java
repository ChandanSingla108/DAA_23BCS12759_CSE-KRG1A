package com.shortestpath.algorithm;

import com.shortestpath.model.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AlgorithmStep Tests")
class AlgorithmStepTest {

    @Test
    @DisplayName("Constructor sets fields and getters return values")
    void testConstructorAndGetters() {
        Node A = new Node("A");
        Node B = new Node("B");
        Set<Node> visited = new HashSet<>(Set.of(A));
        Map<Node, Double> dist = new HashMap<>();
        dist.put(A, 0.0);
        dist.put(B, 5.0);
        Map<Node, Node> pred = new HashMap<>();
        pred.put(B, A);
        List<Node> pq = new ArrayList<>(List.of(B));
        String desc = "Visiting node A";

        AlgorithmStep step = new AlgorithmStep(1, A, visited, dist, pred, pq, desc);
        assertEquals(1, step.getStepNumber());
        assertEquals(A, step.getCurrentNode());
        assertEquals(desc, step.getDescription());
        assertTrue(step.getVisitedNodes().contains(A));
        assertEquals(0.0, step.getDistance(A), 1e-9);
        assertEquals(A, step.getPredecessor(B));
        assertEquals(List.of(B), step.getPriorityQueue());
    }

    @Test
    @DisplayName("Collections returned are unmodifiable")
    void testCollectionsAreUnmodifiable() {
        AlgorithmStep step = createSampleStep();
        assertThrows(UnsupportedOperationException.class, () -> step.getVisitedNodes().add(new Node("X")));
        assertThrows(UnsupportedOperationException.class, () -> step.getDistances().put(new Node("X"), 1.0));
        assertThrows(UnsupportedOperationException.class, () -> step.getPredecessors().put(new Node("X"), null));
        assertThrows(UnsupportedOperationException.class, () -> step.getPriorityQueue().add(new Node("X")));
    }

    @Test
    @DisplayName("Defensive copies prevent external mutation after construction")
    void testDefensiveCopies() {
        Node A = new Node("A");
        Node B = new Node("B");
        Set<Node> visited = new HashSet<>();
        Map<Node, Double> dist = new HashMap<>();
        Map<Node, Node> pred = new HashMap<>();
        List<Node> pq = new ArrayList<>();

        AlgorithmStep step = new AlgorithmStep(0, null, visited, dist, pred, pq, "init");
        visited.add(A);
        dist.put(B, 10.0);
        pred.put(B, A);
        pq.add(B);

        assertFalse(step.getVisitedNodes().contains(A));
        assertEquals(Double.POSITIVE_INFINITY, step.getDistance(B));
        assertNull(step.getPredecessor(B));
        assertTrue(step.getPriorityQueue().isEmpty());
    }

    @Test
    @DisplayName("Utility methods operate correctly")
    void testUtilityMethods() {
        Node A = new Node("A");
        Node B = new Node("B");
        AlgorithmStep s = new AlgorithmStep(2, B,
                new HashSet<>(Set.of(A, B)),
                new HashMap<>(Map.of(A, 0.0, B, 1.0)),
                new HashMap<>(Map.of(B, A)),
                new ArrayList<>(List.of()),
                "desc");
        assertEquals(0.0, s.getDistance(A), 1e-9);
        assertEquals(1.0, s.getDistance(B), 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, s.getDistance(new Node("Z")), 1e-9);
        assertEquals(A, s.getPredecessor(B));
        assertTrue(s.isVisited(A));
        assertTrue(s.isVisited(B));
        assertFalse(s.isVisited(new Node("Z")));
    }

    @Test
    @DisplayName("Null current node allowed for init step")
    void testNullCurrentNode() {
        AlgorithmStep s = new AlgorithmStep(0, null, Set.of(), Map.of(), Map.of(), List.of(), "init");
        assertNull(s.getCurrentNode());
        assertEquals(0, s.getStepNumber());
    }

    @Test
    @DisplayName("toString contains step number and description")
    void testToString() {
        AlgorithmStep s = new AlgorithmStep(3, null, Set.of(), Map.of(), Map.of(), List.of(), "some description");
        String t = s.toString();
        assertTrue(t.contains("3"));
        assertTrue(t.contains("some description"));
    }

    @Test
    @DisplayName("Negative step number throws exception")
    void testNegativeStepNumberThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new AlgorithmStep(-1, null, Set.of(), Map.of(), Map.of(), List.of(), "bad"));
    }

    // Helper
    private static AlgorithmStep createSampleStep() {
        Node A = new Node("A");
        Node B = new Node("B");
        return new AlgorithmStep(1, A,
                new HashSet<>(Set.of(A)),
                new HashMap<>(Map.of(A, 0.0, B, 5.0)),
                new HashMap<>(Map.of(B, A)),
                new ArrayList<>(List.of(B)),
                "desc");
    }
}
