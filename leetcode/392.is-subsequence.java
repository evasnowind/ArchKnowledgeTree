/*
 * @lc app=leetcode id=392 lang=java
 *
 * [392] Is Subsequence
 */

// @lc code=start
class Solution {
    public boolean isSubsequence(String s, String t) {
        if (null == t || null == s) {
            return false;
        }

        int tLen = t.length(), sLen = s.length();
        int i = 0, j = 0;
        while(i < tLen && j < sLen) {
            if (s.charAt(j) == t.charAt(i)) {
                j += 1;
            }
            i += 1;
        }
        return j == sLen;
    }
}
// @lc code=end

