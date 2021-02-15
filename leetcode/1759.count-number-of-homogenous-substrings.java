/*
 * @lc app=leetcode id=1759 lang=java
 *
 * [1759] Count Number of Homogenous Substrings
 */

// @lc code=start
class Solution {
    public int countHomogenous(String s) {
        int len = s.length();
        int mod = 1_0000_0000_7;
        long count = 0;
        int i = 0, j = 0;
        while(i < len) {
            char ch = s.charAt(i);
            /*
            找到相同的所有字符
             */
            while(j < len && s.charAt(j) == ch) {
                j++;
            }
            long n = j - i;
            count += n * (n+1) / 2;
            i = j;
        }
        return (int)(count % mod);
    }
}
// @lc code=end

