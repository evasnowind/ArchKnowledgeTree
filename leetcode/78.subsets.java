import java.util.Deque;
import java.util.LinkedList;

/*
 * @lc app=leetcode id=78 lang=java
 *
 * [78] Subsets
 */

// @lc code=start
class Solution {
    public List<List<Integer>> subsets(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        if (null == nums || nums.length == 0) {
            return res;
        }

        Deque<Integer> path = new LinkedList<>();
        backtrack(nums, 0, path, res);

        return res;
    }

    private void backtrack(int[] nums, int start, Deque<Integer> path, List<List<Integer>> res) {
        /*
        题目已规定输入不会有重复数字，那么利用回溯过程，每次回溯都会产生一个子集。
        */
        res.add(new ArrayList<>(path));

        for(int i = start; i < nums.length; i++) {
            path.addLast(nums[i]);
            backtrack(nums, i+1, path, res);
            //回溯完清理现场
            path.removeLast();
        }
    }
}
// @lc code=end

