package connectivity;

import java.util.List;

public record SCCResult(List<List<Integer>> components, int[] componentOf, long edgeExaminations, long nanos) {
    public int count(){
        return components.size();
    }
}
