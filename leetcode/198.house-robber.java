/*
 * @lc app=leetcode id=198 lang=java
 *
 * [198] House Robber
 */

// @lc code=start
class Solution {
    public int rob(int[] nums) {      
        int prevNo = 0;//不抢当前屋子所得最大值
	    int prevYes = 0;//抢当前屋子所得最大值
	    for (int n : nums) {
	        int temp = prevNo;
	        prevNo = Math.max(prevNo, prevYes);//不抢当前，则最大值应为上个屋子抢、不抢所得最大值
	        prevYes = n + temp;//抢当前，则最大值应为上个屋子不抢+当前屋子
	    }
	    return Math.max(prevNo, prevYes);
    }
}
// @lc code=end

