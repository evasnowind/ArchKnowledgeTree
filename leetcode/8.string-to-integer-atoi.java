
// @lc code=start
public class Solution {
    /*
Implement atoi to convert a string to an integer.

Hint: Carefully consider all possible input cases. If you want a challenge, please do not see below and ask yourself what are the possible input cases.

Notes: It is intended for this problem to be specified vaguely (ie, no given input specs). You are responsible to gather all the input requirements up front.

Update (2015-02-10):
The signature of the C++ function had been updated. If you still see your function signature accepts a const char * argument, please click the reload button  to reset your code definition.
     */
    public int myAtoi(String s) {
        if(s == null || s.trim().equalsIgnoreCase("")) {
            return 0;
        }
        String str = s.trim();
        int sign = 1, i = 0, result = 0;
        char[] chars = str.toCharArray();
        if(chars[i] == '+' || chars[i] == '-') {
            sign = chars[i++]=='-'? -1 : 1;
        }
        while(i < chars.length && chars[i] >= '0' && chars[i] <= '9') {
            if((result > Integer.MAX_VALUE / 10)
                    || (result == Integer.MAX_VALUE / 10 && (chars[i] - '0' > 7))){
                return sign == 1 ? Integer.MAX_VALUE:Integer.MIN_VALUE;
            }
            result = result * 10 + chars[i++] - '0';
        }
        return result * sign;
    }
}
// @lc code=end