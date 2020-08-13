/*
 * @lc app=leetcode id=53 lang=java
 *
 * [53] Maximum Subarray
 */

// @lc code=start
class Solution {
    public int maxSubArray(int[] nums) {
        if (null == nums || nums.length == 0) {
            return Integer.MIN_VALUE;
        }
        //类似寻找最大最小值的题目，初始值一定要定义成理论上的最小最大值
        int maxRes = Integer.MIN_VALUE;
        //dp[i]表示第i个位置时以nums[i]结尾的最大子序和
        int[] dp = new int[nums.length];
        dp[0] = nums[0];
        int res = dp[0];
        for (int i = 1; i < nums.length; i++) {
            /*
            dp[i]表示第i个位置时以nums[i]结尾的最大子序和，则转移公式：dp[i] = max(dp[i-1] + nums[i], nums[i])
            注意dp数组不一定定义成最终结果，它只是一个辅助数组，找出递归关系。
             */
            dp[i] = Math.max(dp[i-1] + nums[i], nums[i]);
            res = Math.max(dp[i], res);
        }
        return res;
    }
}
// @lc code=end

