/*
 * @lc app=leetcode id=191 lang=java
 *
 * [191] Number of 1 Bits
 */

// @lc code=start
public class Solution {
	/**
	 * 思路1：
	 * 直接计算每一位是否为1，因此需要一个掩码mask
	 * 来获得只有一位是1的数去&。
	 * 
	 * 思路2：
	 * 利用n&(n-1)，每次可以消去最后一位1的特性，
	 * 加速计算过程。
	 * 
	 * @param n
	 * @return
	 */
    // you need to treat n as an unsigned value
    public int hammingWeight(int n) {
        // int mask = 1;
    	// int sum = 0;
		// for (int i = 0; i < 32; i++) {
		// 	if((n & mask) != 0) {
		// 		sum += 1;
		// 	}
		// 	mask = mask << 1;
		// }
		
		// return sum;

		int sum = 0;

        while(n != 0) {
            sum++;
            n = n & (n-1);
        }
        return sum;
    }
}
// @lc code=end

