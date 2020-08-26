/*
 * @lc app=leetcode id=201 lang=java
 *
 * [201] Bitwise AND of Numbers Range
 */

// @lc code=start
class Solution {
    public int rangeBitwiseAnd(int m, int n) {
        /*
        利用求公共前缀的思路

由于只要有一位是0，所有数字在该位上的与操作结果都是0。问题可以转化为：找到这个区间内所有数字的最长公共前缀，因为非公共部分是既有0、又有1。
有m n是这个区间的最小、最大值，只用求这两个即可获得所有数字的最长公共前缀。

而求最长公共前缀有两种思路：
1、m n移位运算，算出移动多少位时两者相等
2、利用n&(n-1)可以消除n末尾1，不断消减n末尾的1，直至n<=m
        */
        while(m < n) {
            n = n & (n - 1);
        }
        return n;
    }
}
// @lc code=end

