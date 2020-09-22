/*
 * @lc app=leetcode id=538 lang=java
 *
 * [538] Convert BST to Greater Tree
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
    // public TreeNode convertBST(TreeNode root) {
    //     if (null == root) {
    //         return root;
    //     }

    //     TreeNode cur = root;
    //     int sum = 0;
    //     Deque<TreeNode> stack = new LinkedList();
    //     while(!stack.isEmpty() || null != cur) {
    //         while(null != cur) {
    //             stack.push(cur);
    //             cur = cur.right;
    //         }

    //         cur = stack.pop();
    //         sum += cur.val;
    //         cur.val = sum;

    //         cur = cur.left;
    //     }
    //     return root;
    // }

    private int sum = 0;
    public TreeNode convertBST(TreeNode root) {
        /*
        递归实现反而比上面非递归实现要快~.~ 有点奇怪呀，回头可以考虑下为啥
        */
        if (null != root) {
            convertBST(root.right);
            sum += root.val;
            root.val = sum;
            convertBST(root.left);
        }
        return root;
    }
}
// @lc code=end

