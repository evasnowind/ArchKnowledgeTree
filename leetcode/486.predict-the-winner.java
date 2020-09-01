/*
 * @lc app=leetcode id=486 lang=java
 *
 * [486] Predict the Winner
 */

// @lc code=start
class Solution {
    public boolean PredictTheWinner(int[] nums) {
/*
        对采用二维数组进行DP的解法进一步优化：很常规的思路，二维数组实际上并不需要。
        我们只关心上一步的计算结果。

           dp[i][j]=max(nums[i]−dp[i+1][j],nums[j]−dp[i][j−1])
           因此用一个数组记录，简化空间消耗
         */
        int length = nums.length;
        int[] dp = new int[length];
        for (int i = 0; i < length; i++) {
            dp[i] = nums[i];
        }

        for (int i = length - 2; i >= 0; i--) {
            for (int j = i+1; j < length; j++) {
                dp[j] = Math.max(nums[i] - dp[j], nums[j] - dp[j-1]);
            }
        }
        return dp[length-1] >= 0;
    }
}
// @lc code=end

