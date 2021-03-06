import java.util.Deque;

/*
 * @lc app=leetcode id=503 lang=java
 *
 * [503] Next Greater Element II
 */

// @lc code=start
class Solution {
    public int[] nextGreaterElements(int[] nums) {
        if (null == nums || nums.length == 0) {
            return nums;
        }

        int len = nums.length;
        int[] res = new int[len];
        Deque<Integer> stack = new LinkedList<>();

        /*
        思想上跟next greater number 一样，但由于要考虑循环，一种思路是
        将数组复制一遍，比如
        [2,1,2,4,3]变成[2,1,2,4,3,2,1,2,4,3]
        但可以利用取模，变相达到这个效果。
         */
        for (int i = len * 2 - 1; i >= 0; i--) {
            while(!stack.isEmpty() && stack.peek() <= nums[i % len]) {
                stack.pop();
            }
            res[i % len] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(nums[i % len]);
        }
        return res;
    }
}
// @lc code=end

