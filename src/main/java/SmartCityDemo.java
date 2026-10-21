import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import graph.Edge;
import graph.Graph;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.util.ArrayList;
import java.util.List;

public class SmartCityDemo {
    static final String[] D = {"Downtown", "Airport", "University", "Harbor", "Old Town", "Tech Park", "Suburb"};

    static int id(String name) {
        for (int i = 0; i < D.length; i++) if (D[i].equals(name)) return i;
        throw new IllegalArgumentException("unknown district: " + name);
    }

    static String name(int v) { return D[v]; }

    public static void main(String[] args) {
        infrastructurePlanning();
        servicePlanning();
        routePlanning();
    }

    // ---- 1 ----
    static void infrastructurePlanning() {
        header("1. Infrastructure planning: cheapest road network (MST)");
        Graph roads = new Graph(D.length, false);
        road(roads, "Downtown", "Airport", 12);
        road(roads, "Downtown", "University", 4);
        road(roads, "Downtown", "Old Town", 3);
        road(roads, "University", "Tech Park", 6);
        road(roads, "Old Town", "Harbor", 5);
        road(roads, "Harbor", "Airport", 9);
        road(roads, "Tech Park", "Suburb", 7);
        road(roads, "University", "Suburb", 10);
        road(roads, "Downtown", "Harbor", 8);

        MSTResult k = Kruskal.run(roads), p = Prim.run(roads);
        System.out.println("Kruskal total cost: " + k.totalWeight() + ", Prim total cost: " + p.totalWeight());
        System.out.println("Connected: " + k.connected() + ", same cost: " + (k.totalWeight() == p.totalWeight()));
        System.out.println("Roads to build (Kruskal):");
        for (Edge e : k.edges()) System.out.printf("  %-10s - %-10s cost %d%n", name(e.from()), name(e.to()), e.weight());

        Graph isolated = new Graph(D.length, false);
        road(isolated, "Downtown", "Old Town", 3);
        System.out.println("With almost no roads, network connected? " + Kruskal.run(isolated).connected());
    }

    // ---- 2 ----
    static void servicePlanning() {
        header("2. Service dependencies: build order (topological sort) and circular dependencies (SCC)");
        Graph deps = new Graph(D.length, true);
        dep(deps, "Downtown", "Tech Park");   // "A -> B" means A must be finished before B
        dep(deps, "Downtown", "University");
        dep(deps, "University", "Tech Park");
        dep(deps, "Old Town", "Downtown");
        dep(deps, "Harbor", "Airport");

        TopoResult t = TopologicalSort.run(deps);
        System.out.println("Acyclic? " + t.isDAG());
        System.out.println("Build order: " + names(t.order()));

        // A circular dependency: Tech Park needs Downtown, and Downtown needs Tech Park
        dep(deps, "Tech Park", "Downtown");
        TopoResult t2 = TopologicalSort.run(deps);
        System.out.println("\nAfter adding Tech Park -> Downtown:");
        System.out.println("Acyclic? " + t2.isDAG() + "  (only " + t2.order().size() + " of " + D.length + " could be ordered)");
        SCCResult scc = TarjanSCC.run(deps);
        System.out.println("Strongly connected components:");
        for (List<Integer> c : scc.components())
            System.out.println("  " + names(c) + (c.size() > 1 ? "   <-- circular dependency" : ""));
    }

    // ---- 3 ----
    static void routePlanning() {
        header("3. Route planning: shortest distances from Downtown (Dijkstra)");
        Graph streets = new Graph(D.length, true);
        road2(streets, "Downtown", "Old Town", 3);
        road2(streets, "Old Town", "Harbor", 5);
        road2(streets, "Downtown", "University", 4);
        road2(streets, "University", "Tech Park", 6);
        streets.addEdge(id("Harbor"), id("Airport"), 9);        // one-way: Harbor -> Airport only
        streets.addEdge(id("Downtown"), id("Airport"), 20);     // slow direct road
        // "Suburb" has no road in: unreachable

        DijkstraResult r = Dijkstra.run(streets, id("Downtown"));
        for (int v = 0; v < D.length; v++) {
            if (r.reachable(v)) System.out.printf("  %-10s %3d   via %s%n", name(v), r.dist()[v], String.join(" -> ", names(r.pathTo(v))));
            else System.out.printf("  %-10s unreachable%n", name(v));
        }
    }


    static void road(Graph g, String a, String b, int cost) { g.addEdge(id(a), id(b), cost); }

    static void road2(Graph g, String a, String b, int len) { g.addEdge(id(a), id(b), len); g.addEdge(id(b), id(a), len); }

    static void dep(Graph g, String before, String after) { g.addEdge(id(before), id(after), 1); }

    static List<String> names(List<Integer> vs) {
        List<String> out = new ArrayList<>();
        for (int v : vs) out.add(name(v));
        return out;
    }

    static void header(String s) { System.out.println("\n=== " + s + " ==="); }
}
