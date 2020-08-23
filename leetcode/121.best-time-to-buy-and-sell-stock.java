/*
 * @lc app=leetcode id=121 lang=java
 *
 * [121] Best Time to Buy and Sell Stock
 */

// @lc code=start
class Solution {
    public int maxProfit(int[] prices) {
/*
        int minPrice = Integer.MAX_VALUE;
        int maxProfit = 0;
        for(int i = 0; i < prices.length; i++) {
            if (minPrice > prices[i]) {
                minPrice = prices[i];
            } else if (prices[i] - minPrice > maxProfit) {
                maxProfit = prices[i] - minPrice;
            }
        }

        return maxProfit;
*/

/*
参考 https://labuladong.gitbook.io/algo/di-ling-zhang-bi-du-xi-lie/tuan-mie-gu-piao-wen-ti
	k表示最多交易多少次。
	
	状态方程
	 dp[i][1][0] = max(dp[i-1][1][0], dp[i-1][1][1] + prices[i])
	 dp[i][1][1] = max(dp[i-1][1][1], dp[i-1][0][0] - prices[i])
	 			= max(dp[i-1][1][1], -prices[i])
	 解释：k = 0 的 base case，所以 dp[i-1][0][0] = 0。

	 现在发现 k 都是 1，不会改变，即 k 对状态转移已经没有影响了。
	 可以进行进一步化简去掉所有 k：
	 dp[i][0] = max(dp[i-1][0], dp[i-1][1] + prices[i])
	 dp[i][1] = max(dp[i-1][1], -prices[i])

	 - 今天不持有 = max(昨天不持有，昨天持有、今天卖出)
	 - 今天持有 = max(昨天持有， 昨天不持有、今天买入)
*/
/*
        if (null == prices || prices.length == 0) {
            return 0;
        }
        int n = prices.length;
        int[][] dp = new int[n][2];
        //直接将i=0提前处理，避免dp[i-1]在循环时报错
        dp[0][0] = 0;
        dp[0][1] = -prices[0];
        for (int i = 1; i < n; i++) {
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1] + prices[i]);
            dp[i][1] = Math.max(dp[i-1][1], -prices[i]);
        }
        return dp[n-1][0];
*/
        
        /*
		新状态只和相邻的一个状态有关，其实不用整个 dp 数组，只需要一个变量储存相邻的那个状态就足够了，这样可以把空间复杂度降到 O(1):
		 */
		int n = prices.length;
		// base case: dp[-1][0] = 0, dp[-1][1] = -infinity
		int dp_i_0 = 0, dp_i_1 = Integer.MIN_VALUE;
		for (int i = 0; i < n; i++) {
			// dp[i][0] = max(dp[i-1][0], dp[i-1][1] + prices[i])
			dp_i_0 = Math.max(dp_i_0, dp_i_1 + prices[i]);
			// dp[i][1] = max(dp[i-1][1], -prices[i])
			dp_i_1 = Math.max(dp_i_1, -prices[i]);
		}
		return dp_i_0;
    }
}
// @lc code=end

