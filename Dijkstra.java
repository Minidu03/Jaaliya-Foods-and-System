package com.slginventory.algorithms;

import java.util.*;

public class Dijkstra {
    public static class PathResult {
        public final Map<String, Double> distanceKm;
        public final Map<String, String> previous;
        public PathResult(Map<String, Double> distanceKm, Map<String, String> previous) {
            this.distanceKm = distanceKm;
            this.previous = previous;
        }
        public List<String> reconstructPath(String target) {
            LinkedList<String> path = new LinkedList<>();
            // Check if target is reachable (has finite distance)
            Double distance = distanceKm.get(target);
            if (distance == null || distance == Double.POSITIVE_INFINITY) {
                return path; // Return empty path if unreachable
            }
            String cur = target;
            while (cur != null) {
                path.addFirst(cur);
                cur = previous.get(cur);
            }
            return path;
        }
    }

    public static PathResult shortestPaths(Graph graph, String source) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String v : graph.getAdjacency().keySet()) {
            dist.put(v, Double.POSITIVE_INFINITY);
            prev.put(v, null);
        }
        dist.put(source, 0.0);

        // Use a custom comparator that reads from the dist map
        PriorityQueue<String> pq = new PriorityQueue<>((a, b) -> 
            Double.compare(dist.getOrDefault(a, Double.POSITIVE_INFINITY), 
                          dist.getOrDefault(b, Double.POSITIVE_INFINITY)));
        pq.add(source);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);
            
            for (Graph.Edge e : graph.getAdjacency().getOrDefault(u, List.of())) {
                if (visited.contains(e.to)) {
                    continue;
                }
                double alt = dist.get(u) + e.weightKm;
                if (alt < dist.get(e.to)) {
                    dist.put(e.to, alt);
                    prev.put(e.to, u);
                    // Re-add to priority queue with updated distance
                    pq.remove(e.to);
                    pq.add(e.to);
                }
            }
        }
        return new PathResult(dist, prev);
    }
}
