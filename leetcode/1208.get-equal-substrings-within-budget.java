/*
 * @lc app=leetcode id=1208 lang=java
 *
 * [1208] Get Equal Substrings Within Budget
 */

// @lc code=start
class Solution {
    /**
     * 此题相对比较简单。只要认真读题、弄清意思即可。
     * 实际上就是说每次只能变换一个字符，然后找出一个
     * 可以变换的子字符串，使其开销小于给定值。
     * 这里子串就可以使用滑动窗口思想。
     * 而没给字符变换的开销则可以实现计算出来。
     * 两者一结合即可得到代码。
     * 
     * @param s
     * @param t
     * @param maxCost
     * @return
     */
    public int equalSubstring(String s, String t, int maxCost) {
        //输入已保证会大于0，并且两个字符串长度相同，此处略去判空

        //计算出两个字符串每个字符变换的开销
        int len = s.length();
        int[] diff = new int[len];
        char[] ch1 = s.toCharArray();
        char[] ch2 = t.toCharArray();
        for(int i = 0; i < len; i++) {
            diff[i] = Math.abs(ch1[i] - ch2[i]);
        }

        //滑动窗口，求窗口最大值
        int maxLen = 0, curCost = 0, right = 0, left = 0;
        while(right < len) {
            curCost += diff[right];
            while(curCost > maxCost) {
                curCost -= diff[left];
                left++;
            }

            maxLen = Math.max(maxLen, right - left + 1);
            right++;
        }

        return maxLen;
    }
}
// @lc code=end

