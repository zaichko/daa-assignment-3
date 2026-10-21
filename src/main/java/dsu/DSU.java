package dsu;

public class DSU {
    private final int[] parent;
    private final int[] size;
    private final boolean pathCompression;
    private final boolean unionBySize;
    private int components;

    public long findCalls;

    public DSU(int n, boolean pathCompression, boolean unionBySize){
        this.parent = new int[n];
        this.size = new int[n];
        this.pathCompression = pathCompression;
        this.unionBySize = unionBySize;
        this.components = n;

        for (int i = 0; i < n; i++){
            size[i] = 1;
        }
    }

    public DSU(int n){
        this(n, true, true);
    }

    public int find(int x){
        findCalls++;
        int root = x;

        while (parent[root] != root){
            root = parent[root];
        }

        if (pathCompression){
            while (parent[x] != root){
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
        }

        return root;
    }

    public boolean union(int a, int b){
        int rootA = find(a);
        int rootB = find(b);

        if (connected(a, b)){
            return false;
        }

        if (unionBySize){
            if (size[rootA] < size[rootB]){
                int temp = rootA;
                rootA = rootB;
                rootB = temp;
            }

            parent[rootB] = rootA;
            size[rootA] += size[rootB];
        } else {
            parent[rootB] = rootA;
        }

        components--;
        return true;
    }

    public boolean connected(int a, int b){
        return find(a) == find(b);
    }

    public int componentCount(){
        return components;
    }
}
