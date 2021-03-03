/*
 * @lc app=leetcode id=338 lang=java
 *
 * [338] Counting Bits
 */

// @lc code=start
class Solution {
    /**
     * 思路1：
     * 可以利用x&(x-1)的方法计算一个数字所包含的二进制位1
     * 个数。
     * 但该方法时间复杂度是O(n*num) num为二进位个数
     * 
     * 思路2：
     * 动态规划，可以用数组保存每个数字的二进制位个数。
     * 容易想到的一点是，二进制位由于是依次递增的，那
     * 从0开始的数字之间是有关联关系。
     * 比如说，x/2之后，是将其最高位的1去掉，当然也会
     * 将x最低位的1抹去、因此需要加回来：
     * （1）x是偶数时，不用加，因为末尾是0
     * （2）x是奇数时，需要加，因为末尾是1
     * 统一上述条件，可以用 +(x&1) 来控制
     * 
     * 因此，利用动态规划的思想，从最小的数字开始，一点点
     * 累计。利用 cnt[i] = cnt[i>>1] + (i & 1)
     * 即可统一计算公式。
     * 
     * 
     * 
     * @param num
     * @return
     */
    public int[] countBits(int num) {
        int[] cnt = new int[num + 1];
        cnt[0] = 0;
        for(int i = 1; i <= num; i++) {
            cnt[i] = cnt[i>>1] + (i & 1);
        }
        return cnt;
    }
}
// @lc code=end

