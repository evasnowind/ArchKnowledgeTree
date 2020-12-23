/*
 * @lc app=leetcode id=746 lang=java
 *
 * [746] Min Cost Climbing Stairs
 */

// @lc code=start
class Solution {
    public int minCostClimbingStairs(int[] cost) {
        int n = cost.length;
        int pre = 0, cur = 0;
        for(int i = 2; i <= n; i++) {
            int next = Math.min(pre + cost[i-2], cur + cost[i-1]);
            pre = cur;
            cur = next;
        }
        return cur;
    }
}
// @lc code=end

