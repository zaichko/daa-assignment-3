package shortestpath;

import graph.Edge;
import graph.Graph;

import java.util.Arrays;
import java.util.PriorityQueue;

public class Dijkstra {
    private Dijkstra(){}

    public static DijkstraResult run(Graph graph, int source){
        if (graph.hasNegativeEdge()){
            throw new IllegalArgumentException("Dijkstra requires non-negative edge weights.");
        }

        long start = System.nanoTime();
        int n = graph.vertexCount();
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, DijkstraResult.INF);
        Arrays.fill(parent, -1);
        dist[source] = 0;

        long relaxations = 0, heapOps = 0;
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> Long.compare(a[0], b[0]));
        pq.add(new long[]{0, source});
        heapOps++;

        while (!pq.isEmpty()){
            long[] top = pq.poll();
            heapOps++;
            int u = (int) top[1];
            if (top[0] > dist[u]) {continue; }

            for (Edge edge : graph.neighbours(u)){
                relaxations++;
                long nd = dist[u] + edge.weight();

                if (nd < dist[edge.to()]){
                    dist[edge.to()] = nd;
                    parent[edge.to()] = u;
                    pq.add(new long[]{nd, edge.to()});
                    heapOps++;
                }
            }
        }

        return new DijkstraResult(source, dist, parent, relaxations, heapOps, System.nanoTime() - start);
    }
}
