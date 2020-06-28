/*
 * @lc app=leetcode id=209 lang=java
 *
 * [209] Minimum Size Subarray Sum
 */

// @lc code=start
class Solution {
    public int minSubArrayLen(int s, int[] nums) {
        if (null == nums || 0 == nums.length) {
            return 0;
        }

        int start = 0, end = 0, minLen = Integer.MAX_VALUE, sum = 0;
        while(end < nums.length) {
            sum += nums[end];

            while(sum >= s) {
                minLen = Math.min(minLen, end - start + 1);
                sum -= nums[start];
                start += 1;
            }

            end += 1;
        }

        return minLen == Integer.MAX_VALUE ? 0 : minLen;
    }
}
// @lc code=end

