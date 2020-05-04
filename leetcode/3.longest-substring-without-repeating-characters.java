/*
 * @lc app=leetcode id=3 lang=java
 *
 * [3] Longest Substring Without Repeating Characters
 */

// @lc code=start
class Solution {
    public int lengthOfLongestSubstring(String s) {
        if (null == s || s.length() == 0) {
            return 0;
        }
        Set<Character> scannedChars = new HashSet<>();
        int i = 0, j = 0, maxLength = 0;

        while(j < s.length()) {
            if (!scannedChars.contains(s.charAt(j))) {
                scannedChars.add(s.charAt(j));
                maxLength = Math.max(maxLength, scannedChars.size());
                j += 1;
            } else {
                scannedChars.remove(s.charAt(i));
                i += 1;
            }
        }

        return maxLength;
    }
}
// @lc code=end

