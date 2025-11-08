package com.shortestpath.model;

import java.util.Objects;

public class Edge {
    private final String id;
    private final Node source;
    private final Node target;
    private double weight;

    public Edge(Node source, Node target, double weight) {
        this(source.getId() + "->" + target.getId(), source, target, weight);
    }

    public Edge(String id, Node source, Node target, double weight) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target nodes must be non-null");
        }
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Edge id must be non-empty");
        }
        validateWeight(weight);
        this.id = id;
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    private static void validateWeight(double weight) {
        if (Double.isNaN(weight) || Double.isInfinite(weight)) {
            throw new IllegalArgumentException("Edge weight must be a finite number");
        }
    }

    public Node getSource() {
        return source;
    }

    public Node getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        validateWeight(weight);
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge other = (Edge) obj;
        // Consider equal if IDs match or if source-target pair matches
        return Objects.equals(this.id, other.id) ||
               (Objects.equals(this.source.getId(), other.source.getId()) &&
                Objects.equals(this.target.getId(), other.target.getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return source.getId() + "->" + target.getId() + " (weight: " + weight + ")";
    }
}
