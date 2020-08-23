/*
 * @lc app=leetcode id=122 lang=java
 *
 * [122] Best Time to Buy and Sell Stock II
 */

// @lc code=start
class Solution {
    public int maxProfit(int[] prices) {
        if (null == prices || prices.length == 0) {
            return 0;
        }

        int n = prices.length;
        //dp[i][0]
        int dpHasNoStock = 0;
        //dp[i][1] 设置为Integer.MIN_VALUE 表示一开始不可能拥有股票，必须先买后卖
        int dpHasStock = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            int tmp = dpHasNoStock;
            dpHasNoStock = Math.max(dpHasNoStock, dpHasStock + prices[i]);
            dpHasStock = Math.max(dpHasStock, tmp - prices[i]);
        }
        return dpHasNoStock;
    }
}
// @lc code=end

