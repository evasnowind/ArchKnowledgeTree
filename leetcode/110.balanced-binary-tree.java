/*
 * @lc app=leetcode id=110 lang=java
 *
 * [110] Balanced Binary Tree
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
    public boolean isBalanced(TreeNode root) {
        if (null == root) {
            return true;
        }

        return helper(root) != -1;
    }

    private int helper(TreeNode root) {
        if (null == root) {
            return 0;
        }

        int leftHeight = helper(root.left);
        if (-1 == leftHeight) {
            return -1;
        }
        
        int rightHeight = helper(root.right);
        if (-1 == rightHeight) {
            return -1;
        }

        if (Math.abs(leftHeight - rightHeight) <= 1) {
            return Math.max(leftHeight, rightHeight) + 1;
        }

        return -1;
    }
}
// @lc code=end

