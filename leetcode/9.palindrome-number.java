/*
 * @lc app=leetcode id=9 lang=java
 *
 * [9] Palindrome Number
 */

// @lc code=start
class Solution {
    public boolean isPalindrome(int x) {
        //0. 根据定义，负数不可能是回文数，直接返回
		if (x < 0) {
			return false;
		}
		//1. 先计算出最高位对应的整数，以便取整。注意边界条件
		int div = 1;
		int tmpX = x;
		while (tmpX >= 10) {
			div = div * 10;
			tmpX = tmpX / 10;
		}

		/**
		 2. 循环，比较数字两边是否一致，不一致返回false；若一致，则继续比较下一个数字。
		 注意，比较下一个数字时，需要剔除掉最高位、最低位两个数字，需要先取余div去掉首位、然后再除以10去掉末尾。
		 因此，终止条件是tmpX变成0
		 */


		int left = 0, right = 0;
		while(x > 0) {
			left = x / div;
			right = x % 10;
			if (left != right) {
				return false;
			}

			x = (x % div) / 10;
			div = div / 100;
		}

		return true;
    }
}
// @lc code=end

