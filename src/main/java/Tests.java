import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Tests {
    static int passed = 0, failed = 0;

    static void check(boolean cond, String name) {
        if (cond) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name); }
    }

    public static void main(String[] args) {
        dsuTests();
        mstTests();
        topoTests();
        sccTests();
        dijkstraTests();
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    // ---------- DSU ----------
    static void dsuTests() {
        System.out.println("DSU");
        DSU d = new DSU(6);
        check(d.union(0, 1) && d.union(1, 2), "union merges different sets");
        check(!d.union(0, 2), "union returns false when already connected");
        check(d.connected(0, 2) && !d.connected(0, 5), "connected()");
        check(d.componentCount() == 4, "component count");
    }

    // ---------- MST ----------
    static void mstTests() {
        System.out.println("MST");
        // single vertex
        Graph one = new Graph(1, false);
        check(agree(one) && Kruskal.run(one).connected() && Kruskal.run(one).totalWeight() == 0, "single vertex");

        // classic small graph, known MST weight = 1+2+3 = 6... edges: 0-1(1) 1-2(2) 2-3(3) 0-3(10) 0-2(5)
        Graph small = new Graph(4, false);
        small.addEdge(0, 1, 1); small.addEdge(1, 2, 2); small.addEdge(2, 3, 3);
        small.addEdge(0, 3, 10); small.addEdge(0, 2, 5);
        check(Kruskal.run(small).totalWeight() == 6 && Prim.run(small).totalWeight() == 6, "small graph weight == 6");
        check(Kruskal.run(small).edges().size() == 3, "MST has V-1 edges");

        // disconnected
        Graph dis = new Graph(4, false);
        dis.addEdge(0, 1, 1); dis.addEdge(2, 3, 2);
        check(!Kruskal.run(dis).connected() && !Prim.run(dis).connected(), "disconnected detected by both");
        check(Kruskal.run(dis).totalWeight() == Prim.run(dis).totalWeight(), "forest weight equal on disconnected");

        // multiple valid MSTs + duplicate weights: 4-cycle, all weights equal
        Graph sq = new Graph(4, false);
        sq.addEdge(0, 1, 5); sq.addEdge(1, 2, 5); sq.addEdge(2, 3, 5); sq.addEdge(3, 0, 5);
        check(Kruskal.run(sq).totalWeight() == 15 && Prim.run(sq).totalWeight() == 15, "multiple MSTs: same weight");

        // random connected graphs: Kruskal == Prim, and both are connected
        boolean allEqual = true;
        for (int seed = 0; seed < 100; seed++) {
            Graph g = GraphGenerator.connectedUndirected(30, 100, 10, seed); // small weight range => many ties
            MSTResult k = Kruskal.run(g), p = Prim.run(g);
            if (k.totalWeight() != p.totalWeight() || !k.connected() || !p.connected()
                    || k.edges().size() != 29 || p.edges().size() != 29) allEqual = false;
        }
        check(allEqual, "Kruskal == Prim on 100 random connected graphs (with ties)");
    }

    static boolean agree(Graph g) { return Kruskal.run(g).totalWeight() == Prim.run(g).totalWeight(); }

    // ---------- Topological sort ----------
    static void topoTests() {
        System.out.println("Topological order");
        Graph chain = new Graph(3, true);
        chain.addEdge(0, 1, 1); chain.addEdge(1, 2, 1);
        TopoResult r = TopologicalSort.run(chain);
        check(r.isDAG() && r.order().equals(List.of(0, 1, 2)), "simple chain");

        Graph cyc = new Graph(3, true);
        cyc.addEdge(0, 1, 1); cyc.addEdge(1, 2, 1); cyc.addEdge(2, 0, 1);
        check(!TopologicalSort.run(cyc).isDAG(), "3-cycle detected");

        Graph partial = new Graph(4, true); // 0 -> 1 <-> 2, 3 isolated: cycle somewhere, not all emitted
        partial.addEdge(0, 1, 1); partial.addEdge(1, 2, 1); partial.addEdge(2, 1, 1);
        check(!TopologicalSort.run(partial).isDAG(), "cycle not at the start still detected");

        check(TopologicalSort.run(new Graph(1, true)).isDAG(), "single vertex is a DAG");

        boolean valid = true;
        for (int seed = 0; seed < 100; seed++) {
            Graph g = GraphGenerator.randomDag(40, 150, 10, seed);
            TopoResult t = TopologicalSort.run(g);
            if (!t.isDAG() || !TopologicalSort.isValidOrder(g, t.order())) valid = false;
        }
        check(valid, "valid order on 100 random DAGs");

        boolean allCyclic = true;
        for (int seed = 0; seed < 20; seed++) {
            Graph g = GraphGenerator.randomDirected(40, 400, 10, seed); // dense => cycles
            if (TopologicalSort.run(g).isDAG()) allCyclic = false;
        }
        check(allCyclic, "dense random directed graphs flagged as cyclic");
    }

    // ---------- SCC ----------
    static void sccTests() {
        System.out.println("SCC");
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 1); g.addEdge(1, 2, 1); g.addEdge(2, 0, 1);
        g.addEdge(2, 3, 1); g.addEdge(3, 4, 1); g.addEdge(4, 3, 1);
        SCCResult r = TarjanSCC.run(g);
        check(r.count() == 2 && r.componentOf()[0] == r.componentOf()[2] && r.componentOf()[3] == r.componentOf()[4]
                && r.componentOf()[0] != r.componentOf()[3], "known graph: {0,1,2} and {3,4}");

        check(TarjanSCC.run(new Graph(1, true)).count() == 1, "single vertex");
        Graph dag = GraphGenerator.randomDag(20, 50, 5, 1);
        check(TarjanSCC.run(dag).count() == 20, "DAG: every vertex is its own SCC");

        // brute force: u,v in same SCC  <=>  u reaches v and v reaches u
        boolean ok = true;
        for (int seed = 0; seed < 100 && ok; seed++) {
            int n = 12;
            Graph h = GraphGenerator.randomDirected(n, 20, 5, seed);
            SCCResult s = TarjanSCC.run(h);
            boolean[][] reach = closure(h);
            int total = 0;
            for (List<Integer> c : s.components()) total += c.size();
            if (total != n) ok = false; // each vertex in exactly one SCC
            for (int u = 0; u < n && ok; u++)
                for (int v = 0; v < n; v++) {
                    boolean same = reach[u][v] && reach[v][u];
                    if (same != (s.componentOf()[u] == s.componentOf()[v])) { ok = false; break; }
                }
        }
        check(ok, "matches brute-force reachability on 100 random graphs; every vertex in exactly one SCC");
    }

    static boolean[][] closure(Graph g) {
        int n = g.vertexCount();
        boolean[][] r = new boolean[n][n];
        for (int i = 0; i < n; i++) r[i][i] = true;
        for (Edge e : g.edges()) r[e.from()][e.to()] = true;
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++) if (r[i][k] && r[k][j]) r[i][j] = true;
        return r;
    }

    // ---------- Dijkstra ----------
    static void dijkstraTests() {
        System.out.println("Dijkstra");
        Graph g = new Graph(5, true); // vertex 4 unreachable
        g.addEdge(0, 1, 4); g.addEdge(0, 2, 1); g.addEdge(2, 1, 2); g.addEdge(1, 3, 1);
        DijkstraResult r = Dijkstra.run(g, 0);
        check(r.dist()[1] == 3 && r.dist()[3] == 4, "distances (0->2->1 beats 0->1)");
        check(r.pathTo(3).equals(List.of(0, 2, 1, 3)), "path reconstruction");
        check(!r.reachable(4) && r.pathTo(4).isEmpty(), "unreachable vertex handled");

        Graph one = new Graph(1, false);
        check(Dijkstra.run(one, 0).dist()[0] == 0, "single vertex");

        Graph neg = new Graph(2, true);
        neg.addEdge(0, 1, -3);
        boolean threw = false;
        try { Dijkstra.run(neg, 0); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "negative weight rejected");

        boolean ok = true;
        for (int seed = 0; seed < 100 && ok; seed++) {
            Graph h = GraphGenerator.randomDirected(25, 60, 20, seed);
            long[] bf = bellmanFord(h, 0);
            long[] dj = Dijkstra.run(h, 0).dist();
            if (!Arrays.equals(bf, dj)) ok = false;
        }
        check(ok, "matches Bellman-Ford reference on 100 random directed graphs");
    }

    static long[] bellmanFord(Graph g, int s) {
        long[] d = new long[g.vertexCount()];
        Arrays.fill(d, DijkstraResult.INF);
        d[s] = 0;
        for (int i = 0; i < g.vertexCount() - 1; i++)
            for (Edge e : g.edges())
                if (d[e.from()] != DijkstraResult.INF && d[e.from()] + e.weight() < d[e.to()])
                    d[e.to()] = d[e.from()] + e.weight();
        return d;
    }

    @SuppressWarnings("unused")
    private static final Random UNUSED = new Random(0);
}
