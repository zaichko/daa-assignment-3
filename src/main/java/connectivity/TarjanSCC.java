package connectivity;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class TarjanSCC {
    private final Graph graph;
    private final int[] disc, low, comp;
    private final boolean[] onStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final List<List<Integer>> components = new ArrayList<>();
    private int time = 0;
    private long examined = 0;

    private TarjanSCC(Graph graph){
        this.graph = graph;
        int n = graph.vertexCount();
        disc = new int[n]; low = new int[n]; comp = new int[n];
        onStack = new boolean[n];
        Arrays.fill(disc, -1);
        Arrays.fill(comp, -1);
    }

    public static SCCResult run(Graph graph){
        if (!graph.isDirected()) {throw new IllegalArgumentException("SCC needs a directed graph."); }
        long start = System.nanoTime();
        TarjanSCC t = new TarjanSCC(graph);
        for (int v = 0; v < graph.vertexCount(); v++){
            if (t.disc[v] == -1){
                t.dfs(v);
            }
        }
        return new SCCResult(t.components, t.comp, t.examined, System.nanoTime() - start);
    }

    private void dfs(int u){
        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;

        for (Edge edge : graph.neighbours(u)){
            examined++;
            int v = edge.to();
            if (disc[v] == -1){
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]){
            List<Integer> scc = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                comp[w] = components.size();
                scc.add(w);
            } while (w != u);

            components.add(scc);
        }
    }
}
