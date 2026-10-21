import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class Benchmark {
    static final long SEED = 42;
    static final int REPEATS = 5;
    static final int MAX_W = 100;

    static final int[][] SIZES = {{10, 30}, {100, 500}, {1000, 5000}};
    static final String[] NAMES = {"Small", "Medium", "Large"};

    record Run(long nanos, long edgeExams, long comparisons, long otherOps) {}

    public static void main(String[] args) throws IOException {
        Path dir = Path.of("results", "tables");
        Files.createDirectories(dir);
        Files.createDirectories(Path.of("results", "plots"));

        List<String> rows = new ArrayList<>();
        rows.add("algorithm,size,V,E,avg_time_ms,edge_examinations,comparisons,dsu_or_heap_ops");

        for (int i = 0; i < SIZES.length; i++) {
            int v = SIZES[i][0], e = SIZES[i][1];
            Graph undirected = GraphGenerator.connectedUndirected(v, e, MAX_W, SEED);
            Graph dag = GraphGenerator.randomDag(v, e, MAX_W, SEED);
            Graph cyclic = GraphGenerator.randomDirected(v, e, MAX_W, SEED);

            rows.add(row("Kruskal", i, measure(() -> { MSTResult r = Kruskal.run(undirected);
                return new Run(r.nanos(), r.edgeExaminations(), r.comparisons(), r.dsuOps()); })));
            rows.add(row("Prim", i, measure(() -> { MSTResult r = Prim.run(undirected);
                return new Run(r.nanos(), r.edgeExaminations(), r.comparisons(), r.heapOps()); })));
            rows.add(row("Topo-DAG", i, measure(() -> { TopoResult r = TopologicalSort.run(dag);
                return new Run(r.nanos(), r.edgeExaminations(), 0, 0); })));
            rows.add(row("Topo-Cyclic", i, measure(() -> { TopoResult r = TopologicalSort.run(cyclic);
                return new Run(r.nanos(), r.edgeExaminations(), 0, 0); })));
            rows.add(row("SCC-Tarjan", i, measure(() -> { SCCResult r = TarjanSCC.run(cyclic);
                return new Run(r.nanos(), r.edgeExaminations(), 0, 0); })));
            rows.add(row("Dijkstra", i, measure(() -> { DijkstraResult r = Dijkstra.run(undirected, 0);
                return new Run(r.nanos(), r.edgeRelaxations(), 0, r.heapOps()); })));

            // document whether the graph contains cycles
            SCCResult scc = TarjanSCC.run(cyclic);
            System.out.printf("%s: random directed graph has %d SCCs; DAG isDag=%b; cyclic isDag=%b%n",
                    NAMES[i], scc.count(), TopologicalSort.run(dag).isDAG(), TopologicalSort.run(cyclic).isDAG());
        }
        write(dir.resolve("results.csv"), rows);
        rows.forEach(System.out::println);

        dsuAblation(dir);
    }

    static Run measure(Supplier<Run> task) {
        task.get(); // warm-up, discarded
        long total = 0;
        Run last = null;
        for (int i = 0; i < REPEATS; i++) { last = task.get(); total += last.nanos(); }
        return new Run(total / REPEATS, last.edgeExams(), last.comparisons(), last.otherOps());
    }

    static String row(String algo, int i, Run r) {
        return String.format(Locale.ROOT, "%s,%s,%d,%d,%.4f,%d,%d,%d", algo, NAMES[i], SIZES[i][0], SIZES[i][1],
                r.nanos() / 1e6, r.edgeExams(), r.comparisons(), r.otherOps());
    }

    static void dsuAblation(Path dir) throws IOException {
        int n = 10_000;
        List<String> rows = new ArrayList<>();
        rows.add("config,pointer_hops,time_ms");
        boolean[][] cfgs = {{false, false}, {true, false}, {false, true}, {true, true}};
        String[] labels = {"none", "path_compression_only", "union_by_size_only", "both"};
        for (int c = 0; c < cfgs.length; c++) {
            DSU d = new DSU(n, cfgs[c][0], cfgs[c][1]);
            long t0 = System.nanoTime();
            for (int i = 0; i < n - 1; i++) d.union(i + 1, i);
            for (int v = 0; v < n; v++) d.find(v);
            long ms = (System.nanoTime() - t0);
            rows.add(String.format(Locale.ROOT, "%s,%d,%.3f", labels[c], d.pointerHops, ms / 1e6));
        }
        write(dir.resolve("dsu_ablation.csv"), rows);
        System.out.println();
        rows.forEach(System.out::println);
    }

    static void write(Path p, List<String> lines) throws IOException {
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(p))) { lines.forEach(w::println); }
    }
}
