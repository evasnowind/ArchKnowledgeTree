/*
 * @lc app=leetcode id=38 lang=java
 *
 * [38] Count and Say
 */

// @lc code=start
class Solution {
    public String countAndSay(int n) {
        if (0 == n) {
            return "";
        } else if (1 == n) {
            return "1";
        } else {
            String lastStr = countAndSay(n - 1);
            StringBuilder builder = new StringBuilder();
            int i = 0;
            int cnt = 0;
            char ch = '0';
            while(i < lastStr.length()) {
                ch = lastStr.charAt(i);
                cnt = 1;
                i++;
                while(i < lastStr.length() && lastStr.charAt(i) == ch) {
                    cnt += 1;
                    i += 1;
                }
                builder.append(cnt).append(ch);
            }
            return builder.toString();
        }
    }

}
// @lc code=end

