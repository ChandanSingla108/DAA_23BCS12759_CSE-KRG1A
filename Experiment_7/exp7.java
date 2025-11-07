class Solution {
    static int knapsack(int W, int val[], int wt[]) {
        int[][]dp = new int[val.length][W+1];
        for(int[]d : dp){
            Arrays.fill(d,-1);
        }
        return helper(W,val,wt,dp,val.length-1);
    }
    static int helper(int k, int[] val, int[] wt, int[][]dp, int i){
        if(i<0 || k==0) return 0;
        if(dp[i][k]!=-1) return dp[i][k];
        int x = helper(k,val,wt,dp,i-1); // excluded
        int y = 0;
        if(wt[i]<= k){
            y = helper(k-wt[i],val,wt,dp,i-1)+val[i]; // included 
        }
        dp[i][k] = Math.max(x,y);
        return Math.max(x,y);
    }
}
