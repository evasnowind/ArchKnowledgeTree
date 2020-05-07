import javax.swing.tree.TreeNode;

/*
 * @lc app=leetcode id=101 lang=java
 *
 * [101] Symmetric Tree
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
    public boolean isSymmetric(TreeNode root) {
        if (null == root) {
            return true;
        }

        return isSymmetricSubTree(root.left, root.right);
    }

    private boolean isSymmetricSubTree(TreeNode left, TreeNode right) {
        if (null == left || null == right) {
            return left == right;
        }

        if (left.val != right.val) {
            return false;
        }
        
        return isSymmetricSubTree(left.left, right.right) && isSymmetricSubTree(left.right, right.left);
    }
 
}
// @lc code=end

