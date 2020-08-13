/*
 * @lc app=leetcode id=242 lang=java
 *
 * [242] Valid Anagram
 */

// @lc code=start
class Solution {
    public boolean isAnagram(String s, String t) {
        //判空、一个空一个不为空的这种防御式代码懒得写了
        if (s.length() != t.length()) {
            return false;
        }

        char[] sCh = s.toCharArray();
        char[] tCh = t.toCharArray();
        Arrays.sort(sCh);
        Arrays.sort(tCh);

        return Arrays.equals(sCh, tCh);
    }
}
// @lc code=end

