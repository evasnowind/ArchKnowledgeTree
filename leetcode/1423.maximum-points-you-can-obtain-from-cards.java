/*
 * @lc app=leetcode id=1423 lang=java
 *
 * [1423] Maximum Points You Can Obtain from Cards
 */

// @lc code=start
class Solution {
    public int maxScore(int[] cardPoints, int k) {
        int n = cardPoints.length;
        int windowSize = n - k;
        int sum = 0;
        for(int i = 0; i < windowSize; i++) {
            sum += cardPoints[i];
        }
        int minSum = sum;
        for(int i = windowSize; i < n; i++) {
            sum = sum + cardPoints[i] - cardPoints[i - windowSize];
            minSum = Math.min(minSum, sum);
        }

        return Arrays.stream(cardPoints).sum() - minSum;
    }
}
// @lc code=end

