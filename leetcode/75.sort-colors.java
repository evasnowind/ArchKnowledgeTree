/*
 * @lc app=leetcode id=75 lang=java
 *
 * [75] Sort Colors
 */

// @lc code=start
class Solution {
    public void sortColors(int[] nums) {
        if (null == nums || nums.length == 0) {
            return;
        }

        int cur = 0, left = 0, right = nums.length - 1;
        while(cur <= right) {
            if (nums[cur] == 0) {
                swap(nums, cur, left);
                left++;
                cur++;
            } else if (nums[cur] == 2) {
                swap(nums, cur, right);
                right--;
            } else {
                cur++;
            }
        }
    
    }

    private void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }
}
// @lc code=end

