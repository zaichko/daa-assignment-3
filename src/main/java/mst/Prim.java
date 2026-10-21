package mst;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    private Prim(){}

    public static MSTResult run(Graph graph){
        long start = System.nanoTime();
        int n = graph.vertexCount();
        boolean[] inTree = new boolean[n];
        List<Edge> mst = new ArrayList<>();
        long total = 0, examined = 0, heapOps = 0;
        long[] comparisons = {0};
        int components = 0;

        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {comparisons[0]++; return Integer.compare(a.weight(), b.weight()); });

        for(int s = 0; s < n; s++){
            if(inTree[s]){
                continue;
            }
            components++;
            inTree[s] = true;
            for (Edge edge : graph.neighbours(s)){
                examined++;
                pq.add(edge);
                heapOps++;
            }

            while (!pq.isEmpty()){
                Edge edge = pq.poll();
                heapOps++;
                if (inTree[edge.to()]){continue;}
                inTree[edge.to()] = true;
                mst.add(edge);
                total += edge.weight();

                for (Edge f : graph.neighbours(edge.to())){
                    examined++;
                    if (!inTree[f.to()]){
                        pq.add(f);
                        heapOps++;
                    }
                }
            }
        }

        boolean connected = n <= 1 || components == 1;
        return new MSTResult(mst, total, connected, examined, comparisons[0], 0, heapOps, System.nanoTime() - start);
    }
}
