/*
 * @lc app=leetcode id=69 lang=java
 *
 * [69] Sqrt(x)
 */

// @lc code=start
class Solution {
    public int mySqrt(int x) {
        if (0 == x) {
			return 0;
		}

		int halfOfX = x / 2 + 1;
		int tmpResult = 0;
		int result = 0;
		for (int i = 1; i <= halfOfX; i++) {
			tmpResult = i * i;
			if (tmpResult == x) {
				return i;
			} else if (tmpResult > x) {
				result = i - 1;
				break;
			}
		}

		return result;
    }
}
// @lc code=end

