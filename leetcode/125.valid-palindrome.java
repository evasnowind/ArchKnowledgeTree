/*
 * @lc app=leetcode id=125 lang=java
 *
 * [125] Valid Palindrome
 */

// @lc code=start
class Solution {
    public boolean isPalindrome(String s) {
        if (null == s || s.trim() == "") {
            return true;
        }

        char[] chs = s.toCharArray();
        int start = 0, end = chs.length - 1;
        while (start <= end) {
            //另一种写法：在while循环内再开两个小循环，分别找是字母或是数字的start / end位置。但本质和当前实现也一样
            if (!Character.isLetterOrDigit(chs[start])) {
                start += 1;
            } else if (!Character.isLetterOrDigit(chs[end])) {
                end -= 1;
            } else {
                if (Character.toLowerCase(chs[start]) != Character.toLowerCase(chs[end])) {
                    return false;
                } else {
                    start += 1;
                    end -= 1;
                }
            }

        }

        return true;
    }
}
// @lc code=end

