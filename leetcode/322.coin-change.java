/*
 * @lc app=leetcode id=322 lang=java
 *
 * [322] Coin Change
 */

// @lc code=start
class Solution {

    

    public int coinChange(int[] coins, int amount) {
        if (amount < 1) {
            return 0;
        }

        return dp(coins, amount, new int[amount]);
    }

    private int dp(int[] coins, int amount, int[] memo) {
        if (amount < 0) {
            return -1;
        }
        if (amount == 0) {
            return 0;
        }
        if (memo[amount - 1] != 0) {
            return memo[amount - 1];
        }
        
        int min = Integer.MAX_VALUE;
        for(int coin : coins) {
            int res = dp(coins, amount - coin, memo);
            if (res >= 0 && res < min) {
                min = res + 1;
            }
        }
        memo[amount - 1] = (min == Integer.MAX_VALUE) ? -1 : min;
        return memo[amount - 1];
    }
}
// @lc code=end

