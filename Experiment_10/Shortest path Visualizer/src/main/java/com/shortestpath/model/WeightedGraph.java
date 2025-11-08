package com.shortestpath.model;

import java.util.*;

public class WeightedGraph implements Cloneable {
    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private final Map<Node, List<Edge>> adjacencyList = new HashMap<>();
    private final boolean directed;

    public WeightedGraph() {
        this(true);
    }

    public WeightedGraph(boolean directed) {
        this.directed = directed;
    }

    // Node operations
    public void addNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        String id = node.getId();
        if (nodes.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate node id: " + id);
        }
        nodes.put(id, node);
        adjacencyList.put(node, new ArrayList<>());
    }

    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public boolean containsNode(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    public Collection<Node> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public void removeNode(String nodeId) {
        Node node = nodes.remove(nodeId);
        if (node == null) return;
        // Remove outgoing edges
        List<Edge> outgoing = adjacencyList.remove(node);
        if (outgoing != null) {
            edges.removeAll(outgoing);
        }
        // Remove incoming edges and clean adjacency lists
        Iterator<Edge> it = edges.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            if (e.getTarget().equals(node) || e.getSource().equals(node)) {
                it.remove();
                List<Edge> list = adjacencyList.get(e.getSource());
                if (list != null) {
                    list.remove(e);
                }
            }
        }
        // Remove node from adjacency keys where empty lists may remain handled already
    }

    // Edge operations
    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge cannot be null");
        }
        Node src = edge.getSource();
        Node dst = edge.getTarget();
        if (!nodes.containsKey(src.getId()) || !nodes.containsKey(dst.getId())) {
            throw new IllegalArgumentException("Both source and target nodes must exist in the graph");
        }
        // Add forward edge
        edges.add(edge);
        adjacencyList.computeIfAbsent(src, k -> new ArrayList<>()).add(edge);
        // If undirected, add reverse edge automatically
        if (!directed) {
            Edge reverse = new Edge(dst, src, edge.getWeight());
            edges.add(reverse);
            adjacencyList.computeIfAbsent(dst, k -> new ArrayList<>()).add(reverse);
        }
    }

    public Edge addEdge(Node source, Node target, double weight) {
        Edge e = new Edge(source, target, weight);
        addEdge(e);
        return e;
    }

    public void removeEdge(String edgeId) {
        if (edgeId == null) return;
        Iterator<Edge> it = edges.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            if (edgeId.equals(e.getId())) {
                it.remove();
                List<Edge> list = adjacencyList.get(e.getSource());
                if (list != null) list.remove(e);
                break;
            }
        }
    }

    public void removeEdge(Node source, Node target) {
        if (source == null || target == null) return;
        Iterator<Edge> it = edges.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            if (e.getSource().equals(source) && e.getTarget().equals(target)) {
                it.remove();
                List<Edge> list = adjacencyList.get(source);
                if (list != null) list.remove(e);
                // if undirected, also remove reverse
                if (!directed) {
                    removeEdge(target, source);
                }
                break;
            }
        }
    }

    public Edge getEdge(Node source, Node target) {
        if (source == null || target == null) return null;
        List<Edge> list = adjacencyList.get(source);
        if (list == null) return null;
        for (Edge e : list) {
            if (e.getTarget().equals(target)) return e;
        }
        return null;
    }

    public List<Edge> getAllEdges() {
        return Collections.unmodifiableList(edges);
    }

    public int getEdgeCount() {
        return edges.size();
    }

    // Traversal operations
    public List<Edge> getOutgoingEdges(Node node) {
        List<Edge> list = adjacencyList.get(node);
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public List<Node> getNeighbors(Node node) {
        List<Edge> list = adjacencyList.get(node);
        if (list == null) return Collections.emptyList();
        List<Node> neighbors = new ArrayList<>(list.size());
        for (Edge e : list) {
            neighbors.add(e.getTarget());
        }
        return Collections.unmodifiableList(neighbors);
    }

    public List<Edge> getIncomingEdges(Node node) {
        List<Edge> incoming = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getTarget().equals(node)) {
                incoming.add(e);
            }
        }
        return Collections.unmodifiableList(incoming);
    }

    public double getEdgeWeight(Node source, Node target) {
        Edge e = getEdge(source, target);
        return (e == null) ? Double.POSITIVE_INFINITY : e.getWeight();
    }

    // Utilities
    public boolean isDirected() {
        return directed;
    }

    public void clear() {
        nodes.clear();
        edges.clear();
        adjacencyList.clear();
    }

    @Override
    public WeightedGraph clone() {
        WeightedGraph copy = new WeightedGraph(this.directed);
        // Copy nodes
        Map<String, Node> newNodes = new HashMap<>();
        for (Node n : nodes.values()) {
            Node nn = new Node(n.getId(), n.getX(), n.getY(), n.getLabel());
            newNodes.put(nn.getId(), nn);
            copy.addNode(nn);
        }
        // Copy edges
        for (Edge e : edges) {
            Node src = newNodes.get(e.getSource().getId());
            Node dst = newNodes.get(e.getTarget().getId());
            copy.addEdge(src, dst, e.getWeight());
        }
        return copy;
    }

    @Override
    public String toString() {
        return "WeightedGraph{" +
                "nodes=" + nodes.size() +
                ", edges=" + edges.size() +
                ", directed=" + directed +
                '}';
    }
}
