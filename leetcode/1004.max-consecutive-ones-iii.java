/*
 * @lc app=leetcode id=1004 lang=java
 *
 * [1004] Max Consecutive Ones III
 */

// @lc code=start
class Solution {
    public int longestOnes(int[] A, int K) {
        int len = A.length;
        if (len < 2) {
            return len;
        }

        int left = 0, right = 0, zeroCnt = 0, res = 0;
        while(right < len) {
            if (A[right] == 0) {
                zeroCnt++;
            }
            while(zeroCnt > K) {
                if (A[left] == 0) {
                    zeroCnt--;
                }
                left++;
            }
            res = Math.max(res, right - left + 1);
            right++;
        }
        return res;
    }
}
// @lc code=end

