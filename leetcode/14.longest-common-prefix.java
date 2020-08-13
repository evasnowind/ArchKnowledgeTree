/*
 * @lc app=leetcode id=14 lang=java
 *
 * [14] Longest Common Prefix
 */

// @lc code=start
class Solution {
    public String longestCommonPrefix(String[] strs) {
        if (null == strs || strs.length == 0) {
            return "";
        }

        String curStr = strs[0];
        for(int i = 1; i < strs.length; i++) {
            int j = 0;
            for(; j < curStr.length() && j < strs[i].length(); j++) {
                if (curStr.charAt(j) != strs[i].charAt(j)) {
                    break;
                }
            }
            curStr = curStr.substring(0, j);
            if (null == curStr || curStr.equals("")) {
                return "";
            }
        }

        return curStr;
    }
}
// @lc code=end

