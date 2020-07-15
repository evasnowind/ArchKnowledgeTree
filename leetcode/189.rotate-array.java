/*
 * @lc app=leetcode id=189 lang=java
 *
 * [189] Rotate Array
 */

// @lc code=start
class Solution {
    public void rotate(int[] nums, int k) {
        if (null == nums || nums.length == 0 || k <= 0) {
            return;
        }

        int privot = k % nums.length;
        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, privot - 1);
        reverse(nums, privot, nums.length - 1);
    }

    private void reverse(int[] nums, int start, int end) {
        int tmp = 0;
        while(start < end) {
            tmp = nums[start];
            nums[start] = nums[end];
            nums[end] = tmp;
            start += 1;
            end -= 1;
        }
    }
}
// @lc code=end

