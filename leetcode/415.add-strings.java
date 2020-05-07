/*
 * @lc app=leetcode id=415 lang=java
 *
 * [415] Add Strings
 */

// @lc code=start
class Solution {
    public String addStrings(String num1, String num2) {
        char[] num1Chs = num1.toCharArray();
        char[] num2Chs = num2.toCharArray();

        int carry = 0;
        int idx1 = num1Chs.length - 1, idx2 = num2Chs.length - 1;

        int n1, n2, tmp;
        
        LinkedList<Character> list = new LinkedList<Character>();
        while(idx1 >= 0 || idx2 >= 0) {
            if (idx1 >= 0) {
                n1 = (int)num1Chs[idx1] - (int)('0');    
            } else {
                n1 = 0;
            }
            if (idx2 >= 0) {
                n2 = (int)num2Chs[idx2] - (int)('0');    
            } else {
                n2 = 0;
            }
            
            tmp = n1 + n2 + carry;
            list.add(0, Character.valueOf((char)(tmp % 10 + '0')));
            carry =  tmp / 10;
            idx1 -= 1;
            idx2 -= 1;
        }

        if (carry > 0) {
            list.add(0, Character.valueOf((char)(carry + '0')));
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
        }
        return builder.toString();
    }
}
// @lc code=end

