/*
 * @lc app=leetcode id=344 lang=java
 *
 * [344] Reverse String
 */

// @lc code=start
class Solution {
    public void reverseString(char[] s) {
        if (null == s) {
            return;
        }

        int len = s.length;
        for (int i = 0; i < len / 2; i++) {
            char tmpCh = s[s.length - i - 1];
            s[s.length - i - 1] = s[i];
            s[i] =tmpCh;
        }
    }
}
// @lc code=end

