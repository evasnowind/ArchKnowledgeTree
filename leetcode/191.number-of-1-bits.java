/*
 * @lc app=leetcode id=191 lang=java
 *
 * [191] Number of 1 Bits
 */

// @lc code=start
public class Solution {
    // you need to treat n as an unsigned value
    public int hammingWeight(int n) {
        int mask = 1;
    	int sum = 0;
		for (int i = 0; i < 32; i++) {
			if((n & mask) != 0) {
				sum += 1;
			}
			mask = mask << 1;
		}
		
		return sum;
    }
}
// @lc code=end

