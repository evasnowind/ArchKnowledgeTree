/*
 * @lc app=leetcode id=129 lang=java
 *
 * [129] Sum Root to Leaf Numbers
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
    public int sumNumbers(TreeNode root) {
        return helper(root, 0);
    }
    
    private int helper(TreeNode root, int ancestorVal) {
        if (null == root) {
            return 0;
        }
        int tmp = ancestorVal * 10 + root.val;
        if (null == root.left && null == root.right) {
            return tmp;
        }
        return helper(root.left, tmp) + helper(root.right, tmp);
    }
}
// @lc code=end

