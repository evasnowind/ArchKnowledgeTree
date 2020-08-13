/*
 * @lc app=leetcode id=99 lang=java
 *
 * [99] Recover Binary Search Tree
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
    public void recoverTree(TreeNode root) {
        Deque<TreeNode> stack = new LinkedList<>();
        TreeNode x = null, y = null, preNode = null;
        
        TreeNode node = root;
        while(!stack.isEmpty() || null != node) {
            while(null != node) {
                stack.push(node);
                node = node.left;
            }

            node = stack.pop();
            if (null != preNode && node.val < preNode.val) {
                y = node;
                if (null == x) {
                    x = preNode;
                } else {
                    break;
                }
            }
            
            preNode = node;
            node = node.right;
        }

        swap(x, y);
    }
    
    private void swap(TreeNode x, TreeNode y) {
        int tmp = x.val;
        x.val = y.val;
        y.val = tmp;
    }
}
// @lc code=end

