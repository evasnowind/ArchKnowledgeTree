/*
 * @lc app=leetcode id=69 lang=java
 *
 * [69] Sqrt(x)
 */

// @lc code=start
class Solution {
    public int mySqrt(int x) {
        if(0 == x) {
			return 0;
		}
			
		int low = 1, high = x, result = 0;
		while(low <= high){
			int mid = low + (high - low) / 2;
			if(mid <= x / mid){
				low = mid + 1;
				result = mid;
			} else {
				high = mid - 1;
			}
		}
		return result;
    }
}
// @lc code=end

