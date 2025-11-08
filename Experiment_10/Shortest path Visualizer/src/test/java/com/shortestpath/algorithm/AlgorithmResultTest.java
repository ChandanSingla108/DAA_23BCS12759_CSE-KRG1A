package com.shortestpath.algorithm;

import com.shortestpath.model.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AlgorithmResult Tests")
class AlgorithmResultTest {

    @Test
    @DisplayName("Constructor and getters return expected values")
    void testConstructorAndGetters() {
        Node A = new Node("A");
        Node B = new Node("B");
        AlgorithmStep step = new AlgorithmStep(0, null, java.util.Set.of(), java.util.Map.of(A, 0.0), java.util.Map.of(B, A), List.of(), "init");
        AlgorithmResult result = new AlgorithmResult(List.of(step), List.of(A, B), 5.0, A, B, 10, 2);
        assertEquals(1, result.getSteps().size());
        assertEquals(2, result.getShortestPath().size());
        assertEquals(5.0, result.getPathCost(), 1e-9);
        assertEquals(A, result.getSourceNode());
        assertEquals(B, result.getTargetNode());
        assertEquals(10, result.getExecutionTimeMs());
        assertEquals(2, result.getNodesVisited());
    }

    @Test
    @DisplayName("hasPath returns true when cost is finite and false when infinite")
    void testHasPath() {
        Node A = new Node("A");
        Node B = new Node("B");
        AlgorithmResult ok = new AlgorithmResult(List.of(), List.of(A, B), 3.0, A, B, 1, 2);
        assertTrue(ok.hasPath());
        AlgorithmResult none = new AlgorithmResult(List.of(), List.of(), Double.POSITIVE_INFINITY, A, B, 1, 0);
        assertFalse(none.hasPath());
    }

    @Test
    @DisplayName("Empty path list when no path exists")
    void testEmptyPathWhenNoPathExists() {
        Node A = new Node("A");
        Node B = new Node("B");
        AlgorithmResult res = new AlgorithmResult(List.of(), List.of(), Double.POSITIVE_INFINITY, A, B, 0, 0);
        assertTrue(res.getShortestPath().isEmpty());
        assertFalse(res.hasPath());
    }

    @Test
    @DisplayName("Collections are unmodifiable")
    void testCollectionsAreUnmodifiable() {
        Node A = new Node("A");
        Node B = new Node("B");
        AlgorithmResult res = new AlgorithmResult(List.of(), List.of(A, B), 1.0, A, B, 0, 0);
        assertThrows(UnsupportedOperationException.class, () -> res.getSteps().add(null));
        assertThrows(UnsupportedOperationException.class, () -> res.getShortestPath().add(new Node("X")));
    }

    @Test
    @DisplayName("Step count matches number of steps")
    void testGetStepCount() {
        Node A = new Node("A");
        AlgorithmStep s0 = new AlgorithmStep(0, null, java.util.Set.of(), java.util.Map.of(A, 0.0), java.util.Map.of(), List.of(), "init");
        AlgorithmStep s1 = new AlgorithmStep(1, A, java.util.Set.of(A), java.util.Map.of(A, 0.0), java.util.Map.of(), List.of(), "visit A");
        AlgorithmResult res = new AlgorithmResult(List.of(s0, s1), List.of(A), 0.0, A, A, 1, 1);
        assertEquals(2, res.getStepCount());
    }

    @Test
    @DisplayName("toString contains path, cost and metrics")
    void testToString() {
        Node A = new Node("A");
        Node B = new Node("B");
        AlgorithmResult res = new AlgorithmResult(List.of(), List.of(A, B), 2.0, A, B, 5, 2);
        String s = res.toString();
        assertTrue(s.contains("cost"));
        assertTrue(s.contains("steps"));
        assertTrue(s.contains("timeMs"));
    }

    @Test
    @DisplayName("Validation checks for constructor arguments")
    void testValidation() {
        Node A = new Node("A");
        Node B = new Node("B");
        assertThrows(IllegalArgumentException.class, () -> new AlgorithmResult(List.of(), List.of(), 0.0, null, B, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new AlgorithmResult(List.of(), List.of(), 0.0, A, null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new AlgorithmResult(List.of(), List.of(), 0.0, A, B, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new AlgorithmResult(List.of(), List.of(), 0.0, A, B, 0, -2));
    }
}
