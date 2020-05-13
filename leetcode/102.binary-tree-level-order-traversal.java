/*
 * @lc app=leetcode id=102 lang=java
 *
 * [102] Binary Tree Level Order Traversal
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
    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> result = new ArrayList<>();

        if (null == root) {
            return result;
        }

        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        
        while(!queue.isEmpty()) {
            queue.offer(null);
            
            List<Integer> curLevel = new ArrayList<>();
            while(null != queue.peek()) {
                TreeNode curNode = queue.poll();
                curLevel.add(curNode.val);

                if(null != curNode.left) {
                    queue.offer(curNode.left);
                }
                if(null != curNode.right) {
                    queue.offer(curNode.right);
                }
            }
            
            queue.poll();    

            result.add(curLevel);
        }

        return result;
    }
}
// @lc code=end

