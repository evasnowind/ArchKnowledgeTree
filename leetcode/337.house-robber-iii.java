/*
 * @lc app=leetcode id=337 lang=java
 *
 * [337] House Robber III
 */

// @lc code=start
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    public int rob(TreeNode root) {
        
        if (null == root) {
            return 0;
        }

        Map<TreeNode, Integer> memo = new HashMap<>();

        return helper(root, memo);
    }

    private int helper(TreeNode root, Map<TreeNode, Integer> memo) {
        if (null == root) {
            return 0;
        }

        if (memo.containsKey(root)) {
            return memo.get(root);
        }

        int val = root.val;
        if (null != root.left) {
            val = val + helper(root.left.left, memo) + helper(root.left.right, memo);
        }
        if (null != root.right) {
            val = val + helper(root.right.left, memo) + helper(root.right.right, memo);
        }
        int res = Math.max(val, helper(root.left, memo) + helper(root.right, memo));
        memo.put(root, res);
        return res;
    }
}
// @lc code=end

