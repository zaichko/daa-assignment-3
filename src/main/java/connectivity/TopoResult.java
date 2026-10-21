package connectivity;

import java.util.List;

public record TopoResult(boolean isDAG, List<Integer> order, long edgeExaminations, long nanos) {
}
