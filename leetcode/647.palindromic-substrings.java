/*
 * @lc app=leetcode id=647 lang=java
 *
 * [647] Palindromic Substrings
 */

// @lc code=start
class Solution {
    public int countSubstrings(String s) {
        if (null == s || s.length() == 0) {
            return 0;
        }

        int cnt = 0;
        //找到所有子串
        Set<String> palindromeSet = new HashSet<>();;
        for (int i = 0; i < s.length(); i++) {
            for (int j = i + 1; j <= s.length(); j++) {
                String subStr = s.substring(i, j);
                if (palindromeSet.contains(subStr)) {
                    //引入记忆集，减少重复判断
                    cnt += 1;
                } else if (isPalindromicString(subStr)) {
                    //判断每个子串是否为回文字符串
                    cnt += 1;
                    palindromeSet.add(subStr);
                }
            }
        }

        return cnt;
    }

    private boolean isPalindromicString(String str) {
        if (str.length() == 1) {
            return true;
        }

        char[] chs = str.toCharArray();
        for (int i = 0; i < chs.length / 2; i++) {
            //示例1：length = 3, 折半 len / 2 = 1, 0 <= i <1; 另一半则需要是length - i - 1
            //示例2：length = 4，折半 len / 2 = 2, 0 <= i < 2  [length-2, length-1]
            //所以无论奇偶数，i<length/2搭配 length - i -1都没有问题
            if (chs[i] != chs[chs.length - i - 1]) {
                return false;
            }
        }

        return true;
    }
}
// @lc code=end

