/*
 * @lc app=leetcode id=120 lang=java
 *
 * [120] Triangle
 */

// @lc code=start
class Solution {
    public int minimumTotal(List<List<Integer>> triangle) {
        if (null == triangle || triangle.size() == 0) {
			return 0;
		}

		int n = triangle.size();
		int[] memo = new int[n + 1];
		for (int i = n - 1; i >= 0; i--) {
			for (int j = 0; j <= i; j++) {
				memo[j] = Math.min(memo[j], memo[j+1]) + triangle.get(i).get(j);
			}
		}
		return memo[0];
    }
}
// @lc code=end

