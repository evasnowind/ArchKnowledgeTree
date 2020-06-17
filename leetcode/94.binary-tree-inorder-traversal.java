import java.util.Deque;
import java.util.List;

/*
 * @lc app=leetcode id=94 lang=java
 *
 * [94] Binary Tree Inorder Traversal
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
    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        Deque<TreeNode> stack = new LinkedList<>();

        TreeNode curNode = root;
        while (null != curNode || !stack.isEmpty()) {
            while(null != curNode) {
                stack.push(curNode);
                curNode = curNode.left;
            }

            curNode = stack.pop();
            res.add(curNode.val);
            curNode = curNode.right;
        }

        return res;
    }
}
// @lc code=end

