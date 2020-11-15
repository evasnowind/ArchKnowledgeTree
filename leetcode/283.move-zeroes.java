/*
 * @lc app=leetcode id=283 lang=java
 *
 * [283] Move Zeroes
 */

// @lc code=start
class Solution {
    public void moveZeroes(int[] nums) {
        if (1 == nums.length) {
            return;
        }

        int nextIdx = 0;
        /*
        nextIdx保存的是当前非零位置的最后一个位置。
        其实也就是每次遍历到一个新元素，如果该元素是0，跳过；如果非0，
        则需要与nextId所在位置替换，然后nextId加1，这样可以保证
        将所有0逐步替换到数组后半部分
        */
        for(int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                int tmp = nums[i];
                nums[i] = nums[nextIdx];
                nums[nextIdx] = tmp;
                nextIdx++;
            }
        }
    }
}
// @lc code=end

