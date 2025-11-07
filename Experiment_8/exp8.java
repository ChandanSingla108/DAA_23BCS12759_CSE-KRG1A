class Solution {
    public int[] dijkstra(int V, int[][] edges, int src) {
        // code here
        int[] ans = new int[V];
        Arrays.fill(ans,Integer.MAX_VALUE);
        PriorityQueue<Pair> pq = new PriorityQueue<>();
        List<List<Pair>> graph = constructGraph(edges,V);
        pq.add(new Pair(src,0));
        while(pq.size()>0){
            Pair rem = pq.remove();
            if(ans[rem.v] != Integer.MAX_VALUE) continue;
            ans[rem.v] = rem.wt;
            List<Pair> nbrs = graph.get(rem.v);
            for(Pair n : nbrs){
                if(ans[n.v] == Integer.MAX_VALUE) pq.add(new Pair(n.v,rem.wt+n.wt));
            }
        }
        return ans;
    }
    
    
    public List<List<Pair>> constructGraph(int[][]edges, int V){
        List<List<Pair>> graph = new ArrayList<>();
        for(int i=0; i< V; i++){
            graph.add(new ArrayList<>());
        }
        for(int i=0; i<edges.length; i++){
            int u = edges[i][0];
            int v = edges[i][1];
            int wt = edges[i][2];
            graph.get(u).add(new Pair(v,wt));
            graph.get(v).add(new Pair(u,wt));
        }
        return graph;
    }
}
class Pair implements Comparable<Pair>{
    int v;
    int wt;
    public Pair(int v, int wt){
        this.v = v;
        this.wt = wt;
    }
    @Override
    public int compareTo(Pair other) {
        return this.wt - other.wt; 
    }
}
