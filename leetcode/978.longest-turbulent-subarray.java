/*
 * @lc app=leetcode id=978 lang=java
 *
 * [978] Longest Turbulent Subarray
 */

// @lc code=start
class Solution {
    /**
     * 采用滑动窗口思想。
     * 其实就本质而言，本题并不难，只需要弄清楚
     * 滑动窗口滑动时的边界条件。
     * 1、右边界滑动条件：
     * arr[i-1] < arr[i] && arr[i] > arr[i+1]
     * 或者arr[i-1] > arr[i] && arr[i] < arr[i+1]
     * 
     * 2、左边界滑动条件：
     * 2.1 左右边界在同一个位置，且arr[right+1] = arr[right]
     * 2.2 右边界无法继续滑动，需挪动到right位置，这两者重新开始
     * 
     * @param arr
     * @return
     */
    public int maxTurbulenceSize(int[] arr) {
        if (null == arr || arr.length == 0) {
            return 0;
        }

        /*
        注意边界条件：如果数组只有1个元素，那么
        也是一个湍流子数组
        */
        int res = 1;
        int left = 0, right = 0;
        int len = arr.length;

        while(right < len - 1) {
            if (left == right) {
                /*
                两个边界重合，右边界需要滑动。
                但需要注意两个相邻元素相等的情况，
                这种情况需要将left同时挪动，避免重复判断。
                */
                if (arr[right] == arr[right+1]) {
                    left++;
                }
                right++;
            } else {
                if (arr[right-1] < arr[right] && arr[right] > arr[right+1]) {
                    right++;
                } else if (arr[right-1] > arr[right] && arr[right] < arr[right+1]) {
                    right++;
                } else {
                    left = right;
                }
            }

            res = Math.max(res, right - left + 1);
        }
        
        return res;
    }
}
// @lc code=end

