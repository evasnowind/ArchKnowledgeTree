/*
 * @lc app=leetcode id=13 lang=java
 *
 * [13] Roman to Integer
 */

// @lc code=start
class Solution {
    private static HashMap<Character, Integer> table = new HashMap<Character, Integer>();
	static{
		table.put('I', 1);
		table.put('V', 5);
		table.put('X', 10);
		table.put('L', 50);
		table.put('C', 100);
		table.put('D', 500);
		table.put('M', 1000);
	}
	
	public int romanToInt(String s) {
		if(null == s || "".equalsIgnoreCase(s)) return 0;
		int len = s.length();
		int i = len - 1;
		int cur = table.get(s.charAt(i));
		int result = cur;
		for(i = s.length() - 2;i >= 0; i--){
			int tmp = table.get(s.charAt(i));
			if(tmp >= cur) {
				result += tmp;
			} else {
				result -= tmp;
			}
			cur = tmp;
		}
		return result;
	}
}
// @lc code=end

