/*
 * @lc app=leetcode id=136 lang=java
 *
 * [136] Single Number
 */

// @lc code=start
class Solution {
    public int singleNumber(int[] nums) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        int res = 0;
        for (int n : nums) {
            res = res ^ n;
        }

        return res;
    }
}
// @lc code=end

