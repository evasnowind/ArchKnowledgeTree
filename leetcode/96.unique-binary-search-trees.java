/*
 * @lc app=leetcode id=96 lang=java
 *
 * [96] Unique Binary Search Trees
 */

// @lc code=start
class Solution {
    public int numTrees(int n) {
        int[] memo = new int[n+1];
        memo[0] = 1;
        memo[1] = 1;
        for (int i = 2; i <= n; i++) {
            for (int j = 1; j <= i; j++) {
                memo[i] += memo[j-1] * memo[i - j];
            }
        }
        return memo[n];
    }
}
// @lc code=end

