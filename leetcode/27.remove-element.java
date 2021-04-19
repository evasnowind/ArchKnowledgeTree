/*
 * @lc app=leetcode id=27 lang=java
 *
 * [27] Remove Element
 */

// @lc code=start
class Solution {
    public int removeElement(int[] nums, int val) {
        if (null == nums || nums.length == 0) {
            return 0;
        }

        /*
        很直观的思路：利用双指针，左指针从左往右遍历，
        指向当前元素，右指针从右往左，如果当前元素与
        val相同，则与将右指针指向的元素放到当前位置，
        进行覆盖，这个过程会持续、直到找到一个不是val的值
        赋值给左指针指向元素。
        */
        int i = 0, pivot = nums.length;
        while (i < pivot) {
            if (nums[i] == val) {
                nums[i] = nums[pivot - 1];
                pivot--;
            } else {
                i++;
            }
        }

        return pivot;
    }
}
// @lc code=end

