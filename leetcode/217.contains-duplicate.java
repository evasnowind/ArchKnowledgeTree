import java.util.Set;

/*
 * @lc app=leetcode id=217 lang=java
 *
 * [217] Contains Duplicate
 */

// @lc code=start
class Solution {
    public boolean containsDuplicate(int[] nums) {
        Set<Integer> elementSet = new HashSet<>();
        for (int i : nums) {
            if (elementSet.contains(i)) {
                return true;
            }
            elementSet.add(i);
        }

        return false;
    }
}
// @lc code=end

