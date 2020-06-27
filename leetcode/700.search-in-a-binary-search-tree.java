/*
 * @lc app=leetcode id=700 lang=java
 *
 * [700] Search in a Binary Search Tree
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
    public TreeNode searchBST(TreeNode root, int val) {
        if (null == root) {
            return root;
        }
        
        if (root.val == val) {
            return root;
        }
        
        TreeNode curNode = root;
        while(null != curNode && val != curNode.val) {
            if (val < curNode.val) {
                curNode = curNode.left;
            } else if (val > curNode.val) {
                curNode = curNode.right;
            }
        }
        return curNode;
    }
}
// @lc code=end

