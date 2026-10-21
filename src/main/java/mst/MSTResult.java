package mst;

import graph.Edge;

import java.util.List;

public record MSTResult(List<Edge> edges, long totalWeight, boolean connected,
                        long edgeExaminations, long comparisons, long dsuOps, long heapOps, long nanos) {
}
