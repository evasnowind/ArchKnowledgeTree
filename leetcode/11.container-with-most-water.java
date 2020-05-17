/*
 * @lc app=leetcode id=11 lang=java
 *
 * [11] Container With Most Water
 */

// @lc code=start
class Solution {
    public int maxArea(int[] height) {
        if (null == height || height.length == 0) {
            return 0;
        }
    
        int maxArea = 0;
        int i = 0, j = height.length - 1;
        while (i < j) {
            int minH = Math.min(height[i], height[j]);
            maxArea = Math.max(minH * (j - i), maxArea);
            //下面的判断有相等，主要是为了保证第1次时能进入该循环，然后只要height[i]比当前刚找到minH要小，i就一直向前之后，保证找到一个比minH更高的值
            while(height[i] <= minH && i < j) {
                i += 1;
            }
            while(height[j] <= minH && i < j) {
                j -= 1;
            }
        }
    
        return maxArea; 
    }
}
// @lc code=end

