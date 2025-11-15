package com.slginventory.algorithms;

import java.util.*;

public class Graph {
    public static class Edge {
        public final String to;
        public final double weightKm;
        public Edge(String to, double weightKm) {
            this.to = to;
            this.weightKm = weightKm;
        }
    }

    private final Map<String, List<Edge>> adjacency = new HashMap<>();

    public void addVertex(String v) {
        adjacency.computeIfAbsent(v, k -> new ArrayList<>());
    }

    public void addUndirectedEdge(String a, String b, double km) {
        addVertex(a);
        addVertex(b);
        adjacency.get(a).add(new Edge(b, km));
        adjacency.get(b).add(new Edge(a, km));
    }

    public Map<String, List<Edge>> getAdjacency() {
        return adjacency;
    }

    public double getWeight(String from, String to) {
        List<Edge> edges = adjacency.get(from);
        if (edges != null) {
            for (Edge edge : edges) {
                if (edge.to.equals(to)) {
                    return edge.weightKm;
                }
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    // Build Sri Lanka distribution centers graph
    public static Graph buildSriLankaCenterGraph() {
        Graph graph = new Graph();
        
        // Add distances (in km) - real approximate distances
        graph.addUndirectedEdge("PETTAH", "PELIYAGODA", 12);
        graph.addUndirectedEdge("PETTAH", "DAMBULLA", 148);
        graph.addUndirectedEdge("PELIYAGODA", "DAMBULLA", 140);
        graph.addUndirectedEdge("DAMBULLA", "KANDY", 72);
        graph.addUndirectedEdge("KANDY", "GALLE", 185);
        graph.addUndirectedEdge("PETTAH", "GALLE", 116);
        graph.addUndirectedEdge("PETTAH", "JAFFNA", 396);
        graph.addUndirectedEdge("DAMBULLA", "JAFFNA", 290);
        
        return graph;
    }
}
