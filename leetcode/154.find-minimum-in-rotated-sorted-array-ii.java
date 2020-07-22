/*
 * @lc app=leetcode id=154 lang=java
 *
 * [154] Find Minimum in Rotated Sorted Array II
 */

// @lc code=start
class Solution {
    public int findMin(int[] nums) {
        int left = 0, right = nums.length - 1;
        while(left <= right) {
            int mid = left + (right - left) / 2;
            if(nums[mid] > nums[right]) {
                //说明当前mid在左半边，比right元素要高，可以忽略左边
                left = mid + 1;
            } else if (nums[mid] < nums[right]) {
                //说明当前mid在右半边，比right元素要低，需要忽视右边。注意，由于最小值可能刚好是mid当前指向元素，所以不能跳过mid。
                right = mid;
            } else if (nums[mid] == nums[right]) {
                /*
                可能出现多个相等元素的情况，此时不一定mid就是解，可能刚好mid right相等、但最小值还比他们小。
                此时可以忽略right，往左边挪动一下
                 */
                right = right - 1;
            }
        }
        return nums[left];
    }
}
// @lc code=end

