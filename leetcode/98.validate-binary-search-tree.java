import javax.swing.tree.TreeNode;

/*
 * @lc app=leetcode id=98 lang=java
 *
 * [98] Validate Binary Search Tree
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
    public boolean isValidBST(TreeNode root) {
        return validateTree(root, null, null);
    }

    public boolean validateTree(TreeNode root, Integer minVal, Integer maxVal) {
        if (null == root) {
            return true;
        }        
        
        if (null != minVal && root.val <= minVal) {
            return false;
        }
        if (null != maxVal && root.val >= maxVal) {
            return false;
        }

        return validateTree(root.left, minVal, root.val)
                && validateTree(root.right, root.val, maxVal);
        
    }
}
// @lc code=end

