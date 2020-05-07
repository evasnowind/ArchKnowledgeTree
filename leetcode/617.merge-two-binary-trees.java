/*
 * @lc app=leetcode id=617 lang=java
 *
 * [617] Merge Two Binary Trees
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

    public TreeNode mergeTrees(TreeNode t1, TreeNode t2) {
        if (null == t1 && null == t2) {
            return null;
        }

        TreeNode root = createSingleNode(t1, t2);
        buildTreeHelper(t1, t2, root);
        
        return root;
    }

    private TreeNode createSingleNode(TreeNode t1, TreeNode t2) {
        if (null == t1 && null == t2) {
            return null;
        }
        
        int val = 0;
        if (null != t1) {
            val += t1.val;
        }
        if (null != t2) {
            val += t2.val;
        }
        TreeNode node = new TreeNode(val);
        return node;
    }

    private TreeNode buildTreeHelper(TreeNode t1, TreeNode t2, TreeNode root) {
        
        TreeNode firstLeftNode = null, secondLeftNode = null;
        TreeNode firstRightNode = null, secondRightNode = null;
        if (null != t1) {
            firstLeftNode = t1.left;
            firstRightNode = t1.right;
        }
        if (null != t2) {
            secondLeftNode = t2.left;
            secondRightNode = t2.right;
        }
        root.left = createSingleNode(firstLeftNode, secondLeftNode);
        root.right = createSingleNode(firstRightNode, secondRightNode);
        
        buildTreeHelper(firstLeftNode, firstRightNode, root.left);
        buildTreeHelper(firstRightNode, secondRightNode, root.right);
        
        return root;
    }
}
// @lc code=end

