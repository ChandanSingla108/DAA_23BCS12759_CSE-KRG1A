class Solution {
    public ArrayList<ArrayList<Integer>> countFreq(int[] arr) {
        ArrayList<ArrayList<Integer>> ans = new ArrayList<>();
        HashMap<Integer,Integer>fmap = new HashMap<>();
        for(int i=0; i<arr.length; i++){
            fmap.put(arr[i],fmap.getOrDefault(arr[i],0)+1);
        }
        for(int k : fmap.keySet()){
            ArrayList<Integer>temp = new ArrayList<>();
            temp.add(k);
            temp.add(fmap.get(k));
            ans.add(temp);
        }
        return ans;
    }
}
