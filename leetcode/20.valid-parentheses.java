import java.util.Deque;

/*
 * @lc app=leetcode id=20 lang=java
 *
 * [20] Valid Parentheses
 */

// @lc code=start
class Solution {
    public boolean isValid(String s) {
        Deque<Character> stack = new LinkedList<>();

        char[] chs = s.toCharArray();
     
        for (int i = 0; i < chs.length; i++) {
            char ch = chs[i];
            if (!stack.isEmpty()) {
                char peekCh = stack.peek();
                if ((peekCh == '{' && ch == '}') || (peekCh == '[' && ch == ']') || (peekCh == '(' && ch == ')')) {
                    stack.pop();
                } else {
                    stack.push(ch);
                }
            } else {
                stack.push(ch);
            }
            
        }

        return stack.isEmpty();
    }
}
// @lc code=end

