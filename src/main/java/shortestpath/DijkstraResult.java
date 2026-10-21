package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record DijkstraResult(int source, long[] dist, int[] parent,
                             long edgeRelaxations, long heapOps, long nanos) {
    public static final long INF = Long.MAX_VALUE;

    public boolean reachable(int v) { return dist[v] != INF; }

    public List<Integer> pathTo(int target) {
        if (!reachable(target)) return List.of();
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = parent[v]){
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }
}