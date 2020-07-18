import java.util.Arrays;

/*
 * @lc app=leetcode id=268 lang=java
 *
 * [268] Missing Number
 */

// @lc code=start
class Solution {
    public int missingNumber(int[] nums) {
        Arrays.sort(nums);

        // 判断 n 是否出现在末位
        if (nums[nums.length-1] != nums.length) {
            return nums.length;
        } else if (nums[0] != 0) {
             // 判断 0 是否出现在首位
            return 0;
        }

        for (int i = 0; i < nums.length - 1; i++) {
            int expectNum = nums[i] + 1;
            if (expectNum != nums[i+1]) {
                return expectNum;
            }
        }
        return -1;
    }
}
// @lc code=end

