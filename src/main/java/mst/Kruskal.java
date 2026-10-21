package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;

public final class Kruskal {
    private Kruskal(){}

    public static MSTResult run(Graph graph){
        long start = System.nanoTime();
        int n = graph.vertexCount();
        List<Edge> sorted = new ArrayList<>(graph.edges());

        long[] comparisons = {0};
        sorted.sort((a, b) -> {comparisons[0]++; return Integer.compare(a.weight(), b.weight());});

        DSU dsu = new DSU(n);
        List<Edge> mst = new ArrayList<>();
        long total = 0, examined = 0;
        for (Edge edge : sorted){
            examined++;
            if(dsu.union(edge.from(), edge.to())){
                mst.add(edge);
                total += edge.weight();
                if (mst.size() == n - 1){
                    break;
                }
            }
        }

        boolean connected = n <= 1 || mst.size() == n - 1;
        long dsuOps = dsu.findCalls;
        return new MSTResult(mst, total, connected, examined, comparisons[0], dsuOps, 0, System.nanoTime() - start);
    }
}
