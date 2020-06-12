/*
 * @lc app=leetcode id=70 lang=java
 *
 * [70] Climbing Stairs
 */

// @lc code=start
class Solution {
    public int climbStairs(int n) {
        if (0 >= n) {
            return 0;
        } else if (1 == n) {
            return 1;
        } else if (2 == n) {
            return 2;
        }
        int oneStepBefore = 2;
        int twoStepBefore = 1;
        int allSolution = 0;
        for (int i = 2; i < n; i++) {
            allSolution = oneStepBefore + twoStepBefore;
            twoStepBefore = oneStepBefore;
            oneStepBefore = allSolution;            
        }
        return allSolution;
    }
}
// @lc code=end

