/*
 * @lc app=leetcode id=7 lang=java
 *
 * [7] Reverse Integer
 */

// @lc code=start
class Solution {

    public int reverse(int x) {
        int res = 0, reminder = 0;
        int maxReminder = Integer.MAX_VALUE % 10;
        int minReminder = Integer.MIN_VALUE % 10;
        while(x != 0) {
            reminder = x % 10;
            if (res > Integer.MAX_VALUE / 10 || (res == Integer.MAX_VALUE / 10 && reminder > maxReminder)) {
                return 0;
            }
            if (res < Integer.MIN_VALUE / 10 || (res == Integer.MIN_VALUE / 10 && reminder < minReminder)) {
                return 0;
            }
            res = res * 10 + reminder;
            x = x / 10;
        }
        return res;
    }
}
// @lc code=end

