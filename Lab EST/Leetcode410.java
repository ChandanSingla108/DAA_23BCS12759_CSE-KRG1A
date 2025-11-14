class Solution {
    public int splitArray(int[] nums, int m) {
        int low = 0, high = 0;
        for (int x : nums) { low = Math.max(low, x); high += x; }

        while (low < high) {
            int mid = low + (high - low) / 2;   
            if (canSplit(nums, m, mid)) {
                high = mid;                     
            } else {
                low = mid + 1;                  
            }
        }
        return low;                              
    }

    private boolean canSplit(int[] nums, int m, int cap) {
        int parts = 1, sum = 0;
        for (int x : nums) {
            if (sum + x <= cap) {
                sum += x;                       
            } else {
                parts++;                        
                sum = x;
                if (parts > m) return false;    
            }
        }
        return true;                            
    }
}
