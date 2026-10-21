package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class TopologicalSort {
    private TopologicalSort(){}

    public static TopoResult run(Graph graph){
        if (!graph.isDirected()){
            throw new IllegalArgumentException("Topological order needs a directed graph.");
        }

        long start = System.nanoTime();
        int n = graph.vertexCount();
        int[] indeg = new int[n];
        long examined = 0;
        for (Edge edge : graph.edges()){
            indeg[edge.to()]++;
            examined++;
        }

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++){
            if (indeg[v] == 0){
                queue.add(v);
            }
        }

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()){
            int u = queue.poll();
            order.add(u);
            for (Edge edge : graph.neighbours(u)){
                examined++;
                if (--indeg[edge.to()] == 0){
                    queue.add(edge.to());
                }
            }
        }

        return new TopoResult(order.size() == n, order, examined, System.nanoTime() - start);
    }

    public static boolean isValidOrder(Graph graph, List<Integer> order){
        int n = graph.vertexCount();
        if (order.size() != n) { return false; }

        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (pos[v] != -1) { return false; }
            pos[v] = i;
        }
        for (Edge e : graph.edges()) {
            if (pos[e.from()] >= pos[e.to()]) {
                return false;
            }
        }

        return true;
    }
}
