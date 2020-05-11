/*
 * @lc app=leetcode id=50 lang=java
 *
 * [50] Pow(x, n)
 */

// @lc code=start
class Solution {
    public double myPow(double x, int n) {
        if (n < 0) {
            x = 1 / x;
            n = -n;
        }

        return helper(x, n);
    }

    private double helper(double x, int n) {
        if (0 == n) {
            return 1;
        }
        if (1 == n) {
            return x;
        }

        if ((n & 1) == 1) {
            return x * helper(x * x, n / 2);
        } else {
            return helper(x * x, n / 2);
        }
    }
}
// @lc code=end

