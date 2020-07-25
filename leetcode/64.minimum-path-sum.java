/*
 * @lc app=leetcode id=64 lang=java
 *
 * [64] Minimum Path Sum
 */

// @lc code=start
class Solution {
    public int minPathSum(int[][] grid) {
        if (null == grid || 0 == grid.length || 0 == grid[0].length) {
            return 0;
        }

        int rowSize = grid.length;
        int colSize = grid[0].length;
        int[][] dp = new int[rowSize][colSize];
        dp[0][0] = grid[0][0];
        for (int i = 1; i < rowSize; i++) {
            dp[i][0] = dp[i-1][0] + grid[i][0];
        }
        for (int j = 1; j < colSize; j++) {
            dp[0][j] = dp[0][j-1] + grid[0][j];
        }
        for (int i = 1; i < rowSize; i++) {
            for (int j = 1; j < colSize; j++) {
                dp[i][j] = Math.min(dp[i-1][j], dp[i][j-1]) + grid[i][j];
            }
        }
        return dp[rowSize - 1][colSize - 1];
    }
}
// @lc code=end

