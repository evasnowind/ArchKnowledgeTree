/*
 * @lc app=leetcode id=303 lang=java
 *
 * [303] Range Sum Query - Immutable
 */

// @lc code=start
class NumArray {


    private int[] prefixSum;

    public NumArray(int[] nums) {
        
        prefixSum = nums;

        for(int i = 1; i < prefixSum.length; i++) {
            prefixSum[i] += prefixSum[i-1];
        }
    }
    
    public int sumRange(int i, int j) {
        if (i == 0) {
            return prefixSum[j];
        } else {
            return prefixSum[j] - prefixSum[i-1];            
        }
    }
}

/**
 * Your NumArray object will be instantiated and called as such:
 * NumArray obj = new NumArray(nums);
 * int param_1 = obj.sumRange(i,j);
 */
// @lc code=end

