/*
 * @lc app=leetcode id=665 lang=java
 *
 * [665] Non-decreasing Array
 */

// @lc code=start
class Solution {
    /**
     * 
     * 目前看到的比较好懂的解析是这篇：
     * https://leetcode-cn.com/problems/non-decreasing-array/solution/tu-jie-zheng-ming-zui-duo-yi-ge-feng-gu-6l32k/
     * 
     * 此处需要分情况讨论。
     * 先扫描数组：
     * 1. 如果数组本身就是非递减数组，直接返回true
     * 2. 如果不是，需要判断是否有多于1个峰/谷的变化，如果大于1则不可能变成非递减数组，直接返回即可，有1个峰/谷则需要继续判断
     *  2.1 此处可以采用双指针、从两边夹逼的方式，如果找到峰/谷后，两个指针的差大于1说明有多于1个峰/谷
     *  2.2 另一种思路：不用双指针、直接从前往后遍历。判断可能稍显复杂些
     * 3. 如果只有1个峰/谷，则需要判断
     * 3.1 如果在第0个位置，或是在第n-1个位置，则必然可以修改成非递减数组
     * 3.2 如果是在中间，则需要寻找上下限。此处分析可以参考https://leetcode-cn.com/problems/non-decreasing-array/solution/tu-jie-zheng-ming-zui-duo-yi-ge-feng-gu-6l32k/
     * 关键的点在于：有两种情况，只要满足其中一种即可(设峰值为i，对应A[i], 谷值为i+1，对应A[i+1])：
     * （1）若将“峰”接到“谷”的“下面”：则必须满足A[i-1] <= A`[i] <= A[i+1]
     * （2）若将“谷”接到“峰”的“上面”：则必须满足A[i] <= A`[i+1] <= A[i+2]
     * 
     * @param nums
     * @return
     */
    public boolean checkPossibility(int[] nums) {
        int n = nums.length;
        int left = 0, right = n - 1;
        while(left < n - 1 && nums[left] <= nums[left+1]) {
            left++;
        }
        while(right > 0 && nums[right-1] <= nums[right]) {
            right--;
        }
        /*
        数组本身就是非递减，直接满足要求
        */
        if (n - 1 == left) {
            return true;
        }
        /*
        有两个峰谷，肯定无法调整成非递减数组
        */
        if (right - left + 1 > 2) {
            return false;
        }
        /*
        只有一个峰谷，且在数组最左或是最右，则必然可以
        */
        if (0 == left || n - 1 == right) {
            return true;
        }
        /*
        峰谷在中间的位置，判断是否只需调整一个数字
        */
        if (nums[right+1] >= nums[left] || nums[left-1] <= nums[right]) {
            return true;
        }
        return false;
    }
}
// @lc code=end

