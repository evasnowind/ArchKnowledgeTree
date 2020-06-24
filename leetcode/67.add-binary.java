/*
 * @lc app=leetcode id=67 lang=java
 *
 * [67] Add Binary
 */

// @lc code=start
class Solution {
    public String addBinary(String a, String b) {
        if (null == a || a.isEmpty()) {
			return b;
		}
		if (null == b || b.isEmpty()) {
			return a;
		}
		StringBuilder builder = new StringBuilder();

		int sum = 0;
		int carry = 0;
		int i = a.length() - 1, j = b.length() - 1;
		while(i >= 0 || j >= 0) {
			sum = carry;
			if (i >= 0) {
				sum += a.charAt(i) - '0';
				i -= 1;
			}
			if (j >= 0) {
				sum += b.charAt(j) - '0';
				j -= 1;
			}
			builder.append(sum % 2);
			carry = sum / 2;
		}
		if(carry != 0) {
			builder.append(carry);
		}

		//返回最终结果
		return builder.reverse().toString();
    }
}
// @lc code=end

