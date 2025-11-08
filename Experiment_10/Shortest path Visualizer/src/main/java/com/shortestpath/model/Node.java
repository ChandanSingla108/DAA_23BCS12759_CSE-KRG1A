package com.shortestpath.model;

import java.util.Objects;

public class Node {
    private final String id;
    private double x;
    private double y;
    private String label;

    public Node(String id) {
        this(id, 0.0, 0.0, id);
    }

    public Node(String id, double x, double y) {
        this(id, x, y, id);
    }

    public Node(String id, double x, double y, String label) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Node id must be non-empty");
        }
        this.id = id;
        this.x = x;
        this.y = y;
        this.label = (label == null || label.isEmpty()) ? id : label;
    }

    public String getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = (label == null || label.isEmpty()) ? this.id : label;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node other = (Node) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", label='" + label + '\'' +
                '}';
    }
}
