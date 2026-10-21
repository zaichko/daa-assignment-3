package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GraphGenerator {
    private GraphGenerator() {}

    public static Graph connectedUndirected(int n, int m, int maxWeight, long seed) {
        long maxEdges = (long) n * (n - 1) / 2;
        if (n >= 1 && (m < n - 1 || m > maxEdges))
            throw new IllegalArgumentException("m must be in [n-1, n(n-1)/2], got " + m);
        Random rnd = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();
        for (int i = 1; i < n; i++) {
            int j = rnd.nextInt(i);
            addUndirected(g, used, i, j, 1 + rnd.nextInt(maxWeight));
        }
        while (g.edgeCount() < m) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v) continue;
            if (used.contains(key(u, v, false))) continue;
            addUndirected(g, used, u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    public static Graph randomDag(int n, int m, int maxWeight, long seed) {
        long maxEdges = (long) n * (n - 1) / 2;
        if (m > maxEdges) throw new IllegalArgumentException("Too many edges for a DAG");
        Random rnd = new Random(seed);
        int[] rank = shuffledPermutation(n, rnd);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.edgeCount() < m) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a == b) continue;
            int u = rank[a] < rank[b] ? a : b;
            int v = u == a ? b : a;
            if (!used.add(key(u, v, true))) continue;
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }


    public static Graph randomDirected(int n, int m, int maxWeight, long seed) {
        long maxEdges = (long) n * (n - 1);
        if (m > maxEdges) throw new IllegalArgumentException("too many edges");
        Random rnd = new Random(seed);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.edgeCount() < m) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v || !used.add(key(u, v, true))) continue;
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    private static void addUndirected(Graph g, Set<Long> used, int u, int v, int w) {
        used.add(key(u, v, false));
        g.addEdge(u, v, w);
    }

    private static long key(int u, int v, boolean directed) {
        if (!directed && u > v) { int t = u; u = v; v = t; }
        return (long) u * 1_000_003L + v;
    }

    private static int[] shuffledPermutation(int n, Random rnd) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(i);
        java.util.Collections.shuffle(list, rnd);
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[list.get(i)] = i; // rank[v] = position of v
        return rank;
    }
}
