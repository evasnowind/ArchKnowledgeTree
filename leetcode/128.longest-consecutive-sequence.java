/*
 * @lc app=leetcode id=128 lang=java
 *
 * [128] Longest Consecutive Sequence
 */

// @lc code=start
class Solution {
    public int longestConsecutive(int[] nums) {
        Set<Integer> allElementSet = new HashSet<>();

        for (int n : nums) {
            allElementSet.add(n);
        }

        int longestLen = 0;
        for (int n : nums) {
            if (!allElementSet.contains(n - 1)) {
                int curElement = n;
                int curLen = 0;
                while(allElementSet.contains(curElement)) {
                    curElement += 1;
                    curLen += 1;
                }

                longestLen = Math.max(longestLen, curLen);
            }
        }

        return longestLen;   
    }
}
// @lc code=end

